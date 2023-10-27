/*
 * *******************************************************************************
 *  Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ******************************************************************************
 */

package org.eclipse.tractusx.managedidentitywallets.service;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.event.*;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.util.MiwIntegrationTest;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class}, classes = VerifiableCredentialServiceTest.VerifiableCredentialEventTrackerConfiguration.class)
public class VerifiableCredentialServiceTest extends MiwIntegrationTest {

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private VerifiableCredentialEventTracker verifiableCredentialEventTracker;

    @BeforeEach
    public void setup() {
        verifiableCredentialEventTracker.clear();
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialCreation() {
        final VerifiableCredential vc1 = newVerifiableCredential("did:test:1#1", "did:test:1");
        final VerifiableCredential vc2 = newVerifiableCredential("did:test:1#2", "did:test:2");
        final VerifiableCredential vc3 = newVerifiableCredential("did:test:1#3", "did:test:3");

        verifiableCredentialService.create(vc1);
        verifiableCredentialService.create(vc2);
        verifiableCredentialService.create(vc3);

        try {
            verifiableCredentialService.create(vc1);
        } catch (VerifiableCredentialAlreadyExistsException e) {
            // ignore
        }

        Assertions.assertEquals(4, verifiableCredentialEventTracker.verifiableCredentialCreatingEvents.size(), "4 VerifiableCredentialCreatingEvents should have been fired");
        Assertions.assertEquals(3, verifiableCredentialEventTracker.verifiableCredentialCreatedEvents.size(), "3 VerifiableCredentialCreatedEvents should have been fired");
        Assertions.assertEquals(3, verifiableCredentialRepository.count(), "3 VerifiableCredentials should be in the database");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialDeletion() {
        final VerifiableCredential w1 = createRandomVerifiableCredential();
        final VerifiableCredential w2 = createRandomVerifiableCredential();
        final VerifiableCredential w3 = createRandomVerifiableCredential();

        verifiableCredentialService.delete(w1);
        verifiableCredentialService.delete(w2);
        verifiableCredentialService.delete(w3);

        /*
         * From a state perspective it was never in the database,
         *  the desired state is achieved and the event is published.
         */
        verifiableCredentialService.delete(w1);

        Assertions.assertEquals(4, verifiableCredentialEventTracker.verifiableCredentialDeletingEvents.size(), "4 VerifiableCredentialDeletingEvent should have been fired");
        Assertions.assertEquals(4, verifiableCredentialEventTracker.verifiableCredentialDeletedEvents.size(), "4 VerifiableCredentialDeletedEvent should have been fired");
        Assertions.assertEquals(0, verifiableCredentialRepository.count(), "0 VerifiableCredentials should be in the database");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialFindById() {
        createVerifiableCredential("did:test:1#1", "did:test:1");
        createVerifiableCredential("did:test:2#2", "did:test:2");
        createVerifiableCredential("did:test:3#3", "did:test:3");

        final Optional<VerifiableCredential> verifiableCredential =
                verifiableCredentialService.findById(new VerifiableCredentialId("did:test:1#1"));

        Assertions.assertTrue(verifiableCredential.isPresent(), "VerifiableCredential should be present");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialFindByIssuer() {
        createVerifiableCredential("did:test:1#1", "did:test:1");
        createVerifiableCredential("did:test:2#2", "did:test:2");
        createVerifiableCredential("did:test:3#3", "did:test:2");

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialIssuer(new VerifiableCredentialIssuer("did:test:2"))
                .build();
        final Page<VerifiableCredential> verifiableCredentials = verifiableCredentialService.findAll(verifiableCredentialQuery, 0, 10);

        Assertions.assertEquals(2, verifiableCredentials.getTotalElements(), "2 VerifiableCredentials should be found");
    }

    @Configuration
    static class VerifiableCredentialEventTrackerConfiguration {
        @Bean
        public VerifiableCredentialServiceTest.VerifiableCredentialEventTracker getVerifiableCredentialEventTracker() {
            return new VerifiableCredentialServiceTest.VerifiableCredentialEventTracker();
        }
    }

    static class VerifiableCredentialEventTracker {
        final List<VerifiableCredentialCreatingEvent> verifiableCredentialCreatingEvents = new ArrayList<>();
        final List<VerifiableCredentialCreatedEvent> verifiableCredentialCreatedEvents = new ArrayList<>();
        final List<VerifiableCredentialDeletedEvent> verifiableCredentialDeletedEvents = new ArrayList<>();
        final List<VerifiableCredentialDeletingEvent> verifiableCredentialDeletingEvents = new ArrayList<>();

        @EventListener
        public void onVerifiableCredentialCreatingEvent(@NonNull VerifiableCredentialCreatingEvent event) {
            verifiableCredentialCreatingEvents.add(event);
        }

        @EventListener
        public void onVerifiableCredentialCreatedEvent(@NonNull VerifiableCredentialCreatedEvent event) {
            verifiableCredentialCreatedEvents.add(event);
        }

        @EventListener
        public void onVerifiableCredentialDeletedEvent(@NonNull VerifiableCredentialDeletedEvent event) {
            verifiableCredentialDeletedEvents.add(event);
        }

        @EventListener
        public void onVerifiableCredentialDeletingEvent(@NonNull VerifiableCredentialDeletingEvent event) {
            verifiableCredentialDeletingEvents.add(event);
        }

        public void clear() {
            verifiableCredentialCreatedEvents.clear();
            verifiableCredentialCreatingEvents.clear();
            verifiableCredentialDeletedEvents.clear();
            verifiableCredentialDeletingEvents.clear();
        }
    }
}

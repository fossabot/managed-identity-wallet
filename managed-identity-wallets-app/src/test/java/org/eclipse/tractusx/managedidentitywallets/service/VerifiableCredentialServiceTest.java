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
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.factory.MiwTestCase;
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
public class VerifiableCredentialServiceTest extends MiwTestCase {

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
        final Wallet wallet = newWalletPersisted();
        final VerifiableCredential vc1 = newVerifiableCredential(wallet);
        final VerifiableCredential vc2 = newVerifiableCredential(wallet);
        final VerifiableCredential vc3 = newVerifiableCredential(wallet);

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
        final VerifiableCredential w1 = newVerifiableCredentialPersisted();
        final VerifiableCredential w2 = newVerifiableCredentialPersisted();
        final VerifiableCredential w3 = newVerifiableCredentialPersisted();

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
        var c1 = newVerifiableCredentialPersisted();
        var c2 = newVerifiableCredentialPersisted();
        var c3 = newVerifiableCredentialPersisted();

        final Optional<VerifiableCredential> verifiableCredential =
                verifiableCredentialService.findById(new VerifiableCredentialId(c1.getId().toString()));

        Assertions.assertTrue(verifiableCredential.isPresent(), "VerifiableCredential should be present");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialFindByIssuer() {
        var c1 = newVerifiableCredentialPersisted();
        var c2 = newVerifiableCredentialPersisted();
        var c3 = newVerifiableCredentialPersisted();

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialIssuer(new VerifiableCredentialIssuer(c1.getIssuer().toString()))
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

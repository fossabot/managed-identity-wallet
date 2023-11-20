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

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.VerifiableCredentialEventTracker;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.Optional;

public class VerifiableCredentialServiceTest extends MiwTestCase {

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private VerifiableCredentialEventTracker verifiableCredentialEventTracker;

    @Test
    @SneakyThrows
    public void testVerifiableCredentialCreation() {
        final Wallet wallet = newWalletPersisted();
        final VerifiableCredential vc1 = newVerifiableCredential(wallet);
        final VerifiableCredential vc2 = newVerifiableCredential(wallet);
        final VerifiableCredential vc3 = newVerifiableCredential(wallet);

        verifiableCredentialEventTracker.clear();
        verifiableCredentialService.create(vc1);
        verifiableCredentialService.create(vc2);
        verifiableCredentialService.create(vc3);

        try {
            verifiableCredentialService.create(vc1);
        } catch (VerifiableCredentialAlreadyExistsException e) {
            // ignore
        }

        Assertions.assertEquals(3, verifiableCredentialEventTracker.getVerifiableCredentialCreatingEvents().size(), "3 VerifiableCredentialCreatingEvents should have been fired");
        Assertions.assertEquals(3, verifiableCredentialEventTracker.getVerifiableCredentialCreatedEvents().size(), "3 VerifiableCredentialCreatedEvents should have been fired");
        Assertions.assertEquals(5, verifiableCredentialRepository.count(), "3+BPN&Summary (=5) VerifiableCredentials should be in the database");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialDeletion() {
        final VerifiableCredential w1 = newWalletPlusVerifiableCredentialPersisted();
        final VerifiableCredential w2 = newWalletPlusVerifiableCredentialPersisted();
        final VerifiableCredential w3 = newWalletPlusVerifiableCredentialPersisted();

        verifiableCredentialEventTracker.clear();
        verifiableCredentialService.delete(w1);
        verifiableCredentialService.delete(w2);
        verifiableCredentialService.delete(w3);

        /*
         * From a state perspective it was never in the database,
         *  the desired state is achieved and the event is published.
         */
        verifiableCredentialService.delete(w1);

        Assertions.assertEquals(4, verifiableCredentialEventTracker.getVerifiableCredentialDeletingEvents().size(), "4 VerifiableCredentialDeletingEvent should have been fired");
        Assertions.assertEquals(4, verifiableCredentialEventTracker.getVerifiableCredentialDeletedEvents().size(), "4 VerifiableCredentialDeletedEvent should have been fired");
        Assertions.assertEquals(6, verifiableCredentialRepository.count(), "Only 6 VerifiableCredentials should be in the database (1 Bpn+Summary per Wallet)");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialFindById() {
        var c1 = newWalletPlusVerifiableCredentialPersisted();
        newWalletPlusVerifiableCredentialPersisted();
        newWalletPlusVerifiableCredentialPersisted();

        final Optional<VerifiableCredential> verifiableCredential =
                verifiableCredentialService.findById(new VerifiableCredentialId(c1.getId().toString()));

        Assertions.assertTrue(verifiableCredential.isPresent(), "VerifiableCredential should be present");
    }

    @Test
    @SneakyThrows
    public void testVerifiableCredentialFindByIssuer() {
        var c1 = newWalletPlusVerifiableCredentialPersisted();
        newWalletPlusVerifiableCredentialPersisted();
        newWalletPlusVerifiableCredentialPersisted();

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialIssuer(new VerifiableCredentialIssuer(c1.getIssuer().toString()))
                .build();
        final Page<VerifiableCredential> verifiableCredentials = verifiableCredentialService.findAll(verifiableCredentialQuery, 0, 10);

        Assertions.assertEquals(1, verifiableCredentials.getTotalElements(), "1 VerifiableCredentials should be found");
    }
}

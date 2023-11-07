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

package org.eclipse.tractusx.managedidentitywallets.repository;

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.factory.MiwIntegrationTest;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class})
public class VerifiableCredentialRepositoryTest extends MiwIntegrationTest {

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Test
    public void testCreate() {
        final VerifiableCredential verifiableCredential = createRandomVerifiableCredential();

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(new VerifiableCredentialId(verifiableCredential.getId().toString()))
                .build();
        final Optional<VerifiableCredential> result = verifiableCredentialRepository.findOne(query);

        Assertions.assertTrue(result.isPresent(), "VerifiableCredential not found");
    }

    @Test
    @SneakyThrows
    public void testDelete() {
        final VerifiableCredential verifiableCredential = createRandomVerifiableCredential();

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(new VerifiableCredentialId(verifiableCredential.getId().toString()))
                .build();

        verifiableCredentialRepository.delete(verifiableCredential);
        final Optional<VerifiableCredential> result = verifiableCredentialRepository.findOne(query);

        Assertions.assertTrue(result.isPresent(), "VerifiableCredential not found");
    }

    @Test
    @SneakyThrows
    public void testFindByIssuer() {
        final VerifiableCredential verifiableCredential = createRandomVerifiableCredential();
        createRandomVerifiableCredential();
        createRandomVerifiableCredential();

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialIssuer(new VerifiableCredentialIssuer(verifiableCredential.getIssuer().toString()))
                .build();
        final Page<VerifiableCredential> result = verifiableCredentialRepository.findAll(query, Pageable.unpaged());

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(verifiableCredential.getId(), result.getContent().get(0).getId());
    }

    @Test
    @SneakyThrows
    public void testFindByHolder() {
        final Wallet wallet = createRandomWallet();
        final VerifiableCredential verifiableCredential = createRandomVerifiableCredential();
        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId(verifiableCredential.getId().toString());

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .build();
        verifiableCredentialRepository.createWalletIntersection(verifiableCredentialId, wallet.getWalletId());
        final Page<VerifiableCredential> result = verifiableCredentialRepository.findAll(query, Pageable.unpaged());

        Assertions.assertEquals(3, result.getTotalElements());
    }

    @Test
    @SneakyThrows
    public void testFindByType() {

        createRandomVerifiableCredential();
        createRandomVerifiableCredential();
        createRandomVerifiableCredential();

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType("VerifiableCredential")))
                .build();
        final Page<VerifiableCredential> result = verifiableCredentialRepository.findAll(query, Pageable.unpaged());

        Assertions.assertEquals(3, result.getTotalElements());
    }
}

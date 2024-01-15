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

package org.eclipse.tractusx.managedidentitywallets.repository.database;

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.DismantlerVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.FrameworkVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class VerifiableCredentialRepositoryTest extends MiwTestCase {

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private FrameworkVerifiableCredentialFactory frameworkVerifiableCredentialFactory;

    @Test
    public void testCreate() {
        final VerifiableCredential verifiableCredential = persistenceUtil.newWalletPlusVerifiableCredentialPersisted();

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(new VerifiableCredentialId(verifiableCredential.getId().toString()))
                .build();
        final Optional<VerifiableCredential> result = verifiableCredentialRepository.findOne(query);

        Assertions.assertTrue(result.isPresent(), "VerifiableCredential not found");
    }

    @Test
    @SneakyThrows
    public void testDelete() {
        final VerifiableCredential verifiableCredential = persistenceUtil.newWalletPlusVerifiableCredentialPersisted();

        verifiableCredentialRepository.delete(verifiableCredential);

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(new VerifiableCredentialId(verifiableCredential.getId().toString()))
                .build();
        final Optional<VerifiableCredential> result = verifiableCredentialRepository.findOne(query);

        Assertions.assertTrue(result.isEmpty(), "VerifiableCredential not deleted");
    }

    @Test
    @SneakyThrows
    public void testFindByIssuer() {
        final VerifiableCredential verifiableCredential = persistenceUtil.newWalletPlusVerifiableCredentialPersisted();
        persistenceUtil.newWalletPlusVerifiableCredentialPersisted();
        persistenceUtil.newWalletPlusVerifiableCredentialPersisted();

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
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newWalletPlusVerifiableCredentialPersisted();
        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId(verifiableCredential.getId().toString());

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .build();
        verifiableCredentialRepository.createWalletIntersection(verifiableCredentialId, wallet.getWalletId());
        final Page<VerifiableCredential> result = verifiableCredentialRepository.findAll(query, Pageable.unpaged());

        Assertions.assertEquals(3, result.getTotalElements()); // newly created + bpn credential
    }

    @Test
    @SneakyThrows
    public void testFindByType() {
        persistenceUtil.newWalletPlusVerifiableCredentialPersisted();
        persistenceUtil.newWalletPlusVerifiableCredentialPersisted();
        persistenceUtil.newWalletPlusVerifiableCredentialPersisted();

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypesOr(List.of(new VerifiableCredentialType("VerifiableCredential")))
                .build();

        final Page<VerifiableCredential> result = verifiableCredentialRepository.findAll(query, Pageable.unpaged());

        Assertions.assertEquals(9, result.getTotalElements()); // 3+3+3 (newly created & bpn & summary credential)
    }


    @Test
    @SneakyThrows
    public void testFindByTypesOr() {

        final Wallet wallet = persistenceUtil.newWalletPersisted();

        final VerifiableCredentialType typeFoo = new VerifiableCredentialType("FooVerifiableCredential");
        final VerifiableCredentialType typeBar = new VerifiableCredentialType("BarVerifiableCredential");

        final VerifiableCredential fooCredential =
                frameworkVerifiableCredentialFactory.createFrameworkVerifiableCredential(wallet, typeFoo, "foo", "foo");
        final VerifiableCredential barCredential =
                frameworkVerifiableCredentialFactory.createFrameworkVerifiableCredential(wallet, typeBar, "bar", "bar");

        verifiableCredentialRepository.create(fooCredential);
        verifiableCredentialRepository.createWalletIntersection(new VerifiableCredentialId(fooCredential.getId().toString()), wallet.getWalletId());
        verifiableCredentialRepository.create(barCredential);
        verifiableCredentialRepository.createWalletIntersection(new VerifiableCredentialId(barCredential.getId().toString()), wallet.getWalletId());

        final VerifiableCredentialQuery fooQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypesOr(List.of(typeFoo))
                .build();

        final VerifiableCredentialQuery barQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypesOr(List.of(typeBar))
                .build();

        final VerifiableCredentialQuery fooBarQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypesOr(List.of(typeFoo, typeBar))
                .build();

        final Page<VerifiableCredential> resultFoo = verifiableCredentialRepository.findAll(fooQuery, Pageable.unpaged());
        final Page<VerifiableCredential> resultBar = verifiableCredentialRepository.findAll(barQuery, Pageable.unpaged());
        final Page<VerifiableCredential> resultFooBar = verifiableCredentialRepository.findAll(fooBarQuery, Pageable.unpaged());

        Assertions.assertEquals(1, resultFoo.getTotalElements());
        Assertions.assertEquals(1, resultBar.getTotalElements());
        Assertions.assertEquals(2, resultFooBar.getTotalElements());
    }
}

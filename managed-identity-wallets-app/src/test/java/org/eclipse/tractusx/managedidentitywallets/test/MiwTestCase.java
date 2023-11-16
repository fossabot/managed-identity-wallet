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

package org.eclipse.tractusx.managedidentitywallets.test;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("dev")
public abstract class MiwTestCase {

    @Autowired
    private GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Autowired
    private VerifiableCredentialEventTracker verifiableCredentialEventTracker;

    @Autowired
    private WalletEventTracker walletEventTracker;

    @BeforeEach
    public void cleanUp() {

        walletEventTracker.clear();
        verifiableCredentialEventTracker.clear();
        verifiableCredentialRepository.deleteAll();

        // delete all except authority wallet
        for (Wallet wallet : walletRepository.findAll(WalletQuery.builder().build(), Pageable.unpaged())) {
            if (wallet.getWalletId().getText().equals("BPNL000000000000")) {
                continue;
            }

            walletRepository.delete(wallet.getWalletId());
        }
    }

    @SneakyThrows
    protected VerifiableCredential newWalletPlusVerifiableCredentialPersisted() {
        final Wallet wallet = newWalletPersisted();
        return newWalletPlusVerifiableCredentialPersisted(wallet);
    }

    @SneakyThrows
    protected VerifiableCredential newWalletPlusVerifiableCredentialPersisted(@NonNull Wallet issuer) {
        final VerifiableCredential verifiableCredential = newVerifiableCredential(issuer);

        verifiableCredentialService.create(verifiableCredential);
        return verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredential.getId().toString())).orElseThrow();
    }

    protected Wallet newWalletPersisted() {
        final String random = UUID.randomUUID().toString();
        return newWalletPersisted("id" + random, "name" + random);
    }

    @SneakyThrows
    protected Wallet newWalletPersisted(String id) {
        return newWalletPersisted(id, "name" + id);
    }

    @SneakyThrows
    protected Wallet newWalletPersisted(String id, String name) {
        final Wallet wallet = newWallet(id, name);

        walletService.create(wallet);
        return walletService.findById(wallet.getWalletId()).orElseThrow();
    }

    protected Wallet newWallet(String id, String name) {
        final WalletId walletId = new WalletId(id == null ? UUID.randomUUID().toString() : id);
        final WalletName walletName = new WalletName(name == null ? UUID.randomUUID().toString() : name);

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of())
                .build();
    }

    protected VerifiableCredential newVerifiableCredential(Wallet issuer) {

        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs args
                = GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                .issuerWallet(issuer)
                .subject(new VerifiableCredentialSubject(Map.of(
                        VerifiableCredentialSubject.ID, "" + UUID.randomUUID()
                )))
                .build();

        return genericVerifiableCredentialFactory.createVerifiableCredential(args);
    }

    @Configuration
    static class MiwTestCaseConfiguration {
        @Bean
        public VerifiableCredentialEventTracker getVerifiableCredentialEventTracker() {
            return new VerifiableCredentialEventTracker();
        }

        @Bean
        public WalletEventTracker getWalletEventTracker() {
            return new WalletEventTracker();
        }
    }
}

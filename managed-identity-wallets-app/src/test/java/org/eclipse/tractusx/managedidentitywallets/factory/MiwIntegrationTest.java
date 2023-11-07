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

package org.eclipse.tractusx.managedidentitywallets.factory;

import io.restassured.RestAssured;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType.VERIFIABLE_CREDENTIAL;

public abstract class MiwIntegrationTest {

    @BeforeAll
    public static void setupAll() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TestContextInitializer.PORT;
    }

    @AfterEach
    public void tearDown() {
        walletRepository.deleteAll();
        verifiableCredentialRepository.deleteAll();
    }

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @SneakyThrows
    protected VerifiableCredential createRandomVerifiableCredential() {
        final String random = UUID.randomUUID().toString();
        return createVerifiableCredential("did:test:id" + random, "did:test:issuer" + random);
    }

    @SneakyThrows
    protected VerifiableCredential createVerifiableCredential(String id, String issuer) {
        final VerifiableCredential verifiableCredential = newVerifiableCredential(id, issuer);

        verifiableCredentialRepository.create(verifiableCredential);
        return verifiableCredential;
    }

    protected Wallet createRandomWallet() {
        final String random = UUID.randomUUID().toString();
        return createWallet("id" + random, "name" + random);
    }

    @SneakyThrows
    protected Wallet createWallet(String id) {
        final Wallet wallet = newWalletObject(id, "name" + id);

        walletRepository.create(wallet);
        return wallet;
    }

    @SneakyThrows
    protected Wallet createWallet(String id, String name) {
        final Wallet wallet = newWalletObject(id, name);

        walletRepository.create(wallet);
        return wallet;
    }

    protected Wallet newWalletObject(String id, String name) {
        final WalletId walletId = new WalletId(id == null ? UUID.randomUUID().toString() : id);
        final WalletName walletName = new WalletName(name == null ? UUID.randomUUID().toString() : name);

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of())
                .build();
    }

    protected VerifiableCredential newVerifiableCredential(String id, String issuer) {
        return new VerifiableCredentialBuilder()
                .id(URI.create(id))
                .context(List.of(VerifiableCredential.DEFAULT_CONTEXT))
                .type(List.of(VERIFIABLE_CREDENTIAL))
                .issuer(URI.create(issuer))
                .credentialSubject(List.of(new VerifiableCredentialSubject(Map.of("id", "foobar"))))
                .issuanceDate(Instant.now())
                .expirationDate(Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();
    }
}

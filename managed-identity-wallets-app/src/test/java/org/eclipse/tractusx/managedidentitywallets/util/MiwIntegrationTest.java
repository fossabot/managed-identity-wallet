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

package org.eclipse.tractusx.managedidentitywallets.util;

import io.restassured.RestAssured;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.event.WalletCreatedEvent;
import org.eclipse.tractusx.managedidentitywallets.event.WalletCreatingEvent;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType.*;

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
    protected VerifiableCredential createRandomVerifiableCredential(WalletId walletId) {
        final String random = UUID.randomUUID().toString();
        return createVerifiableCredential(walletId, "did:test:id" + random, "did:test:issuer" + random);
    }

    @SneakyThrows
    protected VerifiableCredential createVerifiableCredential(WalletId walletId, String id, String issuer) {
        VerifiableCredential verifiableCredential = new VerifiableCredentialBuilder()
                .id(URI.create(id))
                .type(List.of(VERIFIABLE_CREDENTIAL))
                .issuer(URI.create(issuer))
                .credentialSubject(List.of(new VerifiableCredentialSubject(Map.of("foo", "bar"))))
                .issuanceDate(Instant.now())
                .expirationDate(Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS))
                .build();

        verifiableCredentialRepository.create(verifiableCredential, walletId);
        return verifiableCredential;
    }

    protected Wallet createRandomWallet() {
        final String random = UUID.randomUUID().toString();
        return createWallet("id" + random, "name" + random, "description" + random);
    }

    @SneakyThrows
    protected Wallet createWallet(String id, String name, String description) {
        final Wallet wallet = newWalletObject(id, name, description);

        walletRepository.create(wallet);
        return wallet;
    }

    protected Wallet newWalletObject(String id, String name, String description) {
        final WalletId walletId = new WalletId(id == null ? UUID.randomUUID().toString() : id);
        final WalletName walletName = new WalletName(name == null ? UUID.randomUUID().toString() : name);
        final WalletDescription walletDescription = new WalletDescription(description == null ? UUID.randomUUID().toString() : description);

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .walletDescription(walletDescription)
                .ed25519Keys(List.of())
                .build();
    }
}

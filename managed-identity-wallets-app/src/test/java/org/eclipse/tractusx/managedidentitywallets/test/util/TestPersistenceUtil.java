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

package org.eclipse.tractusx.managedidentitywallets.test.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.VerifiablePresentationFactory;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.database.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Component
@RequiredArgsConstructor
public class TestPersistenceUtil {

    private final GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;

    private final VerifiablePresentationFactory verifiablePresentationFactory;

    private final MIWSettings miwSettings;

    private final WalletRepository walletRepository;

    private final WalletService walletService;

    private final VerifiableCredentialRepository verifiableCredentialRepository;

    private final VerifiableCredentialService verifiableCredentialService;

    public void cleanUp() {

        verifiableCredentialRepository.deleteAll();

        // delete all except authority wallet
        for (Wallet wallet : walletRepository.findAll(WalletQuery.builder().build(), Pageable.unpaged())) {
            if (wallet.getWalletId().getText().equals(miwSettings.getAuthorityWalletBpn())) {
                continue;
            }

            walletRepository.delete(wallet.getWalletId());
        }
    }

    @SneakyThrows
    public VerifiableCredential newWalletPlusVerifiableCredentialPersisted() {
        final Wallet wallet = newWalletPersisted();
        return newWalletPlusVerifiableCredentialPersisted(wallet);
    }

    @SneakyThrows
    public VerifiablePresentation newWalletPlusVerifiablePresentationPersisted() {
        final Wallet wallet = newWalletPersisted();
        final VerifiableCredential verifiableCredential = newWalletPlusVerifiableCredentialPersisted(wallet);
        return verifiablePresentationFactory.createPresentation(wallet, List.of(verifiableCredential));
    }

    @SneakyThrows
    public JsonWebToken newWalletPlusVerifiablePresentationJwtPersisted() {
        final Wallet wallet = newWalletPersisted();
        final VerifiableCredential verifiableCredential = newWalletPlusVerifiableCredentialPersisted(wallet);
        return verifiablePresentationFactory.createPresentationAsJwt(wallet, List.of(verifiableCredential), new JsonWebTokenAudience("audience"));
    }

    @SneakyThrows
    public VerifiableCredential newWalletPlusVerifiableCredentialPersisted(@NonNull Wallet issuer) {
        final VerifiableCredential verifiableCredential = newVerifiableCredential(issuer);

        verifiableCredentialService.create(verifiableCredential);
        return verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredential.getId().toString())).orElseThrow();
    }

    public Wallet newWalletPersisted() {
        final String random = UUID.randomUUID().toString();
        return newWalletPersisted("id" + random, "name" + random);
    }

    @SneakyThrows
    public Wallet newWalletPersisted(String id) {
        return newWalletPersisted(id, "name" + id);
    }

    @SneakyThrows
    public Wallet newWalletPersisted(String id, String name) {
        final Wallet wallet = newWallet(id, name);

        walletService.create(wallet);
        return walletService.findById(wallet.getWalletId()).orElseThrow();
    }

    public Wallet newWallet(String id, String name) {
        final WalletId walletId = new WalletId(id == null ? UUID.randomUUID().toString() : id);
        final WalletName walletName = new WalletName(name == null ? UUID.randomUUID().toString() : name);

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of())
                .build();
    }

    public VerifiablePresentation newVerifiablePresentation(Wallet wallet, VerifiableCredential verifiableCredential) {
        return verifiablePresentationFactory.createPresentation(wallet, List.of(verifiableCredential));
    }

    public VerifiableCredential newVerifiableCredential(Wallet issuer) {
        return newVerifiableCredential(issuer, miwSettings.getVcExpiryDate().toInstant());
    }

    public VerifiableCredential newVerifiableCredential(Wallet issuer, Instant expirationDate) {

        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs args
                = GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                .issuerWallet(issuer)
                .expirationDate(expirationDate)
                .subject(new VerifiableCredentialSubject(Map.of(
                        VerifiableCredentialSubject.ID, "" + UUID.randomUUID()
                )))
                .build();

        return genericVerifiableCredentialFactory.createVerifiableCredential(args);
    }
}

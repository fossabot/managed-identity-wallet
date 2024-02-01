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

package org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.PersistedEd25519VerificationMethod;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519VerificationMethod;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.exception.InvalidePrivateKeyFormat;
import org.eclipse.tractusx.ssi.lib.exception.UnsupportedSignatureTypeException;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;
import org.eclipse.tractusx.ssi.lib.model.verifiable.Verifiable;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofGenerator;
import org.eclipse.tractusx.ssi.lib.proof.SignatureType;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractVerifiableDocumentFactory {

    @Autowired
    private DidFactory didFactory;
    @Autowired
    private MIWSettings miwSettings;
    @Autowired
    private WalletService walletService;
    @Autowired
    private VaultService vaultService;

    protected VerifiableCredential createdIssuedCredential(@NonNull VerifiableCredentialSubject subject, @NonNull String verifiableCredentialType) {
        return createdIssuedCredential(subject, verifiableCredentialType, Collections.emptyList(), miwSettings.getVcExpiryDate().toInstant());
    }

    protected VerifiableCredential createdIssuedCredential(@NonNull VerifiableCredentialSubject subject, @NonNull List<String> verifiableCredentialTypes, @NonNull List<URI> additionalContexts) {
        return createdIssuedCredential(subject, verifiableCredentialTypes, additionalContexts, miwSettings.getVcExpiryDate().toInstant());
    }

    protected VerifiableCredential createdIssuedCredential(@NonNull VerifiableCredentialSubject subject, @NonNull String verifiableCredentialType, @NonNull List<URI> additionalContexts) {
        return createdIssuedCredential(subject, verifiableCredentialType, additionalContexts, miwSettings.getVcExpiryDate().toInstant());
    }

    protected VerifiableCredential createdIssuedCredential(@NonNull VerifiableCredentialSubject subject, @NonNull String verifiableCredentialType, @NonNull Instant expiryDate) {
        return createdIssuedCredential(subject, verifiableCredentialType, Collections.emptyList(), expiryDate);
    }

    protected VerifiableCredential createdIssuedCredential(@NonNull VerifiableCredentialSubject subject, @NonNull String verifiableCredentialType, @NonNull List<URI> additionalContexts, @NonNull Instant expiryDate) {
        return createdIssuedCredential(subject, List.of(verifiableCredentialType), additionalContexts, expiryDate);
    }

    @SneakyThrows({UnsupportedSignatureTypeException.class, InvalidePrivateKeyFormat.class})
    protected VerifiableCredential createdIssuedCredential(@NonNull VerifiableCredentialSubject subject, @NonNull List<String> verifiableCredentialTypes, @NonNull List<URI> additionalContexts, @NonNull Instant expiryDate) {

        final List<URI> contexts = new ArrayList<>();
        contexts.addAll(miwSettings.getVcContexts());
        contexts.addAll(additionalContexts);

        // if the credential does not contain the JWS proof-context add it
        final URI jwsUri = URI.create("https://w3id.org/security/suites/jws-2020/v1");
        if (!contexts.contains(jwsUri))
            contexts.add(jwsUri);

        // distinct list of all verifiable credential types
        List<String> types = new ArrayList<>(verifiableCredentialTypes);
        types.add(VerifiableCredentialType.VERIFIABLE_CREDENTIAL);
        types = types.stream().distinct().collect(Collectors.toList());

        final Wallet issuerWallet = getIssuerWallet();
        final Did issuerDid = didFactory.generateDid(issuerWallet);
        final VerifiableCredentialBuilder builder =
                new VerifiableCredentialBuilder()
                        .context(contexts)
                        .id(URI.create(issuerDid + "#" + UUID.randomUUID()))
                        .type(types)
                        .issuer(issuerDid.toUri())
                        .expirationDate(expiryDate)
                        .issuanceDate(Instant.now())
                        .credentialSubject(subject);

        final Proof proof = generateProof(issuerWallet, builder.build());
        return builder.proof(proof).build();
    }

    protected Proof generateProof(@NonNull Wallet issuerWallet, @NonNull Verifiable verifiable) throws UnsupportedSignatureTypeException, InvalidePrivateKeyFormat {
        if (issuerWallet.getStoredEd25519Keys().isEmpty()) {
            throw new RuntimeException("No key registered for wallet " + issuerWallet.getWalletId());
        }

        final Did issuerDid = didFactory.generateDid(issuerWallet);

        final ResolvedEd25519VerificationMethod latestKey = issuerWallet.getStoredEd25519Keys()
                .stream()
                .max(Comparator.comparing(PersistedEd25519VerificationMethod::getCreatedAt))
                .map(k -> vaultService.resolveKey(issuerWallet, k))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseThrow();

        final x21559PrivateKey privateKey = new x21559PrivateKey(latestKey.getPrivateKey().getBytes());
        final URI verificationMethod = URI.create(issuerDid + "#" + latestKey.getDidFragment());
        final LinkedDataProofGenerator generator = LinkedDataProofGenerator.newInstance(SignatureType.JWS);

        return generator.createProof(verifiable, verificationMethod, privateKey);
    }

    private Wallet getIssuerWallet() {
        final WalletId walletId = new WalletId(miwSettings.getAuthorityWalletBpn());
        return walletService.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Authority Wallet not found: " + walletId));
    }
}

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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.Ed25519KeyNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.exception.InvalidePrivateKeyFormat;
import org.eclipse.tractusx.ssi.lib.exception.UnsupportedSignatureTypeException;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofGenerator;
import org.eclipse.tractusx.ssi.lib.proof.SignatureType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CxVerifiableCredentialFactory {


    private final DidFactory didFactory;
    private final MIWSettings miwSettings;
    private final WalletService walletService;
    private final VaultService vaultService;

    public VerifiableCredential createBusinessPartnerNumberCredential(@NonNull Wallet wallet) {
        final WalletId walletId = wallet.getWalletId();
        final Did did = didFactory.generateDid(wallet);

        final VerifiableCredentialSubject verifiableCredentialSubject =
                new VerifiableCredentialSubject(Map.of(
                        StringPool.TYPE, MIWVerifiableCredentialType.BPN_CREDENTIAL,
                        StringPool.ID, did.toString(),
                        StringPool.BPN, walletId.getText()));

        return createdIssuedCredential(verifiableCredentialSubject, MIWVerifiableCredentialType.BPN_CREDENTIAL);
    }

    @SneakyThrows({UnsupportedSignatureTypeException.class, Ed25519KeyNotFoundException.class, InvalidePrivateKeyFormat.class})
    public VerifiableCredential createdIssuedCredential(VerifiableCredentialSubject subject, String type) {

        final List<URI> contexts = miwSettings.vcContexts();
        final Instant expirationDate = miwSettings.vcExpiryDate().toInstant();
        final Wallet issuerWallet = getIssuerWallet();
        final Did issuerDid = didFactory.generateDid(issuerWallet);

        // if the credential does not contain the JWS proof-context add it
        final URI jwsUri = URI.create("https://w3id.org/security/suites/jws-2020/v1");
        if (!contexts.contains(jwsUri))
            contexts.add(jwsUri);

        final VerifiableCredentialBuilder builder =
                new VerifiableCredentialBuilder()
                        .context(contexts)
                        .id(URI.create(issuerDid + "#" + UUID.randomUUID()))
                        .type(List.of(VerifiableCredentialType.VERIFIABLE_CREDENTIAL, type))
                        .issuer(issuerDid.toUri())
                        .expirationDate(expirationDate)
                        .issuanceDate(Instant.now())
                        .credentialSubject(subject);

        final Proof proof = generateProof(issuerWallet, issuerDid, builder);
        return builder.proof(proof).build();
    }

    private Proof generateProof(Wallet issuerWallet, Did issuerDid, VerifiableCredentialBuilder builder) throws Ed25519KeyNotFoundException, UnsupportedSignatureTypeException, InvalidePrivateKeyFormat {
        if (issuerWallet.getStoredEd25519Keys().isEmpty()) {
            throw new RuntimeException("No key found for wallet " + issuerWallet.getWalletId());
        }

        final StoredEd25519Key key = issuerWallet.getStoredEd25519Keys()
                .stream().max(Comparator.comparing(StoredEd25519Key::getCreatedAt))
                .orElseThrow();
        final ResolvedEd25519Key resolvedEd25519Key = vaultService.resolveKey(key);
        final x21559PrivateKey privateKey = new x21559PrivateKey(resolvedEd25519Key.getPrivateKey());

        final URI verificationMethod = URI.create(issuerDid + "#" + key.getId());
        final LinkedDataProofGenerator generator = LinkedDataProofGenerator.newInstance(SignatureType.JWS);

        return generator.createProof(builder.build(), verificationMethod, privateKey);
    }

    private Wallet getIssuerWallet() {
        final WalletId walletId = new WalletId(miwSettings.authorityWalletBpn());
        return walletService.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Authority Wallet not found: " + walletId));
    }
}

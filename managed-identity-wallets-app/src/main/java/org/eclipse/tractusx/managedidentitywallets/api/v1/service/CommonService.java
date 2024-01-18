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

package org.eclipse.tractusx.managedidentitywallets.api.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.WalletNotFoundProblem;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.database.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.ssi.lib.crypt.IPrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.IPublicKey;
import org.eclipse.tractusx.ssi.lib.crypt.jwk.JsonWebKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PublicKey;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.model.did.*;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonService {

    private final WalletRepository walletRepository;
    private final VerifiableCredentialRepository verifiableCredentialRepository;

    private final VaultService vaultService;
    private final MIWSettings miwSettings;

    /**
     * Gets wallet by identifier(BPN or did).
     *
     * @param identifier the identifier
     * @return the wallet by identifier
     */
    public Wallet getWalletByIdentifier(String identifier) {
        return getWalletByBpn(asBpn(identifier));
    }

    public Wallet getWalletByBpn(String bpn) {
        final WalletId walletId = new WalletId(bpn);
        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(walletId)
                .build();
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet = walletRepository.findOne(walletQuery)
                .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found: " + bpn));

        final VerifiableCredentialQuery vcQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(walletId)
                .build();
        final Page<VerifiableCredential> verifiableCredentials = verifiableCredentialRepository.findAll(vcQuery, Pageable.unpaged());

        final Did did = getDidByBpn(bpn);
        final DidDocument didDocument = getDidDocument(wallet);

        return Wallet.builder()
                .id(wallet.getWalletId().getText())
                .bpn(bpn)
                .did(did.toString())
                .name(wallet.getWalletName().getText())
                .algorithm(StringPool.ED_25519)
                .didDocument(didDocument)
                .verifiableCredentials(verifiableCredentials.stream().toList())
                .build();
    }

    public Did getDidByBpn(String bpn) {
        return DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn);
    }

    @SneakyThrows
    public DidDocument getDidDocument(org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet) {

        Did did = getDidByBpn(wallet.getWalletId().getText());
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        didDocumentBuilder.id(did.toUri());

        for (var key : wallet.getStoredEd25519Keys()) {

            final ResolvedEd25519Key resolvedEd25519Key = vaultService.resolveKey(wallet, key).orElseThrow();
            final byte[] privateKey = resolvedEd25519Key.getPrivateKey().getBytes();
            IPrivateKey x21559PrivateKey = new x21559PrivateKey(privateKey);

            final byte[] publicKey = resolvedEd25519Key.getPublicKey().getBytes();
            IPublicKey x21559PublicKey = new x21559PublicKey(publicKey);

            final String keyId = key.getDidFragment().getText();
            JsonWebKey jwk = new JsonWebKey(keyId, x21559PublicKey, x21559PrivateKey);
            JWKVerificationMethod jwkVerificationMethod =
                    new JWKVerificationMethodBuilder().did(did).jwk(jwk).build();

            didDocumentBuilder.verificationMethod(jwkVerificationMethod);
        }

        DidDocument didDocument = didDocumentBuilder.build();
        //modify context URLs
        List<URI> context = didDocument.getContext();
        List<URI> mutableContext = new ArrayList<>(context);
        miwSettings.getDidDocumentContextUrls().forEach(uri -> {
            if (!mutableContext.contains(uri)) {
                mutableContext.add(uri);
            }
        });
        didDocument.put("@context", mutableContext);
        didDocument = DidDocument.fromJson(didDocument.toJson());
        return didDocument;
    }


    public static boolean validateExpiry(boolean withCredentialExpiryDate, VerifiableCredential verifiableCredential, Map<String, Object> response) {
        //validate expiry date
        boolean dateValidation = true;
        if (withCredentialExpiryDate) {
            Instant expirationDate = verifiableCredential.getExpirationDate();
            if (expirationDate.isBefore(Instant.now())) {
                dateValidation = false;
                response.put(StringPool.VALIDATE_EXPIRY_DATE, false);
            } else {
                response.put(StringPool.VALIDATE_EXPIRY_DATE, true);
            }
        }
        return dateValidation;
    }

    public String asBpn(String identifier) {
        try {
            Did did = DidParser.parse(identifier);
            final String methodIdentifier = did.getMethodIdentifier().toString();
            // workaround for https://github.com/eclipse-tractusx/SSI-agent-lib/issues/49
            if (methodIdentifier.contains("%3A")) {
                return methodIdentifier.substring(methodIdentifier.indexOf(":")+1, methodIdentifier.length());
            } else {
                return methodIdentifier;
            }
        } catch (Exception e) {
            // not a did ->  ignore
            return identifier;
        }
    }
}
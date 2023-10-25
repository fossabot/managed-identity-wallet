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

package org.eclipse.tractusx.managedidentitywallets.v1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.v2.service.VaultService;
import org.eclipse.tractusx.ssi.lib.crypt.IPrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.IPublicKey;
import org.eclipse.tractusx.ssi.lib.crypt.jwk.JsonWebKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PublicKey;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.exception.DidParseException;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + bpn));

        final VerifiableCredentialQuery vcQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(walletId)
                .build();
        final Page<VerifiableCredential> verifiableCredentials = verifiableCredentialRepository.findAll(vcQuery, Pageable.unpaged());

        final Did did = getDidByBpn(bpn);
        final DidDocument didDocument = getDidDocument(wallet);

        return Wallet.builder()
                .bpn(bpn)
                .did(did.toString())
                .name(wallet.getWalletName().getText())
                .algorithm(StringPool.ED_25519)
                .didDocument(didDocument)
                .verifiableCredentials(verifiableCredentials.stream().toList())
                .build();
    }

    public Did getDidByIdentifier(String identifier) {
        return getDidByBpn(asBpn(identifier));
    }

    public Did getDidByBpn(String bpn) {
        return DidWebFactory.fromHostnameAndPath(miwSettings.host(), bpn);
    }

    @SneakyThrows
    public DidDocument getDidDocument(org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet) {

        Did did = getDidByBpn(wallet.getWalletId().getText());
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        didDocumentBuilder.id(did.toUri());

        for (var key : wallet.getEd25519Keys()) {

            final byte[] privateKey = vaultService.resolvePrivateKey(key.getVaultSecret());
            IPrivateKey x21559PrivateKey = new x21559PrivateKey(privateKey);

            final byte[] publicKey = vaultService.resolvePublicKey(key.getVaultSecret());
            IPublicKey x21559PublicKey = new x21559PublicKey(publicKey);

            final String keyId = key.getDidIdentifier();
            JsonWebKey jwk = new JsonWebKey(keyId, x21559PublicKey, x21559PrivateKey);
            JWKVerificationMethod jwkVerificationMethod =
                    new JWKVerificationMethodBuilder().did(did).jwk(jwk).build();

            didDocumentBuilder.verificationMethod(jwkVerificationMethod);
        }

        DidDocument didDocument = didDocumentBuilder.build();
        //modify context URLs
        List<URI> context = didDocument.getContext();
        List<URI> mutableContext = new ArrayList<>(context);
        miwSettings.didDocumentContextUrls().forEach(uri -> {
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

    private String asBpn(String identifier) {
        try {
            Did did = DidParser.parse(identifier);
            return did.getMethodIdentifier().getValue();
        } catch (DidParseException e) {
            // not a did ->  ignore
            return identifier;
        }
    }
}

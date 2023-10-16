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
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.v1.exception.WalletNotFoundProblem;
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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WalletRepository walletRepository;
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
        final WalletEntity walletEntity = walletRepository.getByName(bpn)
                .orElseThrow(() -> new WalletNotFoundProblem("Error while parsing did " + bpn));

        final Did did = getDidByBpn(bpn);
        final DidDocument didDocument = getDidDocument(walletEntity);
        final List<VerifiableCredential> verifiableCredentials = walletEntity.getCredentialIntersections()
                .stream()
                .map(credentialIntersectionEntity -> credentialIntersectionEntity.getId().getVerifiableCredential())
                .map(credential -> {
                    try {
                        return MAPPER.readValue(credential.getJson(), Map.class);
                    } catch (Exception e) {
                        log.error("Error while parsing credential", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(VerifiableCredential::new)
                .toList();

        return Wallet.builder()
                .bpn(walletEntity.getId())
                .did(did.toString())
                .name(walletEntity.getName())
                .algorithm(StringPool.ED_25519)
                .didDocument(didDocument)
                .verifiableCredentials(verifiableCredentials)
                .build();
    }

    public Did getDidByIdentifier(String identifier) {
        return getDidByBpn(asBpn(identifier));
    }

    public Did getDidByBpn(String bpn) {
        return DidWebFactory.fromHostnameAndPath(miwSettings.host(), bpn);
    }

    @SneakyThrows
    public DidDocument getDidDocument(WalletEntity walletEntity) {

        Did did = getDidByBpn(walletEntity.getId());
        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        didDocumentBuilder.id(did.toUri());

        for (var key : walletEntity.getEd25519Keys()) {

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

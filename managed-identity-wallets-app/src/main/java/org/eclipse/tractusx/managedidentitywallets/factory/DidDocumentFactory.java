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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.crypt.IPrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.IPublicKey;
import org.eclipse.tractusx.ssi.lib.crypt.jwk.JsonWebKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PublicKey;
import org.eclipse.tractusx.ssi.lib.model.did.*;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DidDocumentFactory {

    private final DidFactory didFactory;
    private final VaultService vaultService;
    private final MIWSettings miwSettings;
    private final WalletService walletService;

    @SneakyThrows
    public DidDocument createDidDocument(WalletId walletId) {
        final Wallet wallet = walletService.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return createDidDocument(wallet);
    }

    @SneakyThrows
    public DidDocument createDidDocument(Wallet wallet) {
        final Did did = didFactory.generateDid(wallet);

        final DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        didDocumentBuilder.id(did.toUri());

        for (var key : wallet.getStoredEd25519Keys()) {

            final ResolvedEd25519Key resolvedEd25519Key = vaultService.resolveKey(key);
            final byte[] privateKey = resolvedEd25519Key.getPrivateKey();
            final IPrivateKey x21559PrivateKey = new x21559PrivateKey(privateKey);

            final byte[] publicKey = resolvedEd25519Key.getPublicKey();
            final IPublicKey x21559PublicKey = new x21559PublicKey(publicKey);

            final String keyId = key.getDidFragment().getText();
            final JsonWebKey jwk = new JsonWebKey(keyId, x21559PublicKey, x21559PrivateKey);
            final JWKVerificationMethod jwkVerificationMethod =
                    new JWKVerificationMethodBuilder().did(did).jwk(jwk).build();

            didDocumentBuilder.verificationMethod(jwkVerificationMethod);
        }

        final DidDocument didDocument = didDocumentBuilder.build();
        //modify context URLs
        final List<URI> context = didDocument.getContext();
        final List<URI> mutableContext = new ArrayList<>(context);
        miwSettings.getDidDocumentContextUrls().forEach(uri -> {
            if (!mutableContext.contains(uri)) {
                mutableContext.add(uri);
            }
        });
        didDocument.put("@context", mutableContext);
        return DidDocument.fromJson(didDocument.toJson());
    }
}

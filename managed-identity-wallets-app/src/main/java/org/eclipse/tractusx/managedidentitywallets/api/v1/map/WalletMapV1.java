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

package org.eclipse.tractusx.managedidentitywallets.api.v1.map;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.ssi.lib.crypt.jwk.JsonWebKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PublicKey;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.model.did.*;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalletMapV1 {

    private final MIWSettings miwSettings;
    private final VaultService vaultService;

    @SneakyThrows
    public org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet
        map(org.eclipse.tractusx.managedidentitywallets.models.Wallet w) {

        final var key = vaultService.resolveKey(
                w.getStoredEd25519Keys().stream().findFirst().orElseThrow());
        var keyId = key.getId().getText();

        //create did json
        Did did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), w.getWalletId().getText());

        JsonWebKey jwk = new JsonWebKey(keyId, new x21559PublicKey(key.getPublicKey()), new x21559PrivateKey(key.getPrivateKey()));
        JWKVerificationMethod jwkVerificationMethod =
                new JWKVerificationMethodBuilder().did(did).jwk(jwk).build();

        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        didDocumentBuilder.id(did.toUri());
        didDocumentBuilder.verificationMethods(List.of(jwkVerificationMethod));
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


        return org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet.builder()
                .didDocument(didDocument)
                .bpn(w.getWalletId().getText())
                .name(w.getWalletName().getText())
                .did(did.toUri().toString())
                .algorithm(StringPool.ED_25519)
                .build();
    }

}

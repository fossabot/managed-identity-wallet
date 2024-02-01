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

import com.nimbusds.jwt.SignedJWT;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.annotations.IsJsonLdValid;
import org.eclipse.tractusx.managedidentitywallets.annotations.IsSignatureValid;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.ssi.lib.crypt.octet.OctetKeyPairFactory;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.exception.InvalidePrivateKeyFormat;
import org.eclipse.tractusx.ssi.lib.exception.UnsupportedSignatureTypeException;
import org.eclipse.tractusx.ssi.lib.jwt.SignedJwtFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentationBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentationType;
import org.eclipse.tractusx.ssi.lib.serialization.jsonLd.JsonLdSerializerImpl;
import org.eclipse.tractusx.ssi.lib.serialization.jwt.SerializedJwtPresentationFactory;
import org.eclipse.tractusx.ssi.lib.serialization.jwt.SerializedJwtPresentationFactoryImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.*;

@Component
@RequiredArgsConstructor
@Validated
public class VerifiablePresentationFactory extends AbstractVerifiableDocumentFactory {

    private final DidFactory didFactory;
    private final VaultService vaultService;

    @SneakyThrows({UnsupportedSignatureTypeException.class, InvalidePrivateKeyFormat.class})
    public VerifiablePresentation createPresentation(@NonNull Wallet issuer,
                                                     @NonNull @IsJsonLdValid @IsSignatureValid List<VerifiableCredential> verifiableCredentials) {
        final Did issuerDid = didFactory.generateDid(issuer);


        // if the credential does not contain the JWS proof-context add it
        final List<URI> contexts = new ArrayList<>(List.of(VerifiablePresentation.DEFAULT_CONTEXT));
        final URI jwsUri = URI.create("https://w3id.org/security/suites/jws-2020/v1");
        if (!contexts.contains(jwsUri))
            contexts.add(jwsUri);

        final VerifiablePresentationBuilder verifiablePresentationBuilder =
                new VerifiablePresentationBuilder()
                        .context(contexts)
                        .id(URI.create(issuerDid + "#" + UUID.randomUUID()))
                        .type(List.of(VerifiablePresentationType.VERIFIABLE_PRESENTATION))
                        .verifiableCredentials(verifiableCredentials);

        final Proof proof = generateProof(issuer, verifiablePresentationBuilder.build());
        return verifiablePresentationBuilder.proof(proof).build();
    }

    @SneakyThrows({InvalidePrivateKeyFormat.class})
    public JsonWebToken createPresentationAsJwt(@NonNull Wallet issuer, @NonNull @IsJsonLdValid @IsSignatureValid List<VerifiableCredential> credentials, @NonNull JsonWebTokenAudience audience) {
        final Did issuerDid = didFactory.generateDid(issuer);
        final SerializedJwtPresentationFactory factory = createJwtFactory(issuerDid);

        final ResolvedEd25519VerificationMethod key = issuer.getStoredEd25519Keys()
                .stream()
                .max(Comparator.comparing(PersistedEd25519VerificationMethod::getCreatedAt))
                .map(k -> vaultService.resolveKey(issuer, k))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseThrow();

        final x21559PrivateKey privateKey = new x21559PrivateKey(key.getPrivateKey().getBytes());
        final SignedJWT signedJwt = factory.createPresentation(issuerDid, credentials, audience.getText(), privateKey);

        return new JsonWebToken(signedJwt.serialize());
    }

    private static SerializedJwtPresentationFactory createJwtFactory(Did issuer) {
        return new SerializedJwtPresentationFactoryImpl(
                new SignedJwtFactory(new OctetKeyPairFactory()), new JsonLdSerializerImpl(), issuer);
    }
}

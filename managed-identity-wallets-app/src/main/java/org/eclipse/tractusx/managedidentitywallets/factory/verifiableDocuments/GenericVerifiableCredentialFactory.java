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

import lombok.*;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.Ed25519KeyNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialContext;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.ssi.lib.exception.InvalidePrivateKeyFormat;
import org.eclipse.tractusx.ssi.lib.exception.UnsupportedSignatureTypeException;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GenericVerifiableCredentialFactory extends AbstractVerifiableDocumentFactory {

    private final DidFactory didFactory;
    private final MIWSettings miwSettings;

    @SneakyThrows({UnsupportedSignatureTypeException.class, Ed25519KeyNotFoundException.class, InvalidePrivateKeyFormat.class})
    public VerifiableCredential createVerifiableCredential(GenericVerifiableCredentialFactoryArgs args) {

        final List<VerifiableCredentialSubject> subject = args.getSubjects();
        final Wallet issuerWallet = args.getIssuerWallet();
        final Did issuerDid = didFactory.generateDid(issuerWallet);
        final Instant expirationDate = Optional.ofNullable(args.getExpirationDate()).orElse(miwSettings.getVcExpiryDate().toInstant());

        final List<VerifiableCredentialType> verifiableCredentialTypes = new ArrayList<>();
        Optional.ofNullable(args.getAdditionalVerifiableCredentialTypes()).ifPresent(verifiableCredentialTypes::addAll);
        if (!verifiableCredentialTypes.contains(VerifiableCredentialType.VERIFIABLE_CREDENTIAL)) {
            verifiableCredentialTypes.add(VerifiableCredentialType.VERIFIABLE_CREDENTIAL);
        }

        final List<VerifiableCredentialContext> verifiableCredentialContexts = new ArrayList<>();
        Optional.ofNullable(args.getAdditionalContexts()).ifPresent(verifiableCredentialContexts::addAll);
        if (!verifiableCredentialContexts.contains(VerifiableCredentialContext.CREDENTIALS_V1)) {
            verifiableCredentialContexts.add(VerifiableCredentialContext.CREDENTIALS_V1);
        }

        // if the credential does not contain the JWS proof-context add it
        // this proof context is used to sign the credential
        if (!verifiableCredentialContexts.contains(VerifiableCredentialContext.JWS_2020_V1))
            verifiableCredentialContexts.add(VerifiableCredentialContext.JWS_2020_V1);

        final List<URI> contexts = verifiableCredentialContexts.stream().map(VerifiableCredentialContext::getUri).toList();
        final List<String> types = verifiableCredentialTypes.stream().map(VerifiableCredentialType::getText).toList();

        final VerifiableCredentialBuilder builder =
                new VerifiableCredentialBuilder()
                        .context(contexts)
                        .id(URI.create(issuerDid + "#" + UUID.randomUUID()))
                        .type(types)
                        .issuer(issuerDid.toUri())
                        .expirationDate(expirationDate)
                        .issuanceDate(Instant.now())
                        .credentialSubject(subject);

        final Proof proof = generateProof(issuerWallet, builder.build());
        return builder.proof(proof).build();
    }

    @Builder
    @Value
    public static class GenericVerifiableCredentialFactoryArgs {
        /* Mandatory */
        @Singular
        List<VerifiableCredentialSubject> subjects;
        @NonNull
        Wallet issuerWallet;

        /* Optional */
        @Singular
        List<VerifiableCredentialContext> additionalContexts;
        @Singular
        List<VerifiableCredentialType> additionalVerifiableCredentialTypes;
        Instant expirationDate;
    }
}

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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.AbstractApiHandler;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialContext;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.IssueVerifiableCredentialRequestPayloadV2;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class PostSignedVerifiableCredentialUserApiHandler extends AbstractApiHandler {

    private final VerifiableCredentialService verifiableCredentialService;
    private final WalletService walletService;
    private final GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;

    public ResponseEntity<Map<String, Object>> execute(IssueVerifiableCredentialRequestPayloadV2 issueVerifiableCredentialRequestPayloadV2) {
        logInvocationIfDebug("userIssuedVerifiableCredential(issueVerifiableCredentialRequestPayloadV2={})", issueVerifiableCredentialRequestPayloadV2);

        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.GenericVerifiableCredentialFactoryArgsBuilder credentialFactoryArgsBuilder =
                GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder();

        final Wallet wallet = walletService.findById(TMP_WALLET_ID).orElseThrow();

        /* Subject */
        final Optional<VerifiableCredentialSubject> subject = readVerifiableCredentialSubjectArgs(issueVerifiableCredentialRequestPayloadV2.getVerifiableCredentialSubject());
        if (subject.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        credentialFactoryArgsBuilder.subject(subject.get());

        /* Wallet */
        credentialFactoryArgsBuilder.issuerWallet(wallet);

        /* Expiration Date */
        Optional.ofNullable(issueVerifiableCredentialRequestPayloadV2.getExpirationDate())
                .map(OffsetDateTime::toInstant)
                .ifPresent(credentialFactoryArgsBuilder::expirationDate);

        /* Verifiable Credential Types */
        Optional.ofNullable(issueVerifiableCredentialRequestPayloadV2.getAdditionalVerifiableCredentialTypes())
                .ifPresent(types -> types.stream()
                        .map(VerifiableCredentialType::new)
                        .forEach(credentialFactoryArgsBuilder::additionalVerifiableCredentialType));

        /* Verifiable Credential Contexts */
        Optional.ofNullable(issueVerifiableCredentialRequestPayloadV2.getAdditionalVerifiableCredentialContexts())
                .ifPresent(contexts -> contexts
                        .stream()
                        .map(URI::create)
                        .map(VerifiableCredentialContext::new)
                        .forEach(credentialFactoryArgsBuilder::additionalContext));

        final VerifiableCredential verifiableCredential = genericVerifiableCredentialFactory.createVerifiableCredential(credentialFactoryArgsBuilder.build());
        /* As the MIW should remember all issued Verifiable Credentials, it is written to the database but not (yet) linked to any wallet */
        verifiableCredentialService.create(verifiableCredential);

        return ResponseEntity.ok(verifiableCredential);
    }


}

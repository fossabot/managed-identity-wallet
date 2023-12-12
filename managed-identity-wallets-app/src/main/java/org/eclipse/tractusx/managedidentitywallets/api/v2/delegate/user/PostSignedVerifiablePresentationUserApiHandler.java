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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.AbstractApiHandler;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.VerifiablePresentationFactory;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.IssueVerifiablePresentationRequestPayloadV2;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class PostSignedVerifiablePresentationUserApiHandler extends AbstractApiHandler {

    private final WalletService walletService;
    private final VerifiablePresentationFactory verifiablePresentationFactory;

    public ResponseEntity<Map<String, Object>> execute(@NonNull IssueVerifiablePresentationRequestPayloadV2 issueVerifiablePresentationRequestPayloadV2) {
        logIfDebug("userIssuedVerifiablePresentation(issueVerifiablePresentationRequestPayloadV2={})", issueVerifiablePresentationRequestPayloadV2);

        final Wallet wallet = walletService.findById(TMP_WALLET_ID).orElseThrow();

        final Optional<List<VerifiableCredential>> verifiableCredentials =
                readVerifiableCredentialArgs(issueVerifiablePresentationRequestPayloadV2.getVerifiableCredentials());
        if (verifiableCredentials.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        final VerifiablePresentation presentation = verifiablePresentationFactory.createPresentation(wallet, verifiableCredentials.get());

        return ResponseEntity.ok(presentation);
    }


}

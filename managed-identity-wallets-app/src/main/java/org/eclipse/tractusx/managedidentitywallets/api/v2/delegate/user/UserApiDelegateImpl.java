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
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.UserApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserApiDelegateImpl implements UserApiDelegate {

    private final PostVerifiableCredentialUserApiHandler postVerifiableCredentialUserApiHandler;
    private final DeleteVerifiableCredentialByIdUserApiHandler deleteVerifiableCredentialByIdUserApiHandler;
    private final GetVerifiableCredentialByIdUserApiHandler getVerifiableCredentialByIdUserApiHandler;
    private final GetIssuedVerifiableCredentialsUserApiHandler getIssuedVerifiableCredentialsUserApiHandler;
    private final GetVerifiableCredentialsUserApiHandler getVerifiableCredentialsUserApiHandler;
    private final GetWalletUserApiHandler getWalletUserApiHandler;
    private final PostSignedVerifiableCredentialUserApiHandler postSignedVerifiableCredentialUserApiHandler;
    private final PostSignedVerifiablePresentationUserApiHandler postSignedVerifiablePresentationUserApiHandler;
    private final PostSignedVerifiablePresentationJwtUserApiHandler postSignedVerifiablePresentationJwtUserApiHandler;
    private final PostVerifiableCredentialsValidationUserApiHandler postVerifiableCredentialsValidationUserApiHandler;

    @Override
    public ResponseEntity<Map<String, Object>> userCreateVerifiableCredential(Map<String, Object> payload) {
        return postVerifiableCredentialUserApiHandler.execute(payload);
    }

    @Override
    public ResponseEntity<Void> userDeleteVerifiableCredentialById(String verifiableCredentialId) {
        return deleteVerifiableCredentialByIdUserApiHandler.execute(verifiableCredentialId);
    }

    @Override
    public ResponseEntity<Map<String, Object>> userGetVerifiableCredentialById(String verifiableCredentialId) {
        return getVerifiableCredentialByIdUserApiHandler.execute(verifiableCredentialId);
    }

    @Override
    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> userGetIssuedVerifiableCredentials(Integer page, Integer perPage, String type) {
        return getIssuedVerifiableCredentialsUserApiHandler.execute(page, perPage, type);
    }

    @Override
    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> userGetVerifiableCredentials(Integer page, Integer perPage, String type, String issuer) {
        return getVerifiableCredentialsUserApiHandler.execute(page, perPage, type, issuer);
    }

    @Override
    public ResponseEntity<WalletResponsePayloadV2> userGetWallet() {
        return getWalletUserApiHandler.execute();
    }

    @Override
    public ResponseEntity<Map<String, Object>> userIssuedVerifiableCredential(IssueVerifiableCredentialRequestPayloadV2 issueVerifiableCredentialRequestPayloadV2) {
        return postSignedVerifiableCredentialUserApiHandler.execute(issueVerifiableCredentialRequestPayloadV2);
    }

    @Override
    public ResponseEntity<Map<String, Object>> userIssuedVerifiablePresentation(@NonNull IssueVerifiablePresentationRequestPayloadV2 issueVerifiablePresentationRequestPayloadV2) {
        return postSignedVerifiablePresentationUserApiHandler.execute(issueVerifiablePresentationRequestPayloadV2);
    }

    @Override
    public ResponseEntity<IssueVerifiablePresentationJwtResponsePayloadV2> userIssuedVerifiablePresentationJwt(@NonNull IssueVerifiablePresentationJwtRequestPayloadV2 issueVerifiablePresentationJwtRequestPayloadV2) {
        return postSignedVerifiablePresentationJwtUserApiHandler.execute(issueVerifiablePresentationJwtRequestPayloadV2);
    }

    @Override
    public ResponseEntity<ValidateVerifiableCredentialResponsePayloadV2> verifiableCredentialsValidationPost(@NonNull ValidateVerifiableCredentialRequestPayloadV2 validateVerifiableCredentialRequestPayloadV2) {
        return postVerifiableCredentialsValidationUserApiHandler.execute(validateVerifiableCredentialRequestPayloadV2);
    }

    @Override
    public ResponseEntity<ValidateVerifiablePresentationJwtResponsePayloadV2> verifiablePresentationsJwtValidationPost(ValidateVerifiablePresentationJwtRequestPayloadV2 validateVerifiablePresentationJwtRequestPayloadV2) {
        return UserApiDelegate.super.verifiablePresentationsJwtValidationPost(validateVerifiablePresentationJwtRequestPayloadV2);
    }

    @Override
    public ResponseEntity<ValidateVerifiablePresentationResponsePayloadV2> verifiablePresentationsValidationPost(ValidateVerifiablePresentationRequestPayloadV2 validateVerifiablePresentationRequestPayloadV2) {
        return UserApiDelegate.super.verifiablePresentationsValidationPost(validateVerifiablePresentationRequestPayloadV2);
    }
}

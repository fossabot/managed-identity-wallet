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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.admin;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.AdministratorApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminApiDelegateImpl implements AdministratorApiDelegate {

    private final PostWalletApiAdminApiHandler postWalletApiCommand;
    private final DeleteWalletApiAdminApiHandler deleteWalletApiAdminApiHandler;
    private final GetWalletByIdAdminApiHandler getWalletByIdAdminApiHandler;
    private final GetWalletsAdminApiHandler getWalletsAdminApiHandler;
    private final PutWalletAdminApiHandler putWalletAdminApiHandler;
    private final PostVerifiableCredentialAdminApiHandler postVerifiableCredentialAdminApiHandler;
    private final GetVerifiableCredentialByIdAdminApiHandler getVerifiableCredentialByIdAdminApiHandler;
    private final DeleteVerifiableCredentialByIdAdminApiHandler deleteVerifiableCredentialByIdAdminApiHandler;
    private final GetVerifiableCredentialsAdminApiHandler getVerifiableCredentialsAdminApiHandler;

    @Override
    public ResponseEntity<CreateWalletResponsePayloadV2> adminCreateWallet(@NonNull CreateWalletRequestPayloadV2 createWalletRequestPayloadV2) {
        return postWalletApiCommand.execute(createWalletRequestPayloadV2);
    }

    @Override
    public ResponseEntity<Void> adminDeleteWalletById(@NonNull String walletId) {
        return deleteWalletApiAdminApiHandler.execute(walletId);
    }

    @Override
    public ResponseEntity<WalletResponsePayloadV2> adminGetWalletById(@NonNull String walletId) {
        return getWalletByIdAdminApiHandler.execute(walletId);
    }

    @Override
    public ResponseEntity<ListWalletsResponsePayloadV2> adminGetWallets(Integer page, Integer perPage) {
        return getWalletsAdminApiHandler.execute(page, perPage);
    }

    @Override
    public ResponseEntity<UpdateWalletResponsePayloadV2> adminUpdateWallet(@NonNull UpdateWalletRequestPayloadV2 updateWalletRequestPayloadV2) {
        return putWalletAdminApiHandler.execute(updateWalletRequestPayloadV2);
    }

    @Override
    public ResponseEntity<Map<String, Object>> adminCreateVerifiableCredential(Map<String, Object> requestBody) {
        return postVerifiableCredentialAdminApiHandler.execute(requestBody);
    }

    @Override
    public ResponseEntity<Map<String, Object>> adminGetVerifiableCredentialById(String verifiableCredentialId) {
        return getVerifiableCredentialByIdAdminApiHandler.execute(verifiableCredentialId);
    }

    @Override
    public ResponseEntity<Void> adminDeleteVerifiableCredentialById(String verifiableCredentialId) {
        return deleteVerifiableCredentialByIdAdminApiHandler.execute(verifiableCredentialId);
    }

    @Override
    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> adminGetVerifiableCredentials
            (Integer page, Integer perPage, String id, String type, String issuer, String holder) {
        return getVerifiableCredentialsAdminApiHandler.execute(page, perPage, id, type, issuer, holder);
    }
}

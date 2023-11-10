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
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
class DeleteVerifiableCredentialByIdUserApiHandler extends AbstractApiHandler {

    private final WalletService walletService;
    private final VerifiableCredentialService verifiableCredentialService;

    public ResponseEntity<Void> execute(String verifiableCredentialId) {
        logInvocationIfDebug("userDeleteVerifiableCredentialById(walletId={}, verifiableCredentialId={})", TMP_WALLET_ID, verifiableCredentialId);

        final VerifiableCredentialId id = new VerifiableCredentialId(verifiableCredentialId);
        final VerifiableCredential verifiableCredential = verifiableCredentialService.findById(id)
                .orElseThrow(() -> new VerifiableCredentialNotFoundException(id));
        verifiableCredentialService.delete(verifiableCredential);
        final Wallet wallet = walletService.findById(TMP_WALLET_ID).orElseThrow();
        walletService.removeVerifiableCredential(wallet, verifiableCredential);
        return ResponseEntity.status(204).build();
    }


}

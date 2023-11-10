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
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.AbstractApiHandler;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.ApiV2Mapper;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.UpdateWalletRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.UpdateWalletResponsePayloadV2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class PutWalletAdminApiHandler extends AbstractApiHandler {

    private final ApiV2Mapper apiMapper;
    private final WalletService walletService;

    public ResponseEntity<UpdateWalletResponsePayloadV2> execute(@NonNull UpdateWalletRequestPayloadV2 updateWalletRequestPayloadV2) {
        logInvocationIfDebug("updateWalletById(updateWalletRequestPayloadV2={})", updateWalletRequestPayloadV2);

        if (updateWalletRequestPayloadV2.getName() == null || updateWalletRequestPayloadV2.getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        final WalletId walletId = new WalletId(updateWalletRequestPayloadV2.getId());
        final WalletName newName = new WalletName(updateWalletRequestPayloadV2.getName());
        final Wallet wallet = walletService.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));

        final Wallet.WalletBuilder builder = Wallet.builder()
                .walletId(walletId)
                .walletName(newName)
                .storedEd25519Keys(wallet.getStoredEd25519Keys())
                .createdAt(wallet.getCreatedAt());
        walletService.update(builder.build());

        final Optional<Wallet> updatedWallet = walletService.findById(wallet.getWalletId());
        if (updatedWallet.isPresent()) {
            final UpdateWalletResponsePayloadV2 response = apiMapper.mapUpdateWalletResponsePayloadV2(updatedWallet.get());
            return ResponseEntity.status(202).body(response);
        } else {
            log.error("Wallet {} was not updated", wallet.getWalletId());
            return ResponseEntity.internalServerError().build();
        }
    }

}

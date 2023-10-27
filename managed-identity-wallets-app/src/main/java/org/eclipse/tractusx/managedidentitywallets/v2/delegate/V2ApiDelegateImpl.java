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

package org.eclipse.tractusx.managedidentitywallets.v2.delegate;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exceptions.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.V2ApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.*;
import org.eclipse.tractusx.managedidentitywallets.v2.map.WalletsApiMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class V2ApiDelegateImpl implements V2ApiDelegate {

    private final WalletService walletService;
    private final WalletsApiMapper walletsApiMapper;
    private final MIWSettings miwSettings;

    @Override
    public ResponseEntity<CreateWalletResponsePayloadV2> createWallet(@NonNull CreateWalletRequestPayloadV2 createWalletResponsePayloadV2) {
        if (log.isDebugEnabled()) {
            log.debug("createWallet(wallet={})", createWalletResponsePayloadV2);
        }

        final Wallet wallet = walletsApiMapper.mapCreateWalletResponsePayloadV2(createWalletResponsePayloadV2);

        try {
            walletService.create(wallet);
            final Optional<Wallet> createdWallet = walletService.findById(wallet.getWalletId());
            if (createdWallet.isPresent()) {
                final CreateWalletResponsePayloadV2 response = walletsApiMapper.mapCreateWalletResponsePayloadV2(createdWallet.get());
                final URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(wallet.getWalletId().getText())
                        .toUri();
                return ResponseEntity.created(location).body(response);
            } else {
                log.error("Wallet {} was not created", wallet.getWalletId());
                return ResponseEntity.internalServerError().build();
            }

        } catch (WalletAlreadyExistsException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteWalletById(@NonNull String walletId) {
        if (log.isDebugEnabled()) {
            log.debug("deleteWalletById(walletId={})", walletId);
        }

        walletService.findById(new WalletId(walletId)).ifPresent(walletService::delete);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WalletResponsePayloadV2> getWalletById(@NonNull String walletId) {
        if (log.isDebugEnabled()) {
            log.debug("getWalletById(walletId={})", walletId);
        }

        final Optional<Wallet> wallet = walletService.findById(new WalletId(walletId));
        if (wallet.isPresent()) {
            final WalletResponsePayloadV2 payloadV2 = walletsApiMapper.mapWalletResponsePayloadV2(wallet.get());
            return ResponseEntity.ok(payloadV2);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ListWalletsResponsePayloadV2> getWallets(Integer page, Integer perPage) {
        if (log.isDebugEnabled()) {
            log.debug("getWallets(page={}, perPage={})", page, perPage);
        }

        page = Optional.ofNullable(page).orElse(0);
        perPage = Optional.ofNullable(perPage).orElse(miwSettings.apiDefaultPageSize());

        final Page<Wallet> wallets = walletService.findAll(page, perPage);
        final ListWalletsResponsePayloadV2 response = walletsApiMapper.mapListWalletsResponsePayloadV2(wallets);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UpdateWalletResponsePayloadV2> updateWalletById(@NonNull String walletId, @NonNull UpdateWalletRequestPayloadV2 updateWalletRequestPayloadV2) {
        return V2ApiDelegate.super.updateWalletById(walletId, updateWalletRequestPayloadV2);
    }

}

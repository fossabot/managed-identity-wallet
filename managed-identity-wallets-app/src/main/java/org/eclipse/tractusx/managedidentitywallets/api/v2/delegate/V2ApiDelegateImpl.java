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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.VerifiableCredentialsMapper;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.WalletsApiMapper;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletDoesNotExistException;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.V2ApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class V2ApiDelegateImpl implements V2ApiDelegate {

    private final WalletService walletService;
    private final WalletsApiMapper walletsApiMapper;
    private final MIWSettings miwSettings;
    private final VerifiableCredentialsMapper verifiableCredentialsMapper;

    @Override
    public ResponseEntity<CreateWalletResponsePayloadV2> adminCreateWallet(@NonNull CreateWalletRequestPayloadV2 createWalletResponsePayloadV2) {
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
    public ResponseEntity<Void> adminDeleteWalletById(@NonNull String walletId) {
        if (log.isDebugEnabled()) {
            log.debug("deleteWalletById(walletId={})", walletId);
        }

        walletService.findById(new WalletId(walletId)).ifPresent(walletService::delete);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WalletResponsePayloadV2> adminGetWalletById(@NonNull String walletId) {
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
    public ResponseEntity<ListWalletsResponsePayloadV2> adminGetWallets(Integer page, Integer perPage) {
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
    public ResponseEntity<UpdateWalletResponsePayloadV2> adminUpdateWallet(@NonNull UpdateWalletRequestPayloadV2 updateWalletRequestPayloadV2) {
        if (log.isDebugEnabled()) {
            log.debug("updateWalletById(updateWalletRequestPayloadV2={})", updateWalletRequestPayloadV2);
        }
        try {
            final Wallet wallet = walletsApiMapper.mapUpdateWalletRequestPayloadV2(updateWalletRequestPayloadV2);
            walletService.update(wallet);
            final Optional<Wallet> updatedWallet = walletService.findById(wallet.getWalletId());
            if (updatedWallet.isPresent()) {
                final UpdateWalletResponsePayloadV2 response = walletsApiMapper.mapUpdateWalletResponsePayloadV2(updatedWallet.get());
                return ResponseEntity.status(202).body(response);
            } else {
                log.error("Wallet {} was not updated", wallet.getWalletId());
                return ResponseEntity.internalServerError().build();
            }

        } catch (WalletDoesNotExistException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> adminCreateVerifiableCredential(Map<String, Object> requestBody) {
        if (log.isDebugEnabled()) {
            log.debug("createVerifiableCredential(requestBody={})", requestBody);
        }

        if (!verifiableCredentialsMapper.isVerifiableCredential(requestBody)) {
            return ResponseEntity.badRequest().build();
        }

        final Map<String, Object> vc = verifiableCredentialsMapper.map(requestBody);

        return V2ApiDelegate.super.adminCreateVerifiableCredential(requestBody);
    }

    @Override
    public ResponseEntity<Void> adminDeleteVerifiableCredentialById(String verifiableCredentialId) {
        return V2ApiDelegate.super.adminDeleteVerifiableCredentialById(verifiableCredentialId);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> adminGetVerifiableCredentialById(String verifiableCredentialId) {
        return V2ApiDelegate.super.adminGetVerifiableCredentialById(verifiableCredentialId);
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> adminGetVerifiableCredentials(Integer page, Integer perPage, String id, String type, String issuer, String holder) {
        return V2ApiDelegate.super.adminGetVerifiableCredentials(page, perPage, id, type, issuer, holder);
    }
}

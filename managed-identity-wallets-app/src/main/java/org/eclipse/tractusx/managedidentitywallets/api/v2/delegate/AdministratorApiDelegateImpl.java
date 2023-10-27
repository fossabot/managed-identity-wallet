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
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.AdministratorApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.*;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdministratorApiDelegateImpl implements AdministratorApiDelegate {

    private final MIWSettings miwSettings;

    private final WalletService walletService;
    private final WalletsApiMapper apiMapper;

    private final VerifiableCredentialService verifiableCredentialService;
    private final VerifiableCredentialsMapper verifiableCredentialsMapper;

    @Override
    public ResponseEntity<CreateWalletResponsePayloadV2> adminCreateWallet(@NonNull CreateWalletRequestPayloadV2 createWalletResponsePayloadV2) {
        if (log.isDebugEnabled()) {
            log.debug("createWallet(wallet={})", createWalletResponsePayloadV2);
        }

        final Wallet wallet = apiMapper.mapCreateWalletResponsePayloadV2(createWalletResponsePayloadV2);

        try {
            walletService.create(wallet);
            final Optional<Wallet> createdWallet = walletService.findById(wallet.getWalletId());
            if (createdWallet.isPresent()) {
                final CreateWalletResponsePayloadV2 response = apiMapper.mapCreateWalletResponsePayloadV2(createdWallet.get());
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

        walletService.findById(new HolderWalletId(walletId)).ifPresent(walletService::delete);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WalletResponsePayloadV2> adminGetWalletById(@NonNull String walletId) {
        if (log.isDebugEnabled()) {
            log.debug("getWalletById(walletId={})", walletId);
        }

        final Optional<Wallet> wallet = walletService.findById(new HolderWalletId(walletId));
        if (wallet.isPresent()) {
            final WalletResponsePayloadV2 payloadV2 = apiMapper.mapWalletResponsePayloadV2(wallet.get());
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
        final ListWalletsResponsePayloadV2 response = apiMapper.mapListWalletsResponsePayloadV2(wallets);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UpdateWalletResponsePayloadV2> adminUpdateWallet(@NonNull UpdateWalletRequestPayloadV2 updateWalletRequestPayloadV2) {
        if (log.isDebugEnabled()) {
            log.debug("updateWalletById(updateWalletRequestPayloadV2={})", updateWalletRequestPayloadV2);
        }
        try {
            final Wallet wallet = apiMapper.mapUpdateWalletRequestPayloadV2(updateWalletRequestPayloadV2);
            walletService.update(wallet);
            final Optional<Wallet> updatedWallet = walletService.findById(wallet.getWalletId());
            if (updatedWallet.isPresent()) {
                final UpdateWalletResponsePayloadV2 response = apiMapper.mapUpdateWalletResponsePayloadV2(updatedWallet.get());
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

        final VerifiableCredential verifiableCredential = verifiableCredentialsMapper.map(requestBody);
        verifiableCredentialService.create(verifiableCredential);

        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId(verifiableCredential.getId().toString());
        final Optional<VerifiableCredential> createdVerifiableCredential = verifiableCredentialService.findById(verifiableCredentialId);
        if (createdVerifiableCredential.isPresent()) {
            final URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(verifiableCredentialId.getText())
                    .toUri();
            return ResponseEntity.created(location).body(createdVerifiableCredential.get());
        } else {
            log.error("Verifiable Credential {} was not created", verifiableCredential.getId());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> adminGetVerifiableCredentialById(String verifiableCredentialId) {
        if (log.isDebugEnabled()) {
            log.debug("deleteVerifiableCredentialById(verifiableCredentialId={})", verifiableCredentialId);
        }

        final Optional<VerifiableCredential> wallet = verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredentialId));
        return wallet
                .<ResponseEntity<Map<String, Object>>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> adminDeleteVerifiableCredentialById(String verifiableCredentialId) {
        if (log.isDebugEnabled()) {
            log.debug("deleteVerifiableCredentialById(verifiableCredentialId={})", verifiableCredentialId);
        }

        verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredentialId)).ifPresent(verifiableCredentialService::delete);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> adminGetVerifiableCredentials
            (Integer page, Integer perPage, String id, String type, String issuer, String holder) {

        if (log.isDebugEnabled()) {
            log.debug("getWallets(page={}, perPage={})", page, perPage);
        }

        page = Optional.ofNullable(page).orElse(0);
        perPage = Optional.ofNullable(perPage).orElse(miwSettings.apiDefaultPageSize());

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialIssuer(Optional.ofNullable(issuer).map(VerifiableCredentialIssuer::new).orElse(null))
                .verifiableCredentialId(Optional.ofNullable(id).map(VerifiableCredentialId::new).orElse(null))
                .verifiableCredentialType(Optional.ofNullable(type).map(VerifiableCredentialType::new).orElse(null))
                .holderWalletId(Optional.ofNullable(holder).map(HolderWalletId::new).orElse(null))
                .build();

        final Page<VerifiableCredential> verifiableCredentials = verifiableCredentialService.findAll(verifiableCredentialQuery, page, perPage);
        final VerifiableCredentialListResponsePayloadV2 payload = apiMapper.mapVerifiableCredentialListResponsePayloadV2(verifiableCredentials);

        return ResponseEntity.ok(payload);
    }
}

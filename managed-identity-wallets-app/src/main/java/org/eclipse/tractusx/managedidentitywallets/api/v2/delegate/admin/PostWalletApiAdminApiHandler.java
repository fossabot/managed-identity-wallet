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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.AbstractApiHandler;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.ApiV2Mapper;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.CreateWalletRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.CreateWalletResponsePayloadV2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class PostWalletApiAdminApiHandler extends AbstractApiHandler {

    private final ApiV2Mapper apiMapper;
    private final WalletService walletService;

    public ResponseEntity<CreateWalletResponsePayloadV2> execute(CreateWalletRequestPayloadV2 request) {
        logIfDebug("adminCreateWallet(request={})", request);

        final Wallet wallet = apiMapper.mapCreateWalletResponsePayloadV2(request);

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
    }
}

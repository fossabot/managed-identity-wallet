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

package org.eclipse.tractusx.managedidentitywallets.api.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.apidocs.DidDocumentControllerApiDocs;
import org.eclipse.tractusx.managedidentitywallets.api.v1.apidocs.WalletControllerApiDocs;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.service.WalletServiceV1;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * The type Wallet controller.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallets")
public class WalletController extends BaseController {

    private final MIWSettings miwSettings;

    private final WalletServiceV1 service;

    /**
     * Create wallet response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @WalletControllerApiDocs.CreateWalletApiDoc
    @PostMapping(path = RestURI.WALLETS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        final String bpn = getBpn();
        log.debug("Received request to create wallet with BPN {}. authorized by BPN: {}", request.getBpn(), bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createWallet(request, bpn));
    }

    /**
     * Store credential response entity.
     *
     * @param data       the data
     * @param identifier the identifier
     * @return the response entity
     */
    @WalletControllerApiDocs.StoreVerifiableCredentialApiDoc
    @PostMapping(path = RestURI.API_WALLETS_IDENTIFIER_CREDENTIALS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> storeCredential(@RequestBody Map<String, Object> data,
                                                               @DidDocumentControllerApiDocs.DidOrBpnParameterDoc @PathVariable(name = "identifier") String identifier) {
        final String bpn = getBpn();
        log.debug("Received request to store credential in wallet with identifier {}. authorized by BPN: {}", identifier, bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.storeCredential(data, identifier, bpn));
    }

    /**
     * Gets wallet by bpn.
     *
     * @param identifier      the identifier
     * @param withCredentials the with credentials
     * @return the wallet by bpn
     */
    @WalletControllerApiDocs.RetrieveWalletApiDoc
    @GetMapping(path = RestURI.API_WALLETS_IDENTIFIER, produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Wallet> getWalletByIdentifier( @DidDocumentControllerApiDocs.DidOrBpnParameterDoc @PathVariable(name = "identifier") String identifier,
                                                        @RequestParam(name = "withCredentials", defaultValue = "false") boolean withCredentials) {
        final String bpn = getBpn();
        log.debug("Received request to retrieve wallet with identifier {}. authorized by BPN: {}", identifier, bpn);
        return ResponseEntity.status(HttpStatus.OK).body(service.getWalletByIdentifier(identifier, withCredentials, bpn));
    }

    /**
     * Gets wallets.
     *
     * @return the wallets
     */
    @WalletControllerApiDocs.RetrieveWalletsApiDoc
    @GetMapping(path = RestURI.WALLETS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Wallet>> getWallets(
            @WalletControllerApiDocs.PageNumberParameterDoc @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @WalletControllerApiDocs.SizeParameterDoc @RequestParam(required = false, defaultValue = Integer.MAX_VALUE
                    + "") int size,
            @WalletControllerApiDocs.SortColumnParameterDoc @RequestParam(required = false, defaultValue = "createdAt") String sortColumn,
            @WalletControllerApiDocs.SortTypeParameterDoc @RequestParam(required = false, defaultValue = "desc") String sortTpe) {
        log.debug("Received request to retrieve wallets");
        if (!miwSettings.getAuthorityWalletBpn().equals(getBpn())) {
            throw new ForbiddenException();
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.getWallets(pageNumber, size, sortColumn, sortTpe));
    }
}

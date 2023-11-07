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

package org.eclipse.tractusx.managedidentitywallets.api.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.BadDataException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.DuplicateWalletProblem;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.map.WalletMapV1;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.Validate;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * The type Wallet service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceV1 {

    /**
     * The constant BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN.
     */
    public static final String BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN = "Base wallet BPN is not matching with request BPN(from token)";
    private final WalletService walletService;

    private final VerifiableCredentialService verifiableCredentialService;

    private final MIWSettings miwSettings;

    private final CommonService commonService;

    private final WalletMapV1 walletMapV1;


    /**
     * Store credential map.
     *
     * @param data       the data
     * @param identifier the identifier
     * @param callerBpn  the caller bpn
     * @return the map
     */
    public Map<String, String> storeCredential(Map<String, Object> data, String identifier, String callerBpn) {
        VerifiableCredential verifiableCredential = new VerifiableCredential(data);

        //validate BPN access
        Validate.isFalse(callerBpn.equalsIgnoreCase(identifier)).launch(new ForbiddenException("Wallet BPN is not matching with request BPN(from the token)"));

        //check type
        Validate.isTrue(verifiableCredential.getTypes().isEmpty()).launch(new BadDataException("Invalid types provided in credentials"));


        final WalletId walletId = new WalletId(identifier);
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet = walletService.findById(walletId).orElseThrow(() -> new WalletNotFoundException(walletId));

        verifiableCredentialService.create(verifiableCredential);
        walletService.storeVerifiableCredential(wallet, verifiableCredential);

        final String types = String.join(",", verifiableCredential.getTypes());
        log.debug("VC type of {} stored for bpn ->{} with id-{}", types, callerBpn, verifiableCredential.getId());
        return Map.of("message", String.format("Credential with id %s has been successfully stored", verifiableCredential.getId()));
    }


    private Wallet getWalletByIdentifier(String identifier) {
        return commonService.getWalletByIdentifier(identifier);
    }

    /**
     * Gets wallet by identifier.
     *
     * @param identifier      the identifier
     * @param withCredentials the with credentials
     * @param callerBpn       the caller bpn
     * @return the wallet by identifier
     */
    public Wallet getWalletByIdentifier(String identifier, boolean withCredentials, String callerBpn) {
        Wallet wallet = getWalletByIdentifier(identifier);

        // authority wallet can see all wallets
        if (!miwSettings.getAuthorityWalletBpn().equals(callerBpn)) {
            //validate BPN access
            Validate.isFalse(callerBpn.equalsIgnoreCase(wallet.getBpn())).launch(new ForbiddenException("Wallet BPN is not matching with request BPN(from the token)"));
        }

        if (withCredentials) {

            final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                    .holderWalletId(new WalletId(identifier))
                    .build();
            final List<VerifiableCredential> verifiableCredentials = verifiableCredentialService
                    .findAll(verifiableCredentialQuery)
                    .stream().toList();

            wallet.setVerifiableCredentials(verifiableCredentials);
        }
        return wallet;
    }


    /**
     * Gets wallets.
     *
     * @param pageNumber the page number
     * @param size       the size
     * @param sortColumn the sort column
     * @param sortType   the sort type
     * @return the wallets
     */
    public Page<Wallet> getWallets(int pageNumber, int size, String sortColumn, String sortType) {

        Sort sort = Sort.unsorted();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortType != null) {
            try {
                direction = Sort.Direction.valueOf(sortType.toUpperCase());
            } catch (Exception e) {
                log.debug("Invalid sort type provided ->{}", sortType);
            }
        }

        if (sortColumn != null) {
            switch (sortColumn.toLowerCase()) {
                case "did":
                case "bpn":
                    sort = Sort.by(direction, "walletId.text");
                    break;
                case "name":
                    sort = Sort.by(direction, "walletName.text");
                    break;
                default:
                    log.debug("Invalid sort column provided ->{}", sortColumn);
            }
        }

        final WalletQuery walletQuery = WalletQuery.builder().build();
        return walletService.findAll(walletQuery, pageNumber, size, sort)
                .map(walletMapV1::map);
    }

    /**
     * Create wallet.
     *
     * @param request the request
     * @return the wallet
     */
    @SneakyThrows
    @Transactional
    public Wallet createWallet(CreateWalletRequest request, String callerBpn) {
        validateCreateWallet(request, callerBpn);

        final WalletId walletId = new WalletId(request.getBpn());
        final WalletName walletName = new WalletName(request.getName());
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet =
                org.eclipse.tractusx.managedidentitywallets.models.Wallet.builder()
                        .walletId(walletId)
                        .walletName(walletName)
                        .build();
        walletService.create(wallet);

        return walletMapV1.map(wallet);
    }

    private void validateCreateWallet(CreateWalletRequest request, String callerBpn) {
        // check base wallet
        Validate.isFalse(callerBpn.equalsIgnoreCase(miwSettings.getAuthorityWalletBpn())).launch(new ForbiddenException(BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN));

        // check wallet already exists
        final WalletId walletId = new WalletId(request.getBpn());
        boolean exist = walletService.existsById(walletId);
        if (exist) {
            throw new DuplicateWalletProblem("Wallet is already exists for bpn " + request.getBpn());
        }
    }
}

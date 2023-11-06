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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.HoldersCredential;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.CommonUtils;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.util.verifiableCredential.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * The type Credential service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HoldersCredentialService {

    /**
     * The constant BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN.
     */
    public static final String BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN = "Base wallet BPN is not matching with request BPN(from token)";

    private final CommonService commonService;

    private final VerifiableCredentialService verifiableCredentialService;
    private final WalletService walletService;
    private final GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;


    /**
     * Gets list of holder's credentials
     *
     * @param credentialId     the credentialId
     * @param issuerIdentifier the issuer identifier
     * @param type             the type
     * @param callerBPN        the caller bpn
     * @return the credentials
     */
    public Page<VerifiableCredential> getCredentials(String credentialId, String issuerIdentifier, String sortColumn, String sortType, List<String> type, int pageNumber, int size, String callerBPN) {
        if (credentialId != null) {

            final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                    .verifiableCredentialId(new VerifiableCredentialId(credentialId))
                    .holderWalletId(new WalletId(callerBPN))
                    .build();
            final Optional<VerifiableCredential> credential = verifiableCredentialService.findOne(verifiableCredentialQuery);
            if (credential.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(pageNumber, size), 0);
            } else {
                return new PageImpl<>(Collections.singletonList(credential.get()), PageRequest.of(pageNumber, size), 1);
            }
        }

        Sort sort = Sort.unsorted();
        if (sortColumn != null) {
            final Sort.Direction direction = Sort.Direction.fromOptionalString(sortType.toUpperCase())
                    .orElse(Sort.DEFAULT_DIRECTION);

            switch (sortColumn) {
                case "createdAt":
                    sort = Sort.by(direction, VerifiableCredentialEntity.COLUMN_CREATED_AT);
                    break;
                case "credentialId":
                    sort = Sort.by(direction, VerifiableCredentialEntity.COLUMN_ID);
                    break;
                case "issuerDid":
                    log.warn("Sorting by issuer is not supported.");
                    break;
                case "type":
                    log.warn("Sorting by type is not supported. A Verifiable Credential my have multiple types.");
                    break;
                default:
                    log.warn("Sorting by {} is not supported.", sortColumn);
                    break;
            }
        }

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(new WalletId(callerBPN))
                .verifiableCredentialTypes(type.stream().map(VerifiableCredentialType::new).toList())
                .verifiableCredentialIssuer(new VerifiableCredentialIssuer(issuerIdentifier))
                .build();

        return verifiableCredentialService
                .findAll(verifiableCredentialQuery, pageNumber, size, sort);
    }

    /**
     * Issue credential verifiable credential.
     *
     * @param data      the data
     * @param callerBpn the caller bpn
     * @return the verifiable credential
     */
    public VerifiableCredential issueCredential(Map<String, Object> data, String callerBpn) {
        final VerifiableCredential verifiableCredential = new VerifiableCredential(data);
        final Wallet issuerWallet = walletService.findById(new WalletId(callerBpn)).orElseThrow();

        //validate BPN access, Holder must be caller of API
        if (!callerBpn.equals(issuerWallet.getWalletId())) {
            throw new ForbiddenException(BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN);
        }

        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs factoryArgs =
                GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                        .subjects(verifiableCredential.getCredentialSubject())
                        .issuerWallet(issuerWallet)
                        .build();

        final VerifiableCredential credential = genericVerifiableCredentialFactory.createVerifiableCredential(factoryArgs);

        //Store Credential in holder table
        verifiableCredentialService.create(verifiableCredential);
        final WalletId walletId = new WalletId(callerBpn);
        final Optional<org.eclipse.tractusx.managedidentitywallets.models.Wallet> wallet = walletService.findById(walletId);
        if (wallet.isEmpty()) {
            throw new WalletNotFoundException(walletId);
        }
        walletService.storeVerifiableCredential(wallet.get(), verifiableCredential);

        log.debug("VC type of {} issued to bpn ->{}", StringEscapeUtils.escapeJava(verifiableCredential.getTypes().toString()), StringEscapeUtils.escapeJava(callerBpn));
        // Return VC
        return credential;
    }
}

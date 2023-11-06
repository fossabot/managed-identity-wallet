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

package org.eclipse.tractusx.managedidentitywallets.util.verifiableCredential;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.util.DidFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SummaryAbstractVerifiableCredentialFactory extends AbstractVerifiableCredentialFactory {

    private final DidFactory didFactory;
    private final MIWSettings miwSettings;
    private final VerifiableCredentialService verifiableCredentialService;

    public VerifiableCredential createSummaryVerifiableCredential(@NonNull Wallet wallet) {

        final Did holderDid = didFactory.generateDid(wallet);
        final List<String> items = getFrameworkVcItems(wallet);
        final Instant expirationDate = getExpirationDate(wallet);

        final VerifiableCredentialSubject subject = new VerifiableCredentialSubject(Map.of(
                StringPool.ID, holderDid,
                StringPool.HOLDER_IDENTIFIER, wallet.getWalletId(),
                StringPool.ITEMS, items,
                StringPool.TYPE, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL,
                StringPool.CONTRACT_TEMPLATE, miwSettings.contractTemplatesUrl()));

        return createdIssuedCredential(subject, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL, expirationDate);
    }

    private List<String> getFrameworkVcItems(@NonNull Wallet wallet) {
        final WalletId walletId = wallet.getWalletId();
        final Set<String> frameworkVcTypes = miwSettings.supportedFrameworkVCTypes();
        final List<String> vcItems = new ArrayList<>();

        for (var vcType : frameworkVcTypes) {
            final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                    .verifiableCredentialTypes(List.of(new VerifiableCredentialType(vcType)))
                    .holderWalletId(walletId)
                    .build();
            final Page<VerifiableCredential> vcCredentials =
                    verifiableCredentialService.findAll(verifiableCredentialQuery);

            vcCredentials.stream()
                    .max(Comparator.comparing(VerifiableCredential::getIssuanceDate))
                    .ifPresent(vc -> vcItems.add(vcType));
        }

        return vcItems;
    }

    private Instant getExpirationDate(@NonNull Wallet wallet) {
        final WalletId walletId = wallet.getWalletId();
        Instant expirationDate = Instant.now();
        final Set<String> frameworkVcTypes = miwSettings.supportedFrameworkVCTypes();

        for (var vcType : frameworkVcTypes) {
            final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                    .verifiableCredentialTypes(List.of(new VerifiableCredentialType(vcType)))
                    .holderWalletId(walletId)
                    .build();
            final Page<VerifiableCredential> vcCredentials =
                    verifiableCredentialService.findAll(verifiableCredentialQuery);

            final Optional<VerifiableCredential> latestVcCredential = vcCredentials.stream()
                    .max(Comparator.comparing(VerifiableCredential::getIssuanceDate));

            if (latestVcCredential.isPresent()) {
                // new expiration date should be the lowest date of summarized vcs
                expirationDate = latestVcCredential.get().getExpirationDate().isBefore(expirationDate) ?
                        latestVcCredential.get().getExpirationDate() : expirationDate;
            }
        }

        return expirationDate;
    }

}

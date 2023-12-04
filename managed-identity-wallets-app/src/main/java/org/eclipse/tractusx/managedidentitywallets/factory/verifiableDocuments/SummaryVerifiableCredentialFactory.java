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

package org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.config.VerifiableCredentialContextConfiguration;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SummaryVerifiableCredentialFactory extends AbstractVerifiableDocumentFactory {

    private final DidFactory didFactory;
    private final MIWSettings miwSettings;
    private final VerifiableCredentialContextConfiguration verifiableCredentialContextConfiguration;
    private final VerifiableCredentialService verifiableCredentialService;

    public VerifiableCredential createSummaryVerifiableCredential(@NonNull Wallet wallet) {

        final Did holderDid = didFactory.generateDid(wallet);
        final List<VerifiableCredential> specialCredentials = getAllSpecialVerifiableCredentials(wallet);
        final List<String> specialVcTypes = getSpecialVerifiableCredentialTypes(specialCredentials);
        final Instant expirationDate = getExpirationDate(specialCredentials);

        final VerifiableCredentialSubject subject = new VerifiableCredentialSubject(Map.of(
                StringPool.ID, holderDid.toString(),
                StringPool.HOLDER_IDENTIFIER, wallet.getWalletId().toString(),
                StringPool.ITEMS, specialVcTypes,
                StringPool.TYPE, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL,
                StringPool.CONTRACT_TEMPLATE, miwSettings.getContractTemplatesUrl()));

        final URI summaryContext = verifiableCredentialContextConfiguration.getSummaryVerifiableCredentialContext();
        return createdIssuedCredential(subject, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL, List.of(summaryContext), expirationDate);
    }

    private List<VerifiableCredential> getAllSpecialVerifiableCredentials(Wallet wallet) {

        final List<VerifiableCredentialType> types = miwSettings.getSupportedFrameworkVCTypes()
                .stream()
                .map(VerifiableCredentialType::new)
                .collect(Collectors.toList());
        types.add(new VerifiableCredentialType(MIWVerifiableCredentialType.BPN_CREDENTIAL));
        types.add(new VerifiableCredentialType(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL));
        types.add(new VerifiableCredentialType(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL));

        return getSpecialVerifiableCredentialsByType(wallet, types);
    }


    private List<String> getSpecialVerifiableCredentialTypes(@NonNull List<VerifiableCredential> verifiableCredentials) {
        final List<String> vcItems = new ArrayList<>();
        for (var specialCredential : verifiableCredentials) {
            specialCredential.getTypes()
                    .stream()
                    .filter(t -> !t.equals(VerifiableCredentialType.VERIFIABLE_CREDENTIAL.toString()))
                    .findFirst()
                    .ifPresent(vcItems::add);
        }

        return vcItems;
    }

    private Instant getExpirationDate(@NonNull List<VerifiableCredential> verifiableCredentials) {
        return verifiableCredentials
                .stream().min(Comparator.comparing(VerifiableCredential::getExpirationDate))
                .map(VerifiableCredential::getExpirationDate)
                .orElse(Instant.now());
    }

    private List<VerifiableCredential> getSpecialVerifiableCredentialsByType(Wallet wallet, List<VerifiableCredentialType> types) {
        final WalletId walletId = wallet.getWalletId();
        final WalletId isserWalletId = new WalletId(miwSettings.getAuthorityWalletBpn());
        final Did issuerDid = didFactory.generateDid(isserWalletId);
        final VerifiableCredentialIssuer issuer = new VerifiableCredentialIssuer(issuerDid.toString());

        return types.stream()
                .map(t -> VerifiableCredentialQuery.builder()
                        .verifiableCredentialTypes(List.of(t))
                        .verifiableCredentialIssuer(issuer)
                        .holderWalletId(walletId)
                        .isExpired(false)
                        .build())
                .map(verifiableCredentialService::findAll)
                .flatMap(Page::stream)
                .toList();
    }
}

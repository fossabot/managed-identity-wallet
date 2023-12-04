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

package org.eclipse.tractusx.managedidentitywallets.command;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.SummaryVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateSummaryVerifiableCredentialCommand {

    private final WalletService walletService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final SummaryVerifiableCredentialFactory summaryVerifiableCredentialFactory;

    public void execute(Wallet wallet) {
        removeAllSummaryCredentialsFromWallet(wallet);
        issueNewSummaryCredential(wallet);
    }

    private void removeAllSummaryCredentialsFromWallet(Wallet wallet) {
        final VerifiableCredentialType summaryVerifiableCredentialType = new VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL);
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypes(List.of(summaryVerifiableCredentialType))
                .build();

        final Page<VerifiableCredential> summaryCredentials = verifiableCredentialService.findAll(verifiableCredentialQuery);
        summaryCredentials.forEach(c -> walletService.removeVerifiableCredential(wallet, c));
    }

    private void issueNewSummaryCredential(Wallet wallet) {
        final VerifiableCredential summaryCredential = summaryVerifiableCredentialFactory.createSummaryVerifiableCredential(wallet);
        verifiableCredentialService.create(summaryCredential);
        walletService.storeVerifiableCredential(wallet, summaryCredential);
    }
}

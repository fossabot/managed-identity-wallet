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

package org.eclipse.tractusx.managedidentitywallets.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialStoredInWalletEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialStoringInWalletEvent;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.SummaryVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class VerifiableCredentialStoringInWalletEventEventListener {

    private final WalletService walletService;
    private final MIWSettings miwSettings;
    private final VerifiableCredentialService verifiableCredentialService;
    private final SummaryVerifiableCredentialFactory summaryVerifiableCredentialFactory;

    @EventListener
    @Transactional
    public void updateSummaryVerifiableCredential(VerifiableCredentialStoringInWalletEvent event) {

        final Wallet wallet = event.getWallet();
        final VerifiableCredential verifiableCredential = event.getVerifiableCredential();
        if (!isCxCredential(verifiableCredential)) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Updating summary verifiable credential for wallet {}", wallet.getWalletId());
        }

        removeAllSummaryCredentialsFromWallet(wallet);
        issueNewSummaryCredential(wallet);
    }

    private boolean isCxCredential(VerifiableCredential verifiableCredential) {
        return verifiableCredential.getTypes()
                .stream()
                .anyMatch(type -> type.equalsIgnoreCase(MIWVerifiableCredentialType.BPN_CREDENTIAL)
                        || type.equalsIgnoreCase(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL)
                        || type.equalsIgnoreCase(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL))
                ||
                miwSettings.getSupportedFrameworkVCTypes().stream().anyMatch(
                        type -> verifiableCredential.getTypes().stream().anyMatch(type::equalsIgnoreCase));
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

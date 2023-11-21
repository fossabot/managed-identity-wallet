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

package org.eclipse.tractusx.managedidentitywallets.cron;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.command.UpdateSummaryVerifiableCredentialCommand;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
// TODO Disable again or do this with all CX Verifiable Credentials
public class UpdateSummaryVerifiableCredentialsCron {

    private static final int PAGE_SIZE = 200;

    private final WalletService walletService;
    private final UpdateSummaryVerifiableCredentialCommand updateSummaryVerifiableCredentialCommand;

    @Scheduled(cron = "${miw.cron.updateSummaryVerifiableCredentials}", zone = "Europe/Istanbul")
    public void updateSummaryVerifiableCredentials() {
        log.info("Starting Crone Job: [Update Summary Verifiable Credentials]");

        doUpdateSummaryVerifiableCredentials(0);
    }

    public void doUpdateSummaryVerifiableCredentials(int page) {
        final Page<Wallet> walletPage = walletService.findAll(page, PAGE_SIZE);
        walletPage.forEach(updateSummaryVerifiableCredentialCommand::execute);

        if (walletPage.hasNext()) {
            doUpdateSummaryVerifiableCredentials(page + 1);
        }
    }
}

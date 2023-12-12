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

package org.eclipse.tractusx.managedidentitywallets.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.config.HealthConfiguration;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.*;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialValidationResult;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.ValidationService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerifiableCredentialContextConfigurationHealthIndicator extends AbstractHealthIndicator {
    private final MIWSettings miwSettings;
    private final WalletService walletService;
    private final ValidationService validationService;
    private final HealthConfiguration healthConfiguration;

    private final BusinessPartnerNumberVerifiableCredentialFactory businessPartnerNumberVerifiableCredentialFactory;
    private final DismantlerVerifiableCredentialFactory dismantlerVerifiableCredentialFactory;
    private final FrameworkVerifiableCredentialFactory frameworkVerifiableCredentialFactory;
    private final MembershipVerifiableCredentialFactory membershipVerifiableCredentialFactory;
    private final SummaryVerifiableCredentialFactory summaryVerifiableCredentialFactory;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (!healthConfiguration.isSpecialCredentialIssuable()) {
            return;
        }

        final WalletId walletId = new WalletId(miwSettings.getAuthorityWalletBpn());
        final Wallet wallet = walletService.findById(walletId).orElseThrow();

        final List<VerifiableCredential> verifiableCredentials = createAllSpecialVerifiableCredentials(wallet);
        final VerifiableCredentialValidationResult validationResult = validationService.validate(verifiableCredentials);

        if (validationResult.isValid()) {
            builder.up();
        } else {
            for (var violations : validationResult.getVerifiableCredentialViolations()) {
                final String key = "verifiable-credential-context-configuration-violation";
                final List<String> verifiableCredentialTypes = verifiableCredentials.stream()
                        .filter(vc -> vc.getId().toString().equals(violations.getVerifiableCredentialId().toString()))
                        .map(VerifiableCredential::getTypes)
                        .findFirst().orElseThrow();
                final String message = String.format("Could not generate valid verifiable credential of types [%s]. Please check MIW configuration.",
                        String.join(", ", verifiableCredentialTypes));
                builder.withDetail(key, message);
            }
            builder.down();
        }
    }

    private List<VerifiableCredential> createAllSpecialVerifiableCredentials(Wallet wallet) {
        final String contractTemplateUrl = miwSettings.getContractTemplatesUrl();
        final List<VerifiableCredential> frameworkCredentials =
                miwSettings.getSupportedFrameworkVCTypes().stream()
                        .map(VerifiableCredentialType::new)
                        .map(type -> frameworkVerifiableCredentialFactory.createFrameworkVerifiableCredential(wallet, type, contractTemplateUrl, "1.0.0"))
                        .toList();

        final VerifiableCredential dismantlerCredential = dismantlerVerifiableCredentialFactory.createDismantlerVerifiableCredential(wallet, "activityType");
        final VerifiableCredential bpnCredential = businessPartnerNumberVerifiableCredentialFactory.createBusinessPartnerNumberCredential(wallet);
        final VerifiableCredential summaryCredential = summaryVerifiableCredentialFactory.createSummaryVerifiableCredential(wallet);
        final VerifiableCredential membershipCredential = membershipVerifiableCredentialFactory.createMembershipVerifiableCredential(wallet);

        final List<VerifiableCredential> verifiableCredentials = new ArrayList<>(frameworkCredentials);
        verifiableCredentials.add(dismantlerCredential);
        verifiableCredentials.add(bpnCredential);
        verifiableCredentials.add(summaryCredential);
        verifiableCredentials.add(membershipCredential);
        return verifiableCredentials;
    }
}

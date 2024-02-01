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
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519VerificationMethod;
import org.eclipse.tractusx.managedidentitywallets.models.PersistedEd25519VerificationMethod;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorityWalletKeyHealthIndicator extends AbstractHealthIndicator {
    private final MIWSettings miwSettings;
    private final WalletService walletService;
    private final VaultService vaultService;
    private final HealthConfiguration healthConfiguration;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if(!healthConfiguration.isAuthorityKeyReachable()){
            return;
        }

        final String bpn = Objects.requireNonNull(miwSettings.getAuthorityWalletBpn());

        final WalletId walletId = new WalletId(bpn);
        builder.withDetail("authorityWallet-id", walletId.getText());

        final Optional<Wallet> wallet = walletService.findById(walletId);
        if (wallet.isEmpty()) {
            builder.down();
            return;
        }

        final List<PersistedEd25519VerificationMethod> keys = wallet.get().getStoredEd25519Keys();
        final int keyCount = keys.size();
        builder.withDetail("authorityWallet-key-count", keyCount);
        if (keyCount == 0) {
            builder.down();
            return;
        }

        boolean allKeysPresent = true;
        for (final PersistedEd25519VerificationMethod key : keys) {
            final Optional<ResolvedEd25519VerificationMethod> resolvedKey = vaultService.resolveKey(wallet.get(), key);
            if (resolvedKey.isEmpty()) {
                final String msg = String.format("Authority wallet key not found in vault: %s", key);
                builder.withDetail("authorityWallet-key-presence", msg);
                log.error(msg);
                allKeysPresent = false;
            }
        }

        if (allKeysPresent) {
            builder.up();
        } else {
            builder.down();
        }
    }
}

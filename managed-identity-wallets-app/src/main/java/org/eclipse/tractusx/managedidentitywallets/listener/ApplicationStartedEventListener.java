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
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.service.KeyService;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationStartedEventListener {

    private final MIWSettings miwSettings;
    private final WalletService walletService;
    private final KeyService keyService;
    private final VaultService vaultService;

    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        final WalletId walletId = new WalletId(miwSettings.authorityWalletBpn());
        final WalletName walletName = new WalletName(miwSettings.authorityWalletName());

        if (walletService.existsById(walletId)) {
            log.trace("Authority wallet already exists, skipping creation");
            return;
        }

        final DidFragment didFragment = new DidFragment("key-1");
        final ResolvedEd25519Key resolvedEd25519Key = keyService.generateNewEd25519Key(didFragment);
        final StoredEd25519Key storedEd25519Key = vaultService.storeKey(resolvedEd25519Key);

        final Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of(storedEd25519Key))
                .build();

        log.info("Creating authority wallet with id {}", walletId.getText());
        walletService.create(wallet);
    }
}

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
import org.eclipse.tractusx.managedidentitywallets.event.WalletCreatedEvent;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.BusinessPartnerVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.Ed25519KeyFactory;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class WalletCreatedEventListener {

    private final Ed25519KeyFactory ed25519KeyFactory;
    private final BusinessPartnerVerifiableCredentialFactory businessPartnerVerifiableCredentialFactory;
    private final WalletService walletService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final VaultService vaultService;

    @EventListener
    @Transactional
    public void generateEd25519Key(WalletCreatedEvent event) {

        final Wallet wallet = event.getWallet();
        final WalletId walletId = event.getWallet().getWalletId();

        final boolean hasKeys = !wallet.getStoredEd25519Keys().isEmpty();
        if (hasKeys) {
            return;
        }

        log.trace("Generating new key for wallet {}", walletId);
        final DidFragment didFragment = new DidFragment("key-1");
        final ResolvedEd25519Key resolvedEd25519Key = ed25519KeyFactory.generateNewEd25519Key(didFragment);

        final StoredEd25519Key storedEd25519Key = vaultService.storeKey(resolvedEd25519Key);

        final Wallet updatedWallet = Wallet.builder()
                .walletId(wallet.getWalletId())
                .walletName(wallet.getWalletName())
                .storedEd25519Keys(List.of(storedEd25519Key))
                .build();

        walletService.update(updatedWallet);
    }

    @EventListener
    @Transactional
    public void issueBusinessPartnerCredential(WalletCreatedEvent event) {
        final Wallet wallet = event.getWallet();
        final VerifiableCredential bpnCredential = businessPartnerVerifiableCredentialFactory
                .createBusinessPartnerNumberCredential(event.getWallet());

        verifiableCredentialService.create(bpnCredential);
        walletService.storeVerifiableCredential(wallet, bpnCredential);
    }
}

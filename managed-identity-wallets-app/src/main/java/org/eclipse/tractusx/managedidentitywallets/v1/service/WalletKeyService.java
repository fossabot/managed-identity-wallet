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

package org.eclipse.tractusx.managedidentitywallets.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.v1.exception.WalletNotFoundProblem;
import org.eclipse.tractusx.managedidentitywallets.v2.service.VaultService;
import org.eclipse.tractusx.ssi.lib.crypt.ed25519.Ed25519Key;
import org.springframework.stereotype.Service;

/**
 * The type Wallet key service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletKeyService {

    private final WalletRepository walletRepository;
    private final VaultService vaultService;

    /**
     * Get private key by wallet identifier as bytes byte [ ].
     *
     * @param walletId the wallet id
     * @return the byte [ ]
     */
    @SneakyThrows
    public byte[] getPrivateKeyByWalletIdentifierAsBytes(String walletId) {
        return getPrivateKeyByWalletIdentifier(walletId).getEncoded();
    }

    /**
     * Gets private key by wallet identifier.
     *
     * @param walletId the wallet id
     * @return the private key by wallet identifier
     */
    @SneakyThrows

    public Ed25519Key getPrivateKeyByWalletIdentifier(String walletId) {
        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(new WalletId(walletId))
                .build();
        final Wallet walletEntity = walletRepository.findOne(walletQuery)
                .orElseThrow(() -> new WalletNotFoundProblem(walletId));

        var latestKey = walletEntity.getEd25519Keys().stream()
                .max((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .orElseThrow(() -> new RuntimeException("No key found for wallet " + walletId));

        final byte[] key = vaultService.resolvePrivateKey(latestKey.getVaultSecret());
        return Ed25519Key.asPrivateKey(key);
    }

}

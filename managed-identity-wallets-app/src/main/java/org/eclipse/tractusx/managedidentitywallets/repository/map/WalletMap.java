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

package org.eclipse.tractusx.managedidentitywallets.repository.map;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.exception.MappingException;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NoArgsConstructor
public class WalletMap extends AbstractMap<Wallet, WalletEntity> {
    @Override
    public Wallet map(@NonNull WalletEntity entity) throws MappingException {

        final HolderWalletId walletId = new HolderWalletId(entity.getId());
        final WalletName walletName = new WalletName(entity.getName());
        final WalletDescription walletDescription = new WalletDescription(entity.getDescription());

        final List<Ed25519Key> keys = entity.getEd25519Keys()
                .stream().map(
                        key -> Ed25519Key.builder()
                                .didIdentifier(key.getDidIdentifier())
                                .vaultSecret(key.getVaultSecret())
                                .createdAt(key.getCreatedAt().toInstant())
                                .description(key.getDescription())
                                .build()
                ).toList();

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .walletDescription(walletDescription)
                .ed25519Keys(keys)
                .build();
    }
}

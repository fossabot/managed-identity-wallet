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

        final WalletId walletId = new WalletId(entity.getId());
        final WalletName walletName = new WalletName(entity.getName());

        final List<StoredEd25519Key> keys = entity.getEd25519Keys()
                .stream().map(
                        key -> StoredEd25519Key.builder()
                                .id(new Ed25519KeyId(key.getId()))
                                .didFragment(new DidFragment(key.getDidFragment()))
                                .vaultSecret(new VaultSecret(key.getVaultSecret()))
                                .createdAt(key.getCreatedAt().toInstant())
                                .build()
                ).toList();

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(keys)
                .build();
    }
}

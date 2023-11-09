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

package org.eclipse.tractusx.managedidentitywallets.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.models.Ed25519KeyId;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.StoredEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VaultPath;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class VaultRepository {

    private final Map<VaultPath, ResolvedEd25519Key> keys = new HashMap<>();

    public Optional<ResolvedEd25519Key> resolveKey(@NonNull final WalletId walletId, Ed25519KeyId keyId) {
        if (log.isTraceEnabled()) {
            log.trace("resolveKey {}: {}", walletId, keyId);
        }

        final VaultPath vaultPath = new VaultPath(walletId, keyId);
        return Optional.ofNullable(keys.getOrDefault(vaultPath, null));
    }

    public StoredEd25519Key storeKey(@NonNull final WalletId walletId, @NonNull final ResolvedEd25519Key resolvedEd25519Key) {
        if (log.isTraceEnabled()) {
            log.trace("storeKey {}: {}", walletId, resolvedEd25519Key);
        }

        final VaultPath vaultPath = new VaultPath(walletId, resolvedEd25519Key.getId());
        keys.put(vaultPath, resolvedEd25519Key);
        return mapToStoredKey(resolvedEd25519Key);
    }

    private static StoredEd25519Key mapToStoredKey(@NonNull final ResolvedEd25519Key resolvedEd25519Key) {
        return StoredEd25519Key.builder()
                .id(resolvedEd25519Key.getId())
                .createdAt(resolvedEd25519Key.getCreatedAt())
                .didFragment(resolvedEd25519Key.getDidFragment())
                .build();
    }
}

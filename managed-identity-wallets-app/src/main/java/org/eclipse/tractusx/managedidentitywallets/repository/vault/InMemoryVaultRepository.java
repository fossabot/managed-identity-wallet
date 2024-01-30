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

package org.eclipse.tractusx.managedidentitywallets.repository.vault;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VaultIdentifier;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class InMemoryVaultRepository implements VaultRepository {

    private final Map<VaultIdentifier, ResolvedEd25519VerificationMethod> keys = new HashMap<>();

    @Override
    public Optional<ResolvedEd25519VerificationMethod> resolveKey(@NonNull final WalletId walletId, @NonNull StoredEd25519VerificationMethod storedEd25519Key) {
        final VaultIdentifier vaultIdentifier = new VaultIdentifier(walletId, storedEd25519Key.getId());
        if(log.isTraceEnabled()){
            log.trace("Resolving key with identifier {}", vaultIdentifier);
        }

        return Optional.ofNullable(keys.getOrDefault(vaultIdentifier, null));
    }

    @Override
    public StoredEd25519VerificationMethod storeKey(@NonNull final WalletId walletId, @NonNull final ResolvedEd25519VerificationMethod resolvedEd25519Key) {
        final VaultIdentifier vaultIdentifier = new VaultIdentifier(walletId, resolvedEd25519Key.getId());
        if(log.isTraceEnabled()){
            log.trace("Storing key with identifier {}", vaultIdentifier);
        }

        keys.put(vaultIdentifier, resolvedEd25519Key);
        return mapToStoredKey(resolvedEd25519Key);
    }

    private static StoredEd25519VerificationMethod mapToStoredKey(@NonNull final ResolvedEd25519VerificationMethod resolvedEd25519Key) {
        return StoredEd25519VerificationMethod.builder()
                .id(resolvedEd25519Key.getId())
                .createdAt(resolvedEd25519Key.getCreatedAt())
                .didFragment(resolvedEd25519Key.getDidFragment())
                .publicKey(mapToCypherText(resolvedEd25519Key.getPublicKey()))
                .privateKey(mapToCypherText(resolvedEd25519Key.getPrivateKey()))
                .build();
    }

    private static CypherText mapToCypherText(@NonNull final PlainText plainText) {
        return new CypherText(plainText.getBase64());
    }
}

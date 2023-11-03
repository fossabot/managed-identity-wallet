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
import org.eclipse.tractusx.managedidentitywallets.exception.Ed25519KeyNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.StoredEd25519Key;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class VaultRepository {

    private final List<ResolvedEd25519Key> keys = new ArrayList<>();

    public ResolvedEd25519Key resolveKey(@NonNull final StoredEd25519Key storedEd25519Key) throws Ed25519KeyNotFoundException {
        if (log.isTraceEnabled()) {
            log.trace("resolveKey: {}", storedEd25519Key);
        }

        return keys.stream()
                .filter(k -> k.getVaultSecret().equals(storedEd25519Key.getVaultSecret()))
                .findFirst()
                .orElseThrow();
    }

    public StoredEd25519Key storeKey(@NonNull final ResolvedEd25519Key resolvedEd25519Key) {
        if (log.isTraceEnabled()) {
            log.trace("storeKey: {}", resolvedEd25519Key);
        }

        keys.add(resolvedEd25519Key);
        return mapToStoredKey(resolvedEd25519Key);
    }

    private static StoredEd25519Key mapToStoredKey(@NonNull final ResolvedEd25519Key resolvedEd25519Key) {
        return StoredEd25519Key.builder()
                .id(resolvedEd25519Key.getId())
                .createdAt(resolvedEd25519Key.getCreatedAt())
                .didFragment(resolvedEd25519Key.getDidFragment())
                .vaultSecret(resolvedEd25519Key.getVaultSecret())
                .build();
    }
}

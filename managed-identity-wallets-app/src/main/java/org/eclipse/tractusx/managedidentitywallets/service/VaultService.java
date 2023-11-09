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

package org.eclipse.tractusx.managedidentitywallets.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.VaultRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VaultService {

    private final VaultRepository vaultRepository;

    public Optional<ResolvedEd25519Key> resolveKey(@NonNull Wallet wallet, @NonNull final Ed25519Key key) {
        return resolveKey(wallet, key.getId());
    }

    public Optional<ResolvedEd25519Key> resolveKey(@NonNull Wallet wallet, @NonNull final Ed25519KeyId keyId) {
        return wallet.getStoredEd25519Keys().stream()
                .filter(k -> k.getId().equals(keyId))
                .findFirst()
                .flatMap(k -> vaultRepository.resolveKey(wallet.getWalletId(), k.getId()));
    }

    public StoredEd25519Key storeKey(@NonNull Wallet wallet, @NonNull final ResolvedEd25519Key key) {
        return vaultRepository.storeKey(wallet.getWalletId(), key);
    }

}

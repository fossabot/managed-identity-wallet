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
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class VaultRepositoryImpl implements VaultRepository {

    @NonNull
    private final VaultTemplate vaultTemplate;

    public Optional<ResolvedEd25519VerificationMethod> resolveKey(@NonNull final WalletId walletId, @NonNull StoredEd25519VerificationMethod storedEd25519Key) {
        final VaultIdentifier vaultIdentifier = new VaultIdentifier(walletId, storedEd25519Key.getId());
        if (log.isTraceEnabled()) {
            log.trace("Resolving key with identifier {}", vaultIdentifier);
        }
        if (!existsKey(vaultIdentifier)) {
            return Optional.empty();
        }

        final PlainText publicKey = decrypt(vaultIdentifier, storedEd25519Key.getPublicKey());
        final PlainText privateKey = decrypt(vaultIdentifier, storedEd25519Key.getPrivateKey());

        final ResolvedEd25519VerificationMethod resolvedKey = ResolvedEd25519VerificationMethod.builder()
                .id(storedEd25519Key.getId())
                .createdAt(storedEd25519Key.getCreatedAt())
                .didFragment(storedEd25519Key.getDidFragment())
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
        return Optional.of(resolvedKey);
    }

    public StoredEd25519VerificationMethod storeKey(@NonNull final WalletId walletId, @NonNull final ResolvedEd25519VerificationMethod resolvedEd25519Key) {
        final VaultIdentifier vaultIdentifier = new VaultIdentifier(walletId, resolvedEd25519Key.getId());
        if (log.isTraceEnabled()) {
            log.trace("Storing key with identifier {}", vaultIdentifier);
        }

        final CypherText publicKey = encrypt(vaultIdentifier, resolvedEd25519Key.getPublicKey());
        final CypherText privateKey = encrypt(vaultIdentifier, resolvedEd25519Key.getPrivateKey());

        return StoredEd25519VerificationMethod.builder()
                .id(resolvedEd25519Key.getId())
                .createdAt(resolvedEd25519Key.getCreatedAt())
                .didFragment(resolvedEd25519Key.getDidFragment())
                .publicKey(publicKey)
                .privateKey(privateKey)
                .build();
    }

    private PlainText decrypt(@NonNull VaultIdentifier vaultIdentifier, @NonNull CypherText cypherText) {
        final String value = prepareEncryptKey(vaultIdentifier)
                .decrypt(vaultIdentifier.getIdentifier(), cypherText.getBase64());
        return new PlainText(value);
    }

    private CypherText encrypt(@NonNull VaultIdentifier vaultIdentifier, @NonNull PlainText plainText) {
        final String value = prepareEncryptKey(vaultIdentifier)
                .encrypt(vaultIdentifier.getIdentifier(), plainText.getBase64());
        return new CypherText(value);
    }

    private boolean existsKey(@NonNull VaultIdentifier vaultIdentifier) {
        final VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();
        var key = transitOperations.getKey(vaultIdentifier.getIdentifier());
        return key != null;
    }

    private VaultTransitOperations prepareEncryptKey(@NonNull VaultIdentifier vaultIdentifier) {
        final VaultTransitOperations transitOperations = vaultTemplate.opsForTransit();
        var key = transitOperations.getKey(vaultIdentifier.getIdentifier());
        if (key == null) {
            if (log.isTraceEnabled()) {
                log.trace("Creating key with identifier {}", vaultIdentifier);
            }
            transitOperations.createKey(vaultIdentifier.getIdentifier());
        } else if (!key.supportsDecryption() || !key.supportsEncryption()) {
            throw new RuntimeException("Key does not support encryption and/or decryption");
        }
        return transitOperations;
    }
}

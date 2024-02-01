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

package org.eclipse.tractusx.managedidentitywallets.annotations;

import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class IsKeysExistTest {

    @Mock
    private VaultService vaultService;

    private IsKeysExist.KeysExistValidator keysExistValidator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        keysExistValidator = new IsKeysExist.KeysExistValidator(vaultService);
    }

    @Test
    public void walletIsValidWhenAllKeysExist() {

        var key1 = createStoredKey("key1");
        var key2 = createStoredKey("key2");

        Wallet wallet = newWallet(List.of(key1, key2));
        when(vaultService.resolveKey(wallet, key1.getId())).thenReturn(Optional.of(createResolvedKey("key1")));
        when(vaultService.resolveKey(wallet, key2.getId())).thenReturn(Optional.of(createResolvedKey("key2")));

        assertTrue(keysExistValidator.isValid(wallet, null));
    }

    @Test
    public void walletIsInvalidWhenAnyKeyDoesNotExist() {

        var key1 = createStoredKey("key1");
        var key2 = createStoredKey("key2");

        Wallet wallet = newWallet(List.of(key1, key2));
        when(vaultService.resolveKey(wallet, key1.getId())).thenReturn(Optional.of(createResolvedKey("key1")));
        when(vaultService.resolveKey(wallet, key2.getId())).thenReturn(Optional.empty());

        assertFalse(keysExistValidator.isValid(wallet, null));
    }

    @Test
    public void walletIsValidWhenNoKeysStored() {
        Wallet wallet = newEmptyWallet();

        assertTrue(keysExistValidator.isValid(wallet, null));
    }

    private Wallet newEmptyWallet() {
        return newWallet(Collections.emptyList());
    }

    private Wallet newWallet(List<PersistedEd25519VerificationMethod> keys) {
        return Wallet.builder()
                .walletId(new WalletId("id-" + UUID.randomUUID()))
                .walletName(new WalletName("" + UUID.randomUUID()))
                .createdAt(OffsetDateTime.now())
                .storedEd25519Keys(keys)
                .build();
    }

    private ResolvedEd25519VerificationMethod createResolvedKey(@NonNull String id) {
        return ResolvedEd25519VerificationMethod.builder()
                .id(new Ed25519KeyId(id))
                .didFragment(new DidFragment(id))
                .createdAt(OffsetDateTime.now())
                .privateKey(new PrivateKeyPlainText(id))
                .publicKey(new PublicKeyPlainText(id))
                .build();
    }

    private PersistedEd25519VerificationMethod createStoredKey(@NonNull String id) {
        return PersistedEd25519VerificationMethod.builder()
                .id(new Ed25519KeyId(id))
                .didFragment(new DidFragment(id))
                .createdAt(OffsetDateTime.now())
                .privateKey(new PrivateKeyCypherText(id))
                .publicKey(new PublicKeyCypherText(id))
                .build();
    }
}



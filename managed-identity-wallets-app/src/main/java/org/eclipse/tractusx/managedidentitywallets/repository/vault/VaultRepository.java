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
import org.eclipse.tractusx.managedidentitywallets.models.Ed25519KeyId;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.StoredEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;

import java.util.Optional;

public interface VaultRepository {

    Optional<ResolvedEd25519Key> resolveKey(@NonNull WalletId walletId, StoredEd25519Key storedEd25519Key);

    StoredEd25519Key storeKey(@NonNull WalletId walletId, @NonNull ResolvedEd25519Key resolvedEd25519Key);
}

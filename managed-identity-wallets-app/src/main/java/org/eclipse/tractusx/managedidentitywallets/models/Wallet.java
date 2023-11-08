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

package org.eclipse.tractusx.managedidentitywallets.models;

import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class Wallet {

    @NonNull
    private final WalletId walletId;

    @NonNull
    private final WalletName walletName;

    @NonNull
    private final OffsetDateTime createdAt;

    @Builder.Default
    @NonNull
    private final List<StoredEd25519Key> storedEd25519Keys = Collections.emptyList();

    public List<StoredEd25519Key> getStoredEd25519Keys() {
        return Collections.unmodifiableList(storedEd25519Keys);
    }
}

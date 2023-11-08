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

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
@ToString(exclude = {"privateKey"})
public class ResolvedEd25519Key implements Ed25519Key {

    @NonNull
    private final Ed25519KeyId id;
    @NonNull
    private final DidFragment didFragment;
    @NonNull
    private final VaultSecret vaultSecret;
    @NonNull
    private final OffsetDateTime createdAt;

    private final byte @NonNull [] publicKey;
    private final byte @NonNull [] privateKey;
}

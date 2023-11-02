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

package org.eclipse.tractusx.managedidentitywallets.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity(name = Ed25519KeyEntity.TABLE_NAME)
@Table(name = Ed25519KeyEntity.TABLE_NAME)
public class Ed25519KeyEntity extends AbstractEntity {

    public static final String TABLE_NAME = "key_ed25519";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WALLET_ID = "wallet_id";
    public static final String COLUMN_DID_FRAGMENT = "did_fragment";

    public static final String COLUMN_VAULT_SECRET = "vault_secret";

    @Id
    @Column(name = COLUMN_ID, nullable = false)
    private String id;

    @Column(name = COLUMN_DID_FRAGMENT, nullable = false)
    private String didFragment;

    @Column(name = COLUMN_VAULT_SECRET, nullable = false)
    private String vaultSecret;

    @ManyToOne
    @JoinColumn(name = COLUMN_WALLET_ID, nullable = false)
    private WalletEntity wallet;
}

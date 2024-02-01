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
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity(name = Ed25519KeyEntity.TABLE_NAME)
@Table(name = Ed25519KeyEntity.TABLE_NAME)
@ToString
public class Ed25519KeyEntity extends AbstractEntity {

    public static final String TABLE_NAME = "key_ed25519";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WALLET_ID = "wallet_id";
    public static final String COLUMN_DID_FRAGMENT = "did_fragment";
    public static final String COLUMN_PUBLIC_KEY_CYPHER_TEXT_BASE_64 = "public_key_cypher_text_base64";
    public static final String COLUMN_PRIVATE_KEY_CYPHER_TEXT_BASE_64 = "private_key_cypher_text_base64";

    @Id
    @ToString.Include
    @Column(name = COLUMN_ID, nullable = false, updatable = false)
    private String id;

    @Column(name = COLUMN_DID_FRAGMENT, nullable = false, updatable = false)
    private String didFragment;

    @Column(name = COLUMN_PUBLIC_KEY_CYPHER_TEXT_BASE_64, nullable = false, updatable = false)
    private String publicKeyCypherTextBase64;

    @Column(name = COLUMN_PRIVATE_KEY_CYPHER_TEXT_BASE_64, nullable = false, updatable = false)
    @ToString.Exclude
    private String PrivateKeyCypherTextBase64;

    @ManyToOne
    @JoinColumn(name = COLUMN_WALLET_ID, nullable = false, updatable = false)
    private WalletEntity wallet;
}

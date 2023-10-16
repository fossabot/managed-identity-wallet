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
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.List;
import java.util.Set;


@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity(name = WalletEntity.TABLE_NAME)
@Table(name = WalletEntity.TABLE_NAME)
public class WalletEntity extends AbstractEntity {

    public static final String TABLE_NAME = "wallet";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";

    @Id
    private String id;

    @Unique
    private String name;

    private String description;

    @OneToMany
    private List<Ed25519KeyEntity> ed25519Keys;

    @OneToMany(mappedBy = "verifiableCredential", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VerifiableCredentialIntersectionEntity> credentialIntersections;

}

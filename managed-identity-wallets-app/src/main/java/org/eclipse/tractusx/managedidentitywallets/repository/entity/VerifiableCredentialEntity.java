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

import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Entity(name = VerifiableCredentialEntity.TABLE_NAME)
@Table(name = VerifiableCredentialEntity.TABLE_NAME)
@ToString
public class VerifiableCredentialEntity extends AbstractEntity {

    public static final String TABLE_NAME = "verifiable_credential";
    public static final String COLUMN_JSON = "raw";
    public static final String COLUMN_ID = "id";

    @Id
    @ToString.Include
    @Column(name = COLUMN_ID, nullable = false)
    private String id;

    @Column(name = COLUMN_JSON, nullable = false)
    private String json;

    @OneToMany(mappedBy = "id.verifiableCredential", cascade = CascadeType.REMOVE)
    private Set<VerifiableCredentialWalletIntersectionEntity> walletIntersections;

    @OneToMany(mappedBy = "id.verifiableCredential", cascade = CascadeType.REMOVE)
    private Set<VerifiableCredentialTypeIntersectionEntity> credentialTypeIntersections;

    @OneToMany(mappedBy = "id.verifiableCredential", cascade = CascadeType.REMOVE)
    private Set<VerifiableCredentialIssuerIntersectionEntity> credentialIssuerIntersections;

}

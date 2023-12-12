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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "issuer", callSuper = false)
@Entity(name= VerifiableCredentialIssuerEntity.TABLE_NAME)
@Table(name = VerifiableCredentialIssuerEntity.TABLE_NAME)
@ToString
public class VerifiableCredentialIssuerEntity extends AbstractEntity {

    public static final String TABLE_NAME = "verifiable_credential_issuer";
    public static final String COLUMN_ISSUER = "issuer";

    @Id
    @ToString.Include
    @Column(name = COLUMN_ISSUER, nullable = false)
    private String issuer;
}

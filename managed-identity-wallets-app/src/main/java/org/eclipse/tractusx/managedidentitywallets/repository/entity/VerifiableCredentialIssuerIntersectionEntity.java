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

import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Entity(name= VerifiableCredentialIssuerIntersectionEntity.TABLE_NAME)
@Table(name = VerifiableCredentialIssuerIntersectionEntity.TABLE_NAME)
public class VerifiableCredentialIssuerIntersectionEntity extends AbstractEntity {

    public static final String TABLE_NAME = "verifiable_credential_issuer_intersection";
    public static final String COLUMN_VERIFIABLE_CREDENTIAL_ID = "verifiable_credential_id";
    public static final String COLUMN_VERIFIABLE_CREDENTIAL_ISSUER_ID = "verifiable_credential_issuer_id";

    @EmbeddedId
    private VerifiableCredentialIssuerIntersectionEntityId id;

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(of = {"verifiableCredential", "verifiableCredentialIssuer"})
    @Embeddable
    public static class VerifiableCredentialIssuerIntersectionEntityId implements Serializable {

        @ManyToOne
        @JoinColumn(name = COLUMN_VERIFIABLE_CREDENTIAL_ID)
        private VerifiableCredentialEntity verifiableCredential;

        @ManyToOne
        @JoinColumn(name = COLUMN_VERIFIABLE_CREDENTIAL_ISSUER_ID)
        private VerifiableCredentialIssuerEntity verifiableCredentialIssuer;
    }
}

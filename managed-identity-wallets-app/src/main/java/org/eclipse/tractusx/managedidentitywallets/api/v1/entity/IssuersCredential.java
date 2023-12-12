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

package org.eclipse.tractusx.managedidentitywallets.api.v1.entity;

import lombok.*;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;


/**
 * The type Credential.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssuersCredential {

    private String holderDid;

    private String issuerDid;

    private String type;

    private VerifiableCredential data;

    private String credentialId;

    public static IssuersCredential of(HoldersCredential holdersCredential) {
        return IssuersCredential.builder()
                .credentialId(holdersCredential.getCredentialId())
                .data(holdersCredential.getData())
                .type(holdersCredential.getType())
                .issuerDid(holdersCredential.getIssuerDid())
                .holderDid(holdersCredential.getHolderDid())
                .build();
    }
}

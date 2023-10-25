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

package org.eclipse.tractusx.managedidentitywallets.repository.predicate;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.QVerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;

import java.util.Optional;


@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class VerifiableCredentialPredicate {

    public static Predicate fromQuery(@NonNull VerifiableCredentialQuery query) {
        final BooleanBuilder predicate = new BooleanBuilder(notNull());

        /* By Verifiable Credential Id */
        Optional.ofNullable(query.getVerifiableCredentialId())
                .map(VerifiableCredentialId::getText)
                .map(VerifiableCredentialPredicate::hasId)
                .ifPresent(predicate::and);

        /* By Verifiable Credential Type */
        Optional.ofNullable(query.getVerifiableCredentialType())
                .map(VerifiableCredentialType::getText)
                .map(VerifiableCredentialPredicate::hasType)
                .ifPresent(predicate::and);

        /* By Wallet Id */
        Optional.ofNullable(query.getHolderWalletId())
                .map(WalletId::getText)
                .map(VerifiableCredentialPredicate::hasWallet)
                .ifPresent(predicate::and);

        /* By Issuer Id */
        Optional.ofNullable(query.getVerifiableCredentialIssuer())
                .map(VerifiableCredentialIssuer::getText)
                .map(VerifiableCredentialPredicate::hasIssuer)
                .ifPresent(predicate::and);

        return predicate;
    }

    private static BooleanExpression notNull() {
        return QVerifiableCredentialEntity.verifiableCredentialEntity
                .id.isNotNull();
    }

    private static BooleanExpression hasId(String id) {
        return QVerifiableCredentialEntity.verifiableCredentialEntity
                .id.eq(id);
    }

    private static BooleanExpression hasType(String type) {
        return QVerifiableCredentialEntity.verifiableCredentialEntity
                .credentialTypeIntersections.any()
                .id.verifiableCredentialType
                .type.eq(type);
    }

    private static BooleanExpression hasWallet(String walletId) {
        return QVerifiableCredentialEntity.verifiableCredentialEntity
                .walletIntersections.any()
                .id.wallet
                .id.eq(walletId);
    }

    private static BooleanExpression hasIssuer(String walletId) {
        return QVerifiableCredentialEntity.verifiableCredentialEntity
                .credentialIssuerIntersections.any()
                .id.verifiableCredentialIssuer
                .issuer.eq(walletId);
    }
}

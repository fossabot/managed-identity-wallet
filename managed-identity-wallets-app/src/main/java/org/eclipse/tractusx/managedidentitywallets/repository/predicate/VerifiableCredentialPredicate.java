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
import org.eclipse.tractusx.managedidentitywallets.repository.entity.QVerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;

import java.util.Objects;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class VerifiableCredentialPredicate {

    public static Predicate fromQuery(VerifiableCredentialQuery query) {
        final BooleanBuilder predicate = new BooleanBuilder();

        final String id = query.getVerifiableCredentialId().getText();
        final String type = query.getVerifiableCredentialType().getText();
        final String walletId = query.getHolderWalletId().getText();
        final String issuer = query.getVerifiableCredentialIssuer().getText();

        if (Objects.nonNull(id)) {
            predicate.and(hasId(id));
        }

        if (Objects.nonNull(walletId)) {
            predicate.and(hasWallet(walletId));
        }

        if (Objects.nonNull(type)) {
            predicate.and(hasType(type));
        }

        if (Objects.nonNull(issuer)) {
            predicate.and(hasIssuer(issuer));
        }

        return predicate;
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

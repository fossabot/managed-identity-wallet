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

package org.eclipse.tractusx.managedidentitywallets.repository.database.predicate;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletWithVerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.QVerifiableCredentialWalletIntersectionEntity;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class WalletWithVerifiableCredentialPredicate {

    public static Predicate fromQuery(@NonNull WalletWithVerifiableCredentialQuery query) {

        final BooleanBuilder predicate = new BooleanBuilder(notNull());

        /* By Wallet Id */
        predicate.and(hasWalletId(query.getWalletId().getText()));

        /* By Verifiable Credential Id */
        predicate.and(hasVerifiableCredentialId(query.getVerifiableCredentialId().getText()));

        return predicate;
    }

    private static Predicate hasVerifiableCredentialId(String text) {
        return QVerifiableCredentialWalletIntersectionEntity.verifiableCredentialWalletIntersectionEntity
                .id.verifiableCredential.id.eq(text);
    }

    private static BooleanExpression hasWalletId(String id) {
        return QVerifiableCredentialWalletIntersectionEntity.verifiableCredentialWalletIntersectionEntity
                .id.wallet.id.eq(id);
    }

    private static BooleanExpression notNull() {
        return QVerifiableCredentialWalletIntersectionEntity.verifiableCredentialWalletIntersectionEntity
                .id.isNotNull();
    }
}

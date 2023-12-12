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
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.QWalletEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;

import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class WalletPredicate {

    public static Predicate fromQuery(@NonNull WalletQuery query) {

        final BooleanBuilder predicate = new BooleanBuilder(notNull());

        /* By Wallet Id */
        Optional.ofNullable(query.getWalletId())
                .map(WalletId::getText)
                .map(WalletPredicate::hasId)
                .ifPresent(predicate::and);

        /* By Wallet Name */
        Optional.ofNullable(query.getName())
                .map(WalletName::getText)
                .map(WalletPredicate::hasName)
                .ifPresent(predicate::and);

        return predicate;
    }

    private static BooleanExpression hasId(String id) {
        return QWalletEntity.walletEntity
                .id.eq(id);
    }

    private static BooleanExpression hasName(String name) {
        return QWalletEntity.walletEntity
                .name.eq(name);
    }

    private static BooleanExpression notNull() {
        return QWalletEntity.walletEntity
                .id.isNotNull();
    }
}

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

package org.eclipse.tractusx.managedidentitywallets.repository.repository;

import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
interface VerifiableCredentialJpaRepository
        extends PagingAndSortingRepository<VerifiableCredentialEntity, String>,
        CrudRepository<VerifiableCredentialEntity, String>,
        QuerydslPredicateExecutor<VerifiableCredentialEntity> {

    Optional<VerifiableCredentialEntity> findByIdAndWalletIntersections_Wallet_Id(String id, String walletId);

    Page<VerifiableCredentialEntity> findByCredentialIssuerIntersections_Issuer(String issuerId);
}

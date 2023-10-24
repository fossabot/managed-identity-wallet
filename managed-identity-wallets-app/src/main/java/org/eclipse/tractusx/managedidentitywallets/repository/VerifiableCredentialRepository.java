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

package org.eclipse.tractusx.managedidentitywallets.repository;

import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.exceptions.WalletDoesNotExistException;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.*;
import org.eclipse.tractusx.managedidentitywallets.repository.map.VerifiableCredentialEntityMap;
import org.eclipse.tractusx.managedidentitywallets.repository.predicate.WalletPredicate;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.managedidentitywallets.repository.predicate.VerifiableCredentialPredicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerifiableCredentialRepository {

    private final WalletJpaRepository walletJpaRepository;
    private final VerifiableCredentialJpaRepository verifiableCredentialJpaRepository;
    private final VerifiableCredentialIntersectionJpaRepository verifiableCredentialIntersectionJpaRepository;
    private final VerifiableCredentialIssuerIntersectionJpaRepository verifiableCredentialIssuerIntersectionJpaRepository;
    private final VerifiableCredentialTypeIntersectionJpaRepository verifiableCredentialTypeIntersectionJpaRepository;

    private final VerifiableCredentialEntityMap verifiableCredentialEntityMap;

    @Transactional
    public void save(@NonNull VerifiableCredential vc, @NonNull WalletId walletId) throws WalletDoesNotExistException {
        final WalletQuery query = WalletQuery.builder().walletId(walletId).build();
        final Predicate predicate = WalletPredicate.fromQuery(query);
        final WalletEntity walletEntity = walletJpaRepository.findOne(predicate)
                .orElseThrow(() -> new WalletDoesNotExistException(walletId));

        // Verifiable Credential
        final VerifiableCredentialEntity verifiableCredentialEntity = new VerifiableCredentialEntity();
        verifiableCredentialEntity.setId(UUID.randomUUID().toString());
        verifiableCredentialEntity.setJson(vc.toJson());
        if (!verifiableCredentialJpaRepository.existsById(verifiableCredentialEntity.getId()))
            verifiableCredentialJpaRepository.save(verifiableCredentialEntity);

        // Verifiable Credential - Wallet Intersection
        final VerifiableCredentialIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = new VerifiableCredentialIntersectionEntity.VerifiableCredentialIntersectionEntityId();
        verifiableCredentialIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIntersectionEntityId.setWallet(walletEntity);
        final VerifiableCredentialIntersectionEntity verifiableCredentialIntersectionEntity = new VerifiableCredentialIntersectionEntity();
        verifiableCredentialIntersectionEntity.setId(verifiableCredentialIntersectionEntityId);
        if (!verifiableCredentialIntersectionJpaRepository.existsById(verifiableCredentialIntersectionEntityId))
            verifiableCredentialIntersectionJpaRepository.save(verifiableCredentialIntersectionEntity);

        // Verifiable Credential - Issuer Intersection
        final VerifiableCredentialIssuerEntity verifiableCredentialIssuerEntity = new VerifiableCredentialIssuerEntity();
        verifiableCredentialIssuerEntity.setIssuer(vc.getIssuer().toString());
        final VerifiableCredentialIssuerIntersectionEntity.VerifiableCredentialIssuerIntersectionEntityId verifiableCredentialIssuerIntersectionEntityId = new VerifiableCredentialIssuerIntersectionEntity.VerifiableCredentialIssuerIntersectionEntityId();
        verifiableCredentialIssuerIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIssuerIntersectionEntityId.setVerifiableCredentialIssuer(verifiableCredentialIssuerEntity);
        final VerifiableCredentialIssuerIntersectionEntity verifiableCredentialIssuerIntersectionEntity = new VerifiableCredentialIssuerIntersectionEntity();
        verifiableCredentialIssuerIntersectionEntity.setId(verifiableCredentialIssuerIntersectionEntityId);
        if (!verifiableCredentialIssuerIntersectionJpaRepository.existsById(verifiableCredentialIssuerIntersectionEntityId))
            verifiableCredentialIssuerIntersectionJpaRepository.save(verifiableCredentialIssuerIntersectionEntity);

        // Verifiable Credential - Type Intersection
        for (final String type : vc.getTypes()) {
            final VerifiableCredentialTypeEntity verifiableCredentialType = new VerifiableCredentialTypeEntity();
            verifiableCredentialType.setType(type);
            final VerifiableCredentialTypeIntersectionEntity.VerifiableCredentialTypeIntersectionEntityId verifiableCredentialTypeIntersectionEntityId = new VerifiableCredentialTypeIntersectionEntity.VerifiableCredentialTypeIntersectionEntityId();
            verifiableCredentialTypeIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
            verifiableCredentialTypeIntersectionEntityId.setVerifiableCredentialType(verifiableCredentialType);
            final VerifiableCredentialTypeIntersectionEntity verifiableCredentialTypeIntersectionEntity = new VerifiableCredentialTypeIntersectionEntity();
            verifiableCredentialTypeIntersectionEntity.setId(verifiableCredentialTypeIntersectionEntityId);
            if (!verifiableCredentialTypeIntersectionJpaRepository.existsById(verifiableCredentialTypeIntersectionEntityId))
                verifiableCredentialTypeIntersectionJpaRepository.save(verifiableCredentialTypeIntersectionEntity);
        }
    }

    public List<VerifiableCredential> findAll(@NonNull VerifiableCredentialQuery query) {
        final List<VerifiableCredential> credentials = new ArrayList<>();
        final Predicate predicate = VerifiableCredentialPredicate.fromQuery(query);
        verifiableCredentialJpaRepository.findAll(predicate)
                .iterator().forEachRemaining(
                        c -> credentials.add(verifiableCredentialEntityMap.map(c)));
        return credentials;
    }

    public Page<VerifiableCredential> findAll(@NonNull VerifiableCredentialQuery query, @NonNull Pageable p) {
        final Predicate predicate = VerifiableCredentialPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("findAll: predicate={}", predicate);
        }
        return verifiableCredentialJpaRepository.findAll(predicate, p)
                .map(verifiableCredentialEntityMap::map);
    }
}

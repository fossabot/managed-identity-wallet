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
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialDoesNotExistException;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletDoesNotExistException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
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

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerifiableCredentialRepository {

    private final WalletJpaRepository walletJpaRepository;
    private final VerifiableCredentialJpaRepository verifiableCredentialJpaRepository;
    private final VerifiableCredentialWalletIntersectionJpaRepository verifiableCredentialWalletIntersectionJpaRepository;
    private final VerifiableCredentialIssuerIntersectionJpaRepository verifiableCredentialIssuerIntersectionJpaRepository;
    private final VerifiableCredentialTypeIntersectionJpaRepository verifiableCredentialTypeIntersectionJpaRepository;
    private final VerifiableCredentialTypeJpaRepository verifiableCredentialTypeJpaRepository;
    private final VerifiableCredentialIssuerJpaRepository verifiableCredentialIssuerJpaRepository;

    private final VerifiableCredentialEntityMap verifiableCredentialEntityMap;

    @Transactional
    public void createWalletIntersection(@NonNull VerifiableCredentialId verifiableCredentialId, @NonNull WalletId walletId)
            throws WalletDoesNotExistException, VerifiableCredentialDoesNotExistException {
        if (log.isTraceEnabled()) {
            log.trace("createWalletIntersection: wallet={}, credential={}", walletId, verifiableCredentialId);
        }

        final WalletQuery walletQuery = WalletQuery.builder().walletId(walletId).build();
        final Predicate predicate = WalletPredicate.fromQuery(walletQuery);
        final WalletEntity walletEntity = walletJpaRepository.findOne(predicate)
                .orElseThrow(() -> new WalletDoesNotExistException(walletId));

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(verifiableCredentialId)
                .build();
        final Predicate vcPredicate = VerifiableCredentialPredicate.fromQuery(verifiableCredentialQuery);
        final VerifiableCredentialEntity verifiableCredentialEntity = verifiableCredentialJpaRepository.findOne(vcPredicate)
                .orElseThrow(() -> new VerifiableCredentialDoesNotExistException(verifiableCredentialId));

        // Verifiable Credential - Wallet Intersection
        final VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = new VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId();
        verifiableCredentialIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIntersectionEntityId.setWallet(walletEntity);
        final VerifiableCredentialWalletIntersectionEntity verifiableCredentialWalletIntersectionEntity = new VerifiableCredentialWalletIntersectionEntity();
        verifiableCredentialWalletIntersectionEntity.setId(verifiableCredentialIntersectionEntityId);
        if (!verifiableCredentialWalletIntersectionJpaRepository.existsById(verifiableCredentialIntersectionEntityId))
            verifiableCredentialWalletIntersectionJpaRepository.save(verifiableCredentialWalletIntersectionEntity);
    }

    @Transactional
    public void create(@NonNull VerifiableCredential vc) throws VerifiableCredentialAlreadyExistsException {
        if (log.isTraceEnabled()) {
            log.trace("create: credential={}", vc);
        }

        // Verifiable Credential
        final VerifiableCredentialEntity verifiableCredentialEntity = new VerifiableCredentialEntity();
        verifiableCredentialEntity.setId(vc.getId().toString());
        verifiableCredentialEntity.setJson(vc.toJson());
        if (!verifiableCredentialJpaRepository.existsById(verifiableCredentialEntity.getId())) {
            verifiableCredentialJpaRepository.save(verifiableCredentialEntity);
        } else {
            throw new VerifiableCredentialAlreadyExistsException(new VerifiableCredentialId(vc.getId().toString()));
        }

        // Verifiable Credential - Issuer
        final VerifiableCredentialIssuerEntity verifiableCredentialIssuerEntity = new VerifiableCredentialIssuerEntity();
        verifiableCredentialIssuerEntity.setIssuer(vc.getIssuer().toString());
        if (!verifiableCredentialIssuerJpaRepository.existsById(verifiableCredentialIssuerEntity.getIssuer()))
            verifiableCredentialIssuerJpaRepository.save(verifiableCredentialIssuerEntity);

        // Verifiable Credential - Issuer Intersection
        final VerifiableCredentialIssuerIntersectionEntity.VerifiableCredentialIssuerIntersectionEntityId verifiableCredentialIssuerIntersectionEntityId = new VerifiableCredentialIssuerIntersectionEntity.VerifiableCredentialIssuerIntersectionEntityId();
        verifiableCredentialIssuerIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIssuerIntersectionEntityId.setVerifiableCredentialIssuer(verifiableCredentialIssuerEntity);
        final VerifiableCredentialIssuerIntersectionEntity verifiableCredentialIssuerIntersectionEntity = new VerifiableCredentialIssuerIntersectionEntity();
        verifiableCredentialIssuerIntersectionEntity.setId(verifiableCredentialIssuerIntersectionEntityId);
        if (!verifiableCredentialIssuerIntersectionJpaRepository.existsById(verifiableCredentialIssuerIntersectionEntityId))
            verifiableCredentialIssuerIntersectionJpaRepository.save(verifiableCredentialIssuerIntersectionEntity);

        for (final String type : vc.getTypes()) {
            // Verifiable Credential - Type
            final VerifiableCredentialTypeEntity verifiableCredentialType = new VerifiableCredentialTypeEntity();
            verifiableCredentialType.setType(type);
            if (!verifiableCredentialTypeJpaRepository.existsById(verifiableCredentialType.getType()))
                verifiableCredentialTypeJpaRepository.save(verifiableCredentialType);
            // Verifiable Credential - Type Intersection
            final VerifiableCredentialTypeIntersectionEntity.VerifiableCredentialTypeIntersectionEntityId verifiableCredentialTypeIntersectionEntityId = new VerifiableCredentialTypeIntersectionEntity.VerifiableCredentialTypeIntersectionEntityId();
            verifiableCredentialTypeIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
            verifiableCredentialTypeIntersectionEntityId.setVerifiableCredentialType(verifiableCredentialType);
            final VerifiableCredentialTypeIntersectionEntity verifiableCredentialTypeIntersectionEntity = new VerifiableCredentialTypeIntersectionEntity();
            verifiableCredentialTypeIntersectionEntity.setId(verifiableCredentialTypeIntersectionEntityId);
            if (!verifiableCredentialTypeIntersectionJpaRepository.existsById(verifiableCredentialTypeIntersectionEntityId))
                verifiableCredentialTypeIntersectionJpaRepository.save(verifiableCredentialTypeIntersectionEntity);
        }
    }

    @Transactional
    public void deleteAll() {
        if (log.isTraceEnabled()) {
            log.trace("delete all");
        }
        verifiableCredentialTypeIntersectionJpaRepository.deleteAll();
        verifiableCredentialTypeJpaRepository.deleteAll();

        verifiableCredentialIssuerIntersectionJpaRepository.deleteAll();
        verifiableCredentialIssuerJpaRepository.deleteAll();

        verifiableCredentialWalletIntersectionJpaRepository.deleteAll();

        verifiableCredentialJpaRepository.deleteAll();
    }

    @Transactional
    public void deleteWalletIntersection(@NonNull VerifiableCredentialId verifiableCredentialId, @NonNull WalletId walletId) throws WalletDoesNotExistException, VerifiableCredentialDoesNotExistException {
        if (log.isTraceEnabled()) {
            log.trace("deleteWalletIntersection: walletId={}, credentialId={}", walletId, verifiableCredentialId);
        }

        final WalletQuery walletQuery = WalletQuery.builder().walletId(walletId).build();
        final Predicate predicate = WalletPredicate.fromQuery(walletQuery);
        final WalletEntity walletEntity = walletJpaRepository.findOne(predicate)
                .orElseThrow(() -> new WalletDoesNotExistException(walletId));

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(verifiableCredentialId)
                .build();
        final Predicate vcPredicate = VerifiableCredentialPredicate.fromQuery(verifiableCredentialQuery);
        final VerifiableCredentialEntity verifiableCredentialEntity = verifiableCredentialJpaRepository.findOne(vcPredicate)
                .orElseThrow(() -> new VerifiableCredentialDoesNotExistException(verifiableCredentialId));

        // Verifiable Credential - Wallet Intersection
        final VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = new VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId();
        verifiableCredentialIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIntersectionEntityId.setWallet(walletEntity);
        final VerifiableCredentialWalletIntersectionEntity verifiableCredentialWalletIntersectionEntity = new VerifiableCredentialWalletIntersectionEntity();
        verifiableCredentialWalletIntersectionEntity.setId(verifiableCredentialIntersectionEntityId);
        if (!verifiableCredentialWalletIntersectionJpaRepository.existsById(verifiableCredentialIntersectionEntityId))
            verifiableCredentialWalletIntersectionJpaRepository.delete(verifiableCredentialWalletIntersectionEntity);
    }

    @Transactional
    public void delete(@NonNull VerifiableCredential vc) {
        if (log.isTraceEnabled()) {
            log.trace("delete: credential={}", vc);
        }

        // Verifiable Credential
        final VerifiableCredentialQuery credentialQuery = VerifiableCredentialQuery.builder().
                verifiableCredentialId(new VerifiableCredentialId(vc.getId().toString()))
                .build();
        final Predicate vcPredicate = VerifiableCredentialPredicate.fromQuery(credentialQuery);
        final Optional<VerifiableCredentialEntity> verifiableCredentialEntity = verifiableCredentialJpaRepository.findOne(vcPredicate);

        // if it does not exist there is nothing to delete
        verifiableCredentialEntity.ifPresent(verifiableCredentialJpaRepository::delete);
    }

    public long count() {
        return count(VerifiableCredentialQuery.builder().build());
    }

    public long count(@NonNull VerifiableCredentialQuery query) {
        final Predicate predicate = VerifiableCredentialPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("count: predicate={}", predicate);
        }
        return verifiableCredentialJpaRepository.count(predicate);
    }

    public Page<VerifiableCredential> findAll(@NonNull VerifiableCredentialQuery query, @NonNull Pageable p) {
        final Predicate predicate = VerifiableCredentialPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("findAll: predicate={}", predicate);
        }
        return verifiableCredentialJpaRepository.findAll(predicate, p)
                .map(verifiableCredentialEntityMap::map);
    }

    public Optional<VerifiableCredential> findOne(@NonNull VerifiableCredentialQuery query) {
        final Predicate predicate = VerifiableCredentialPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("findOne: predicate={}", predicate);
        }
        return verifiableCredentialJpaRepository.findOne(predicate)
                .map(verifiableCredentialEntityMap::map);
    }

}

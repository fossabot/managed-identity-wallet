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

package org.eclipse.tractusx.managedidentitywallets.repository.database;

import com.querydsl.core.types.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialAlreadyStoredInWalletException;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.PersistedEd25519VerificationMethod;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.database.predicate.WalletWithVerifiableCredentialPredicate;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletWithVerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.EncryptionKeyEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.EncryptionKeyEntityType;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialWalletIntersectionEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.map.WalletMap;
import org.eclipse.tractusx.managedidentitywallets.repository.database.predicate.WalletPredicate;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletRepository {

    private final WalletJpaRepository walletJpaRepository;
    private final VerifiableCredentialJpaRepository verifiableCredentialJpaRepository;
    private final VerifiableCredentialWalletIntersectionJpaRepository verifiableCredentialWalletIntersectionJpaRepository;
    private final EncryptionKeyJpaRepository encryptionKeyJpaRepository;
    private final WalletMap walletMap;

    @Transactional
    public void create(@NonNull final Wallet wallet) {

        /* Create New Wallet */
        final String walletId = wallet.getWalletId().getText();
        final String walletName = wallet.getWalletName().getText();

        final WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(walletId);
        walletEntity.setName(walletName);

        final List<EncryptionKeyEntity> ed25519KeyEntities = wallet.getStoredEd25519Keys().stream()
                .map(k -> {
                    final EncryptionKeyEntity keyEntity = new EncryptionKeyEntity();
                    keyEntity.setId(UUID.randomUUID().toString());
                    keyEntity.setWallet(walletEntity);
                    keyEntity.setDidFragment(k.getDidFragment().getText());
                    keyEntity.setPublicKeyCypherTextBase64(k.getPublicKey().getBase64());
                    keyEntity.setPrivateKeyCypherTextBase64(k.getPrivateKey().getBase64());
                    return keyEntity;
                }).collect(Collectors.toList());
        walletEntity.setEncryptionKeys(ed25519KeyEntities);

        /* Assert Wallet Does Not Exist */
        if (walletJpaRepository.existsById(walletId)) {
            throw new WalletAlreadyExistsException(wallet.getWalletId());
        }

        /* Write to DB */
        if (log.isTraceEnabled()) {
            log.trace("create: wallet={}", wallet);
        }

        walletJpaRepository.save(walletEntity);
        encryptionKeyJpaRepository.saveAll(ed25519KeyEntities);
    }

    @Transactional
    public void update(@NonNull final Wallet wallet) {

        /* Assert Wallet Exists*/
        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(wallet.getWalletId())
                .build();
        final Predicate predicate = WalletPredicate.fromQuery(walletQuery);
        final WalletEntity walletEntity = walletJpaRepository.findOne(predicate)
                .orElseThrow(() -> new WalletNotFoundException(wallet.getWalletId()));

        /* Update Wallet */
        final String newWalletName = wallet.getWalletName().getText();
        walletEntity.setName(newWalletName);

        final List<EncryptionKeyEntity> ed25519KeyEntities = new ArrayList<>();
        for (final PersistedEd25519VerificationMethod storedEd25519Key : wallet.getStoredEd25519Keys()) {
            // keep keys that are already in db or generate new ones
            // it should not be possible to update the key itself
            walletEntity.getEncryptionKeys().stream().filter(
                            k -> k.getId().equals(storedEd25519Key.getId().getText())
                    ).findFirst()
                    .ifPresentOrElse(ed25519KeyEntities::add, () -> {
                        final EncryptionKeyEntity keyEntity = new EncryptionKeyEntity();
                        keyEntity.setKeyType(EncryptionKeyEntityType.ED25519);
                        keyEntity.setId(storedEd25519Key.getId().getText());
                        keyEntity.setWallet(walletEntity);
                        keyEntity.setDidFragment(storedEd25519Key.getDidFragment().getText());
                        keyEntity.setCreatedAt(storedEd25519Key.getCreatedAt());
                        keyEntity.setPublicKeyCypherTextBase64(storedEd25519Key.getPublicKey().getBase64());
                        keyEntity.setPrivateKeyCypherTextBase64(storedEd25519Key.getPrivateKey().getBase64());
                        ed25519KeyEntities.add(keyEntity);
                    });
        }

        walletEntity.getEncryptionKeys().clear();
        walletEntity.getEncryptionKeys().addAll(ed25519KeyEntities);

        /* Write to DB */
        if (log.isTraceEnabled()) {
            log.trace("update: wallet={}", wallet);
        }

        encryptionKeyJpaRepository.saveAll(ed25519KeyEntities);
        walletJpaRepository.save(walletEntity);
    }

    @Transactional
    public void deleteAll() {
        if (log.isTraceEnabled()) {
            log.trace("delete all");
        }
        verifiableCredentialWalletIntersectionJpaRepository.deleteAll();
        encryptionKeyJpaRepository.deleteAll();
        walletJpaRepository.deleteAll();
    }

    public void delete(@NonNull final WalletId walletId) {
        if (log.isTraceEnabled()) {
            log.trace("delete: wallet={}", walletId);
        }
        walletJpaRepository.deleteById(walletId.getText());
    }

    public void deleteAll(@NonNull final WalletQuery query) {
        final Predicate predicate = WalletPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("deleteAll: predicate={}", predicate);
        }
        final Iterable<WalletEntity> wallets = walletJpaRepository.findAll(predicate);
        walletJpaRepository.deleteAll(wallets);
    }

    public long count() {
        return count(WalletQuery.builder().build());
    }

    public long count(@NonNull final WalletQuery query) {
        final Predicate predicate = WalletPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("count: predicate={}", predicate);
        }
        return walletJpaRepository.count(predicate);
    }

    public boolean exists(@NonNull final WalletQuery query) {
        final Predicate predicate = WalletPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("exists: predicate={}", predicate);
        }
        return walletJpaRepository.findOne(predicate).isPresent();

    }

    public boolean existsById(@NonNull final WalletId walletId) {
        if (log.isTraceEnabled()) {
            log.trace("existsById: walletId={}", walletId);
        }
        return walletJpaRepository.findById(walletId.getText()).isPresent();
    }

    public Optional<Wallet> findById(@NonNull final WalletId walletId) {
        final WalletQuery query = WalletQuery.builder().walletId(walletId).build();
        return findOne(query);
    }

    public Optional<Wallet> findOne(@NonNull final WalletQuery query) {
        final Predicate predicate = WalletPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("findOne: predicate={}", predicate);
        }
        return walletJpaRepository.findOne(predicate)
                .map(walletMap::map);

    }

    public Page<Wallet> findAll() {
        return findAll(WalletQuery.builder().build(), Pageable.unpaged());
    }

    public Page<Wallet> findAll(@NonNull final WalletQuery query, @NonNull final Pageable pageable) {
        final Predicate predicate = WalletPredicate.fromQuery(query);
        if (log.isTraceEnabled()) {
            log.trace("findAll: predicate={}", predicate);
        }
        return walletJpaRepository.findAll(predicate, pageable)
                .map(walletMap::map);
    }

    public void storeVerifiableCredentialInWallet(@NonNull final Wallet wallet, @NonNull final VerifiableCredential verifiableCredential) {
        final VerifiableCredentialWalletIntersectionEntity verifiableCredentialWalletIntersectionEntity = createVerifiableCredentialWalletIntersectionEntity(wallet, verifiableCredential);
        final VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = verifiableCredentialWalletIntersectionEntity.getId();

        // if it's not already stored put it into the wallet
        if (!verifiableCredentialWalletIntersectionJpaRepository.existsById(verifiableCredentialIntersectionEntityId)) {
            if (log.isTraceEnabled()) {
                log.trace("storeVerifiableCredentialInWallet: wallet={}, verifiableCredential={}", wallet, verifiableCredential);
            }

            if (!walletJpaRepository.existsById(wallet.getWalletId().getText())) {
                throw new WalletNotFoundException(wallet.getWalletId());
            }
            if (!verifiableCredentialJpaRepository.existsById(verifiableCredential.getId().toString())) {
                throw new VerifiableCredentialNotFoundException(new VerifiableCredentialId(verifiableCredential.getId().toString()));
            }

            verifiableCredentialWalletIntersectionJpaRepository.save(verifiableCredentialWalletIntersectionEntity);
        } else {
            throw new VerifiableCredentialAlreadyStoredInWalletException(wallet, verifiableCredential);
        }
    }

    public void removeVerifiableCredentialFromWallet(@NonNull final Wallet wallet, @NonNull final VerifiableCredential verifiableCredential) {
        final VerifiableCredentialWalletIntersectionEntity verifiableCredentialWalletIntersectionEntity = createVerifiableCredentialWalletIntersectionEntity(wallet, verifiableCredential);
        final VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = verifiableCredentialWalletIntersectionEntity.getId();

        // if it's stored remove it
        if (verifiableCredentialWalletIntersectionJpaRepository.existsById(verifiableCredentialIntersectionEntityId)) {
            if (log.isTraceEnabled()) {
                log.trace("removeVerifiableCredentialFromWallet: wallet={}, verifiableCredential={}", wallet, verifiableCredential);
            }
            verifiableCredentialWalletIntersectionJpaRepository.delete(verifiableCredentialWalletIntersectionEntity);
        }
    }

    private VerifiableCredentialWalletIntersectionEntity createVerifiableCredentialWalletIntersectionEntity(@NonNull final Wallet wallet, @NonNull final VerifiableCredential verifiableCredential) {
        final VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId id = new VerifiableCredentialWalletIntersectionEntity.VerifiableCredentialIntersectionEntityId();
        final VerifiableCredentialWalletIntersectionEntity entity = new VerifiableCredentialWalletIntersectionEntity();

        final VerifiableCredentialEntity verifiableCredentialEntity = new VerifiableCredentialEntity();
        verifiableCredentialEntity.setId(verifiableCredential.getId().toString());
        final WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(wallet.getWalletId().getText());

        id.setVerifiableCredential(verifiableCredentialEntity);
        id.setWallet(walletEntity);
        entity.setId(id);

        return entity;
    }

    public boolean exists(@NonNull final WalletWithVerifiableCredentialQuery credentialQuery) {
        final Predicate predicate = WalletWithVerifiableCredentialPredicate.fromQuery(credentialQuery);
        if (log.isTraceEnabled()) {
            log.trace("exists: predicate={}", predicate);
        }
        return verifiableCredentialWalletIntersectionJpaRepository.exists(predicate);
    }
}

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
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.exceptions.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.exceptions.WalletDoesNotExistException;
import org.eclipse.tractusx.managedidentitywallets.models.Ed25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.Ed25519KeyEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.map.WalletMap;
import org.eclipse.tractusx.managedidentitywallets.repository.predicate.WalletPredicate;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletRepository {

    private final WalletJpaRepository walletJpaRepository;
    private final Ed25519KeyJpaRepository ed25519KeyJpaRepository;
    private final WalletMap walletMap;

    @Transactional
    public void create(Wallet wallet) throws WalletAlreadyExistsException {

        /* Create New Wallet */
        final String walletId = wallet.getWalletId().getText();
        final String walletName = wallet.getWalletName().getText();
        final String walletDescription = wallet.getWalletDescription().getText();

        final WalletEntity walletEntity = new WalletEntity();
        walletEntity.setId(walletId);
        walletEntity.setName(walletName);
        walletEntity.setDescription(walletDescription);

        final List<Ed25519KeyEntity> ed25519KeyEntities = wallet.getEd25519Keys().stream()
                .map(k -> {
                    final Ed25519KeyEntity keyEntity = new Ed25519KeyEntity();
                    keyEntity.setVaultSecret(k.getVaultSecret());
                    keyEntity.setDescription(k.getDescription());
                    keyEntity.setWallet(walletEntity);
                    keyEntity.setDidIdentifier(k.getDidIdentifier());
                    return keyEntity;
                }).collect(Collectors.toList());
        walletEntity.setEd25519Keys(ed25519KeyEntities);

        /* Assert Wallet Does Not Exist */
        if (walletJpaRepository.existsById(walletId)) {
            throw new WalletAlreadyExistsException(wallet.getWalletId());
        }

        /* Write to DB */
        walletJpaRepository.save(walletEntity);
        ed25519KeyJpaRepository.saveAll(ed25519KeyEntities);
    }


    @Transactional
    public void update(Wallet wallet) throws WalletDoesNotExistException {

        /* Assert Wallet Exists*/
        WalletQuery walletQuery = WalletQuery.builder()
                .walletId(wallet.getWalletId())
                .build();
        final Predicate predicate = WalletPredicate.fromQuery(walletQuery);
        WalletEntity walletEntity = walletJpaRepository.findOne(predicate)
                .orElseThrow(() -> new WalletDoesNotExistException(wallet.getWalletId()));

        /* Update Wallet */
        final String walletName = wallet.getWalletName().getText();
        final String walletDescription = wallet.getWalletDescription().getText();

        walletEntity.setName(walletName);
        walletEntity.setDescription(walletDescription);

        final List<Ed25519KeyEntity> ed25519KeyEntities = wallet.getEd25519Keys().stream()
                .map(k -> {
                    final Ed25519KeyEntity keyEntity = new Ed25519KeyEntity();
                    keyEntity.setVaultSecret(k.getVaultSecret());
                    keyEntity.setDescription(k.getDescription());
                    keyEntity.setWallet(walletEntity);
                    keyEntity.setDidIdentifier(k.getDidIdentifier());
                    return keyEntity;
                }).collect(Collectors.toList());

        walletEntity.getEd25519Keys().clear();
        walletEntity.getEd25519Keys().addAll(ed25519KeyEntities);

        /* Write to DB */
        walletJpaRepository.save(walletEntity);
        ed25519KeyJpaRepository.saveAll(ed25519KeyEntities);
    }

    public void delete(WalletId walletId) {
        walletJpaRepository.deleteById(walletId.getText());
    }

    public Optional<Wallet> find(WalletQuery query) {
        final Predicate predicate = WalletPredicate.fromQuery(query);
        return walletJpaRepository.findOne(predicate)
                .map(walletMap::map);
    }
}

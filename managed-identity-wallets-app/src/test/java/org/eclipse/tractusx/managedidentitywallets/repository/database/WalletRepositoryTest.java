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

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class WalletRepositoryTest extends MiwTestCase {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @SneakyThrows
    public void testWalletCreation() {
        final Wallet wallet = newWalletPersisted();
        final WalletId walletId = wallet.getWalletId();

        final WalletQuery query = WalletQuery.builder()
                .walletId(walletId)
                .build();

        final Optional<Wallet> result = walletRepository.findOne(query);

        Assertions.assertTrue(result.isPresent(), "Wallet not found");
    }

    @Test
    @SneakyThrows
    public void testWalletUpdate() {

        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");

        final Wallet.WalletBuilder walletBuilder = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of());

        walletRepository.create(walletBuilder.build());

        final WalletName newWalletName = new WalletName("bar2");
        walletBuilder.walletName(newWalletName);

        walletRepository.update(walletBuilder.build());

        final WalletQuery query = WalletQuery.builder()
                .walletId(walletId)
                .build();

        final Optional<Wallet> result = walletRepository.findOne(query);

        Assertions.assertTrue(result.isPresent(), "Wallet not found");
        Assertions.assertEquals(newWalletName, result.get().getWalletName(), "Wallet name not updated");
    }

    @Test
    @SneakyThrows
    public void testWalletDeletion() {

        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");

        final Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of())
                .build();

        walletRepository.create(wallet);
        walletRepository.delete(walletId);

        final WalletQuery query = WalletQuery.builder()
                .walletId(walletId)
                .build();

        final Optional<Wallet> result = walletRepository.findOne(query);
        Assertions.assertTrue(result.isEmpty(), "Wallet found, but should have been deleted");
    }

    @Test
    public void testFindById() {
        final Wallet wallet = newWalletPersisted();
        newWalletPersisted();
        newWalletPersisted();

        final WalletQuery query = WalletQuery.builder()
                .walletId(wallet.getWalletId())
                .build();
        final Page<Wallet> result = walletRepository.findAll(query, Pageable.unpaged());
        Assertions.assertEquals(1, result.getTotalElements(), "Must be only one wallet");
    }

    @Test
    public void testFindByName() {
        final String name = "foo";
        newWalletPersisted(null, name);
        newWalletPersisted(null, name);
        newWalletPersisted();
        newWalletPersisted();

        final WalletQuery query = WalletQuery.builder()
                .name(new WalletName(name))
                .build();
        final Page<Wallet> result = walletRepository.findAll(query, Pageable.ofSize(1));
        Assertions.assertEquals(2, result.getTotalElements(), "Must be two wallets");
        Assertions.assertEquals(2, result.getTotalPages(), "Must be two pages");
    }
}

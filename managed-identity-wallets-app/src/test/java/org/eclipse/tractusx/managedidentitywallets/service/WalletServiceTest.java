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

package org.eclipse.tractusx.managedidentitywallets.service;

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.WalletEventTracker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.Optional;

public class WalletServiceTest extends MiwTestCase {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletEventTracker walletEventTracker;

    @Test
    @SneakyThrows
    public void testWalletCreation() {
        final Wallet w1 = newWallet("1", "1");
        final Wallet w2 = newWallet("2", "2");
        final Wallet w3 = newWallet("3", "3");

        walletService.create(w1);
        walletService.create(w2);
        walletService.create(w3);

        try {
            walletService.create(w1);
        } catch (WalletAlreadyExistsException e) {
            // ignore
        }

        Assertions.assertEquals(3, walletEventTracker.getWalletCreatingEvents().size(), "4 WalletCreatingEvents should have been fired");
        Assertions.assertEquals(3, walletEventTracker.getWalletCreatedEvents().size(), "3 WalletCreatedEvents should have been fired");
        Assertions.assertEquals(4, walletRepository.count(), "3 new plus authority wallet (=4) Wallets should be in the database");
    }

    @Test
    @SneakyThrows
    public void testWalletUpdate() {
        final Wallet originalWallet = newWalletPersisted();

        final WalletId originalId = originalWallet.getWalletId();
        final WalletName newName = new WalletName("updatedName");
        final Wallet modifiedWallet = Wallet.builder()
                .walletId(originalId)
                .walletName(newName)
                .build();

        walletEventTracker.clear();
        walletService.update(modifiedWallet);

        final Wallet updatedWallet = walletService.findById(originalId).orElseThrow();

        Assertions.assertEquals(1, walletEventTracker.getWalletUpdatingEvents().size(), "1 WalletUpdatingEvent should have been fired");
        Assertions.assertEquals(1, walletEventTracker.getWalletUpdatedEvents().size(), "1 walletUpdatedEvents should have been fired");
        Assertions.assertEquals(newName, updatedWallet.getWalletName(), "WalletName should have been updated");
    }

    @Test
    @SneakyThrows
    public void testWalletDeletion() {

        final Wallet w1 = newWalletPersisted();
        final Wallet w2 = newWalletPersisted();
        final Wallet w3 = newWalletPersisted();

        walletService.delete(w1);
        walletService.delete(w2);
        walletService.delete(w3);

        /*
         * From a state perspective it was never in the database,
         *  the desired state is achieved and the event is published.
         */
        walletService.delete(w1);

        Assertions.assertEquals(4, walletEventTracker.getWalletDeletingEvents().size(), "4 WalletDeletingEvent should have been fired");
        Assertions.assertEquals(4, walletEventTracker.getWalletDeletedEvents().size(), "4 WalletDeletedEvent should have been fired");
        Assertions.assertEquals(1, walletRepository.count(), "Only the authority wallet should be in the database");
    }

    @Test
    @SneakyThrows
    public void testWalletFindById() {
        newWalletPersisted("1", "name");
        newWalletPersisted("2", "name");
        newWalletPersisted("3", "name");

        final Optional<Wallet> wallet = walletService.findById(new WalletId("1"));

        Assertions.assertTrue(wallet.isPresent(), "Wallet should be present");
    }

    @Test
    @SneakyThrows
    public void testWalletFindByName() {
        newWalletPersisted("1", "name");
        newWalletPersisted("2", "name");
        newWalletPersisted("3", "name");

        final WalletQuery walletQuery = WalletQuery.builder()
                .name(new WalletName("name"))
                .build();
        final Page<Wallet> wallets = walletService.findAll(walletQuery, 0, 10);

        Assertions.assertEquals(3, wallets.getTotalElements(), "3 Wallets should be found");
    }
}

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

import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.event.*;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.factory.MiwIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class}, classes = WalletServiceTest.WalletEventTrackerConfiguration.class)
public class WalletServiceTest extends MiwIntegrationTest {


    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletEventTracker walletEventTracker;

    @BeforeEach
    public void setup() {
        walletEventTracker.clear();
    }

    @Test
    @SneakyThrows
    public void testWalletCreation() {
        final Wallet w1 = newWalletObject("1", "1");
        final Wallet w2 = newWalletObject("2", "2");
        final Wallet w3 = newWalletObject("3", "3");

        walletService.create(w1);
        walletService.create(w2);
        walletService.create(w3);

        try {
            walletService.create(w1);
        } catch (WalletAlreadyExistsException e) {
            // ignore
        }

        Assertions.assertEquals(4, walletEventTracker.walletCreatingEvents.size(), "4 WalletCreatingEvents should have been fired");
        Assertions.assertEquals(3, walletEventTracker.walletCreatedEvents.size(), "3 WalletCreatedEvents should have been fired");
        Assertions.assertEquals(3, walletRepository.count(), "3 Wallets should be in the database");
    }

    @Test
    @SneakyThrows
    public void testWalletUpdate() {
        final Wallet originalWallet = createRandomWallet();

        final WalletId originalId = originalWallet.getWalletId();
        final WalletName newName = new WalletName("updatedName");
        final Wallet modifiedWallet = Wallet.builder()
                .walletId(originalId)
                .walletName(newName)
                .build();

        walletService.update(modifiedWallet);

        final Wallet updatedWallet = walletService.findById(originalId).orElseThrow();

        Assertions.assertEquals(1, walletEventTracker.walletUpdatingEvents.size(), "1 WalletUpdatingEvent should have been fired");
        Assertions.assertEquals(1, walletEventTracker.walletUpdatedEvents.size(), "1 walletUpdatedEvents should have been fired");
        Assertions.assertEquals(newName, updatedWallet.getWalletName(), "WalletName should have been updated");
    }

    @Test
    @SneakyThrows
    public void testWalletDeletion() {
        final Wallet w1 = createRandomWallet();
        final Wallet w2 = createRandomWallet();
        final Wallet w3 = createRandomWallet();

        walletService.delete(w1);
        walletService.delete(w2);
        walletService.delete(w3);

        /*
         * From a state perspective it was never in the database,
         *  the desired state is achieved and the event is published.
         */
        walletService.delete(w1);

        Assertions.assertEquals(4, walletEventTracker.walletDeletingEvents.size(), "4 WalletDeletingEvent should have been fired");
        Assertions.assertEquals(4, walletEventTracker.walletDeletedEvents.size(), "4 WalletDeletedEvent should have been fired");
        Assertions.assertEquals(0, walletRepository.count(), "0 Wallets should be in the database");
    }

    @Test
    @SneakyThrows
    public void testWalletFindById() {
        createWallet("1", "name");
        createWallet("2", "name");
        createWallet("3", "name");

        final Optional<Wallet> wallet = walletService.findById(new WalletId("1"));

        Assertions.assertTrue(wallet.isPresent(), "Wallet should be present");
    }

    @Test
    @SneakyThrows
    public void testWalletFindByName() {
        createWallet("1", "name");
        createWallet("2", "name");
        createWallet("3", "name");

        final WalletQuery walletQuery = WalletQuery.builder()
                .name(new WalletName("name"))
                .build();
        final Page<Wallet> wallets = walletService.findAll(walletQuery, 0, 10);

        Assertions.assertEquals(3, wallets.getTotalElements(), "3 Wallets should be found");
    }

    @Configuration
    static class WalletEventTrackerConfiguration {
        @Bean
        public WalletEventTracker walletEventTracker() {
            return new WalletEventTracker();
        }
    }

    static class WalletEventTracker {
        final List<WalletCreatingEvent> walletCreatingEvents = new ArrayList<>();
        final List<WalletCreatedEvent> walletCreatedEvents = new ArrayList<>();
        final List<WalletDeletedEvent> walletDeletedEvents = new ArrayList<>();
        final List<WalletDeletingEvent> walletDeletingEvents = new ArrayList<>();
        final List<WalletUpdatedEvent> walletUpdatedEvents = new ArrayList<>();
        final List<WalletUpdatingEvent> walletUpdatingEvents = new ArrayList<>();

        @EventListener
        public void onWalletCreatingEvent(@NonNull WalletCreatingEvent event) {
            walletCreatingEvents.add(event);
        }

        @EventListener
        public void onWalletCreatedEvent(@NonNull WalletCreatedEvent event) {
            walletCreatedEvents.add(event);
        }

        @EventListener
        public void onWalletDeletedEvent(@NonNull WalletDeletedEvent event) {
            walletDeletedEvents.add(event);
        }

        @EventListener
        public void onWalletDeletingEvent(@NonNull WalletDeletingEvent event) {
            walletDeletingEvents.add(event);
        }

        @EventListener
        public void onWalletUpdatedEvent(@NonNull WalletUpdatedEvent event) {
            walletUpdatedEvents.add(event);
        }

        @EventListener
        public void onWalletUpdatingEvent(@NonNull WalletUpdatingEvent event) {
            walletUpdatingEvents.add(event);
        }

        public void clear() {
            walletCreatedEvents.clear();
            walletCreatingEvents.clear();
            walletDeletedEvents.clear();
            walletDeletingEvents.clear();
            walletUpdatedEvents.clear();
            walletUpdatingEvents.clear();
        }
    }
}

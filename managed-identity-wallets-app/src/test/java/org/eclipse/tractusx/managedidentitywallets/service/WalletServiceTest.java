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
import org.eclipse.tractusx.managedidentitywallets.exceptions.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.util.MiwIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class}, classes = WalletServiceTest.WalletEventTrackerConfiguration.class)
public class WalletServiceTest extends MiwIntegrationTest {


    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletEventTracker walletEventTracker;

    @BeforeEach
    public void setup() {
        walletEventTracker.clear();
    }

    @Test
    @SneakyThrows
    public void testWalletCreationEvents() {
        Wallet w1 = newWalletObject("1", "1", "1");
        Wallet w2 = newWalletObject("2", "2", "2");
        Wallet w3 = newWalletObject("3", "3", "3");

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
    }

    @Test
    @SneakyThrows
    public void testWalletDeletionEvents() {
        Wallet w1 = createRandomWallet();
        Wallet w2 = createRandomWallet();
        Wallet w3 = createRandomWallet();

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

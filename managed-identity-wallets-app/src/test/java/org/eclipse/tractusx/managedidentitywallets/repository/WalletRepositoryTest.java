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

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletDescription;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class})
public class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    @SneakyThrows
    public void testWalletCreation() {

        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");
        final WalletDescription walletDescription = new WalletDescription("baz");

        final Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .walletDescription(walletDescription)
                .ed25519Keys(List.of())
                .build();

        walletRepository.create(wallet);

        final WalletQuery query = WalletQuery.builder()
                .walletId(walletId)
                .build();

        final Optional<Wallet> result = walletRepository.find(query);

        Assertions.assertTrue(result.isPresent(), "Wallet not found");
    }

    @Test
    @SneakyThrows
    public void testWalletUpdate() {

        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");
        final WalletDescription walletDescription = new WalletDescription("baz");

        final Wallet.WalletBuilder walletBuilder = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .walletDescription(walletDescription)
                .ed25519Keys(List.of());

        walletRepository.create(walletBuilder.build());

        final WalletName newWalletName = new WalletName("bar2");
        walletBuilder.walletName(newWalletName);

        walletRepository.update(walletBuilder.build());

        final WalletQuery query = WalletQuery.builder()
                .walletId(walletId)
                .build();

        final Optional<Wallet> result = walletRepository.find(query);

        Assertions.assertTrue(result.isPresent(), "Wallet not found");
        Assertions.assertEquals(newWalletName, result.get().getWalletName(), "Wallet name not updated");
    }


    @Test
    @SneakyThrows
    public void testWalletDeletion() {

        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");
        final WalletDescription walletDescription = new WalletDescription("baz");

        final Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .walletDescription(walletDescription)
                .ed25519Keys(List.of())
                .build();

        walletRepository.create(wallet);
        walletRepository.delete(walletId);

        final WalletQuery query = WalletQuery.builder()
                .walletId(walletId)
                .build();

        final Optional<Wallet> result = walletRepository.find(query);
        Assertions.assertTrue(result.isEmpty(), "Wallet found, but should have been deleted");
    }

}

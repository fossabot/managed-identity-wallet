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

package org.eclipse.tractusx.managedidentitywallets.v2.map;

import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletDescription;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WalletsApiMapperTest {

    private final WalletsApiMapper walletsApiMapper = new WalletsApiMapperImpl();

    @Test
    public void testMapper() {
        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");
        final WalletDescription walletDescription = new WalletDescription("baz");
        final Wallet originalWallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .walletDescription(walletDescription)
                .build();

        final Wallet mappedWallet = walletsApiMapper.map(walletsApiMapper.map(originalWallet));

        Assertions.assertEquals(mappedWallet.getWalletId(), walletId, "WalletId should be the same");
        Assertions.assertEquals(mappedWallet.getWalletName(), walletName, "WalletName should be the same");
        Assertions.assertEquals(mappedWallet.getWalletDescription(), walletDescription, "WalletDescription should be the same");
    }
}

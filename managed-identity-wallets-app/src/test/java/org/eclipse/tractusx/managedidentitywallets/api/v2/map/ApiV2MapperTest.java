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

package org.eclipse.tractusx.managedidentitywallets.api.v2.map;

import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApiV2MapperTest {

    private final ApiV2Mapper apiV2Mapper = new ApiV2MapperImpl();

    @Test
    public void testWalletMap() {
        final WalletId walletId = new WalletId("foo");
        final WalletName walletName = new WalletName("bar");
        final Wallet originalWallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .build();

        final Wallet mappedWallet = apiV2Mapper.mapWallet(apiV2Mapper.mapWalletV2(originalWallet));

        Assertions.assertEquals(mappedWallet.getWalletId(), walletId, "WalletId should be the same");
        Assertions.assertEquals(mappedWallet.getWalletName(), walletName, "WalletName should be the same");
    }
}

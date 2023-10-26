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

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.WalletResponseV2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletResponseMap {

    private final WalletObjectMap walletObjectMap;

    public WalletResponseV2 map(Page<Wallet> walletPage) {
        final WalletResponseV2 response = new WalletResponseV2();
        response.setSize(walletPage.getNumber());
        response.setPage(walletPage.getNumber());
        response.setTotalElements(walletPage.getTotalElements());
        response.setItems(walletObjectMap.map(walletPage));
        return response;
    }
}

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
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.WalletResponseV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.WalletV2;
import org.hibernate.annotations.Comment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WalletObjectMap {
    public List<WalletV2> map(Page<Wallet> walletPage) {

        return walletPage.stream()
                .map(w -> {
                    var wallet = new WalletV2();
                    wallet.id(w.getWalletId().getText());
                    wallet.name(w.getWalletName().getText());
                    wallet.description(w.getWalletDescription().getText());
                    return wallet;
                })
                .collect(Collectors.toList());
    }
}

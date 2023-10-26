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

package org.eclipse.tractusx.managedidentitywallets.v2.delegate;


import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.V2ApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.WalletResponseV2;
import org.eclipse.tractusx.managedidentitywallets.v2.map.WalletResponseMap;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
//@ContextConfiguration(initializers = {TestContextInitializer.class})
@Component
@RequiredArgsConstructor
public class V2ApiDelegateImpl implements V2ApiDelegate {

    private final WalletService walletService;
    private final WalletResponseMap walletResponseMap;

    @Override
    public ResponseEntity<WalletResponseV2> adminWalletsGet(Integer page, Integer perPage) {
        final Page<Wallet> wallets = walletService.findAll(page, perPage);
        final WalletResponseV2 response = walletResponseMap.map(wallets);
        return ResponseEntity.ok(response);
    }
}

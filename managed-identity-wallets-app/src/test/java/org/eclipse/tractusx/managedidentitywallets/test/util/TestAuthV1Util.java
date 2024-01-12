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

package org.eclipse.tractusx.managedidentitywallets.test.util;

import io.restassured.http.Header;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.ApplicationRole;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Getter
@Component
@RequiredArgsConstructor
public class TestAuthV1Util {

    private static final List<String> ALL_ROLES = List.of(ApplicationRole.ROLE_VIEW_WALLETS, ApplicationRole.ROLE_VIEW_WALLET, ApplicationRole.ROLE_ADD_WALLETS, ApplicationRole.ROLE_UPDATE_WALLETS, ApplicationRole.ROLE_UPDATE_WALLET, ApplicationRole.ROLE_MANAGE_APP);

    private final TestAuthV2Util testAuthV2Util;
    private final TestPersistenceUtil testPersistenceUtil;

    public HttpHeaders getValidUserHttpHeaders() {
        final Wallet wallet = testPersistenceUtil.newWalletPersisted();
        final Header header = testAuthV2Util.getAuthHeader(ALL_ROLES, wallet);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(header.getName(), header.getValue());
        return headers;
    }

    public HttpHeaders getValidUserHttpHeaders(String bpn) {
        final Wallet wallet = testPersistenceUtil.getWalletRepository().findById(new WalletId(bpn)).orElseThrow();
        final Header header = testAuthV2Util.getAuthHeader(ALL_ROLES, wallet);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(header.getName(), header.getValue());
        return headers;
    }

    public HttpHeaders getNonExistingUserHttpHeaders() {
        final Wallet nonPersistetWallet = testPersistenceUtil.newWallet(UUID.randomUUID().toString(), "foo");
        final Header header = testAuthV2Util.getAuthHeader(ALL_ROLES, nonPersistetWallet);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(header.getName(), header.getValue());
        return headers;
    }
}

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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.admin;

import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.RestAssuredTestCase;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.UpdateWalletRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class PutWalletApiAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Test
    public void testPutWalletAdminApiSuccess() {

        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final UpdateWalletRequestPayloadV2 payload = new UpdateWalletRequestPayloadV2();
        payload.id(wallet.getWalletId().getText());
        payload.name("foo");

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(202);

        payload.id("foo"); // non-existing wallet
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(404);

        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(wallet.getWalletId())
                .build();
        final Wallet storedWallet = walletRepository.findOne(walletQuery).orElseThrow();
        Assertions.assertEquals("foo", storedWallet.getWalletName().getText(), "Wallet name should have been updated");
    }
}

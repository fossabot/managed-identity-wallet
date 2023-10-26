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

package org.eclipse.tractusx.managedidentitywallets.v2.api;

import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.util.MiwIntegrationTest;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class})
public class AdminApiTest extends MiwIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void testAdminApiWalletGetRequest() {

        createRandomWallet();
        createRandomWallet();

        when()
                .get("/api/v2/admin/wallets?page=1&per_page=1")
                .then()
                .statusCode(200)
                .body("page", equalTo(1))
                .body("size", equalTo(1))
                .body("items.size()", equalTo(1))
                .body("totalElements", equalTo(2));
    }

    @Test
    public void testAdminApiWalletDeleteRequest() {

        final Wallet wallet = createRandomWallet();
        createRandomWallet();

        when()
                .delete("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(204);

        when()
                .delete("/api/v2/admin/wallets/foo")
                .then()
                .statusCode(204);

        Assertions.assertEquals(1, walletRepository.count(), "Wallet should have been deleted");
    }
}

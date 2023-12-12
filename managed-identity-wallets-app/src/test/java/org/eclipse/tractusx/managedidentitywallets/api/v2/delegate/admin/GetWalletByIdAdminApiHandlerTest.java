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

import io.restassured.http.Header;
import org.eclipse.tractusx.managedidentitywallets.api.v2.ApiRolesV2;
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.RestAssuredTestCase;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV2Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class GetWalletByIdAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    public TestAuthV2Util testAuthV2Util;

    @Test
    public void testUnauthorizedAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();

        when()
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(401);
    }

    @Test
    public void testSuccessfulAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .when()
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(200);
    }

    @Test
    public void testForbiddenAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final Header auth_foo = testAuthV2Util.getAuthHeader(List.of("FOO"));

        given()
                .header(auth_foo)
                .when()
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(403);
    }

    @Test
    public void testGetWalletByIdAdminApiSuccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(200);
    }

    @Test
    public void testGetWalletByIdAdminApiNotFound() {
        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .get("/api/v2/admin/wallets/foo")
                .then()
                .statusCode(404);
    }
}

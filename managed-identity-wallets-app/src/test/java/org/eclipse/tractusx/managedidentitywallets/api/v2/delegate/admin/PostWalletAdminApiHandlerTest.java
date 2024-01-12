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
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.CreateWalletRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV2Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class PostWalletAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    public TestAuthV2Util testAuthV2Util;

    @Test
    public void testUnauthorizedAccess() {
        final CreateWalletRequestPayloadV2 payload = new CreateWalletRequestPayloadV2();
        payload.id(UUID.randomUUID().toString());
        payload.name("foo");

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .statusCode(401);
    }

    @Test
    public void testSuccessfulAccess() {
        final CreateWalletRequestPayloadV2 payload = new CreateWalletRequestPayloadV2();
        payload.id(UUID.randomUUID().toString());
        payload.name("foo");

        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .statusCode(201);
    }

    @Test
    public void testForbiddenAccess() {
        final CreateWalletRequestPayloadV2 payload = new CreateWalletRequestPayloadV2();
        payload.id(UUID.randomUUID().toString());
        payload.name("foo");

        final Header auth_foo = testAuthV2Util.getAuthHeader(List.of("FOO"));

        given()
                .header(auth_foo)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .statusCode(403);
    }

    @Test
    public void testPostWalletAdminApiSuccess() {

        final CreateWalletRequestPayloadV2 payload = new CreateWalletRequestPayloadV2();
        payload.id(UUID.randomUUID().toString());
        payload.name("foo");

        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(201);

        given()
                .header(auth_admin)
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(409);

        Assertions.assertEquals(2, walletRepository.count(), "One Wallet should have been created (plus Authority Wallet = 2)");
    }

}

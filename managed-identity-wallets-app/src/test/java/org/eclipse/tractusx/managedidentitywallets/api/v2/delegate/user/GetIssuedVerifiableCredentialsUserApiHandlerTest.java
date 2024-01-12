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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.user;

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
import static org.hamcrest.Matchers.equalTo;

public class GetIssuedVerifiableCredentialsUserApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    public TestAuthV2Util testAuthV2Util;

    @Test
    public void testUnauthorizedAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        persistenceUtil.newWalletPersisted();

        when()
                .get("/api/v2/signed-verifiable-credentials")
                .then()
                .statusCode(401);
    }

    @Test
    public void testSuccessfulAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        persistenceUtil.newWalletPersisted();

        final Header auth = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.WALLET_OWNER), wallet);

        given()
                .header(auth)
                .when()
                .get("/api/v2/signed-verifiable-credentials")
                .then()
                .statusCode(200);
    }

    @Test
    public void testForbiddenAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        persistenceUtil.newWalletPersisted();

        final Header auth = testAuthV2Util.getAuthHeader(List.of("FOO"), wallet);

        given()
                .header(auth)
                .when()
                .get("/api/v2/signed-verifiable-credentials")
                .then()
                .statusCode(403);
    }

    @Test
    public void testGetSignedVerifiableCredentialsUserApiSuccess() {

        final Wallet issuerWallet = persistenceUtil.newWalletPersisted();

        final int MAX_CREDENTIALS = 10;
        for (var i = 0; i < MAX_CREDENTIALS; i++) {
            persistenceUtil.newWalletPlusVerifiableCredentialPersisted(issuerWallet);
        }

        final Header auth = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.WALLET_OWNER), issuerWallet);
        given()
                .header(auth)
                .when()

                .get("/api/v2/signed-verifiable-credentials?page=0&per_page=" + MAX_CREDENTIALS)
                .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(MAX_CREDENTIALS))
                .body("items.size()", equalTo(MAX_CREDENTIALS))
                .body("totalElements", equalTo(MAX_CREDENTIALS));
    }
}

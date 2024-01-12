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
import org.eclipse.tractusx.managedidentitywallets.repository.database.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV2Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class PostVerifiableCredentialAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    public TestAuthV2Util testAuthV2Util;

    @Test
    public void testUnauthorizedAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(wallet);

        given()
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .then()
                .statusCode(401);
    }

    @Test
    public void testSuccessfulAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(wallet);

        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .post("/api/v2/admin/verifiable-credentials")
                .then()
                .statusCode(201);
    }

    @Test
    public void testForbiddenAccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(wallet);

        final Header auth_foo = testAuthV2Util.getAuthHeader(List.of("FOO"));

        given()
                .header(auth_foo)
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .post("/api/v2/admin/verifiable-credentials")
                .then()
                .statusCode(403);
    }

    @Test
    public void testPostWalletAdminApiSuccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(wallet);

        final Header auth_admin = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.ADMIN));

        given()
                .header(auth_admin)
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .post("/api/v2/admin/verifiable-credentials")
                .then()
                .statusCode(201);

        given()
                .header(auth_admin)
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .post("/api/v2/admin/verifiable-credentials")
                .then()
                .statusCode(409);

        final int expected = 3 /* Summary + BPN + NewVC */;
        Assertions.assertEquals(3, verifiableCredentialRepository.count(), "Verifiable Credential should have been created");
    }
}

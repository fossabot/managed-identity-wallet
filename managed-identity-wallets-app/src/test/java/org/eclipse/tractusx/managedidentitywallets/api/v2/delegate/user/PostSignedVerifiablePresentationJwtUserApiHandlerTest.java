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
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class PostSignedVerifiablePresentationJwtUserApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    private TestAuthV2Util testAuthV2Util;

    @Test
    public void testVerifiablePresentationJwtIssuingSuccess() {

        final VerifiableCredential verifiableCredential = persistenceUtil.newWalletPlusVerifiableCredentialPersisted();
        final Map<String, Object> payload = Map.of(
                "audience", "foo",
                "verifiableCredentials", List.of(verifiableCredential)
        );

        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final Header auth = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.WALLET_OWNER), wallet);

        given()
                .header("Content-Type", "application/json")
                .header(auth)
                .body(payload)
                .when()
                .post("/api/v2/signed-verifiable-presentations/jwt")
                .then()
                .statusCode(200)
                .log().all()
                .body("verifiable-presentation", org.hamcrest.Matchers.notNullValue());
    }
}

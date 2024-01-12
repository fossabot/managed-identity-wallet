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

public class PostSignedVerifiableCredentialUserApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private TestPersistenceUtil persistenceUtil;


    @Autowired
    private TestAuthV2Util testAuthV2Util;

    @Test
    public void testIssuingOfNewVerifiableCredentialSuccess() {

        final String vcWithoutProof = "{" +
                "    \"verifiableCredentialSubject\": {" +
                "        \"holderIdentifier\": \"FOO\"," +
                "        \"id\": \"BAR\"," +
                "        \"type\": \"SummaryCredential\"," +
                "        \"contractTemplate\": \"https://public.catena-x.org/contracts/\"," +
                "        \"items\": [" +
                "            \"BpnCredential\"" +
                "        ]" +
                "    }," +
                "    \"expirationDate\": \"\"," +
                "    \"additionalVerifiableCredentialTypes\": [" +
                "        \"SummaryCredential\"" +
                "    ]," +
                "    \"additionalVerifiableCredentialContexts\": [" +
                "        \"https://catenax-ng.github.io/product-core-schemas/SummaryVC.json\"" +
                "    ]" +
                "}";

        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final Header auth = testAuthV2Util.getAuthHeader(List.of(ApiRolesV2.WALLET_OWNER), wallet);

        given()
                .header("Content-Type", "application/json")
                .header(auth)
                .body(vcWithoutProof)
                .when()
                .post("/api/v2/signed-verifiable-credentials")
                .then()
                .statusCode(200)
                .log().all()
                .body("proof", org.hamcrest.Matchers.notNullValue());
    }
}

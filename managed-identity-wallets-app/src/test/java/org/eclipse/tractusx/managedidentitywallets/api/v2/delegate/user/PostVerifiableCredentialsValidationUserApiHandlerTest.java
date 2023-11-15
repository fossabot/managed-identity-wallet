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

import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.RestAssuredTestCase;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class PostVerifiableCredentialsValidationUserApiHandlerTest extends RestAssuredTestCase {

    @Test
    public void testPostSignedVerifiableCredentialUserApiHandler() {
        final VerifiableCredential verifiableCredential = newVerifiableCredentialPersisted();
        final Map<String,Object> payload= Map.of(
                "verifiableCredentials", List.of(verifiableCredential)
        );

        given()
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post("/api/v2/verifiable-credentials-validation")
                .then()
                .statusCode(200);
    }
}

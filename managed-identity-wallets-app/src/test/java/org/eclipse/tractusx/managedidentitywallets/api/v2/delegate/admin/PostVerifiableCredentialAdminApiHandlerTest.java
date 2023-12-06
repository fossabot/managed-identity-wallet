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
import org.eclipse.tractusx.managedidentitywallets.repository.database.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class PostVerifiableCredentialAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Test
    public void testPostWalletAdminApiSuccess() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(wallet);

        given()
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .post("/api/v2/admin/verifiable-credentials")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(verifiableCredential)
                .when()
                .post("/api/v2/admin/verifiable-credentials")
                .then()
                .statusCode(409);

        // expect automatically issued BPN+Summary VC + New VC
        Assertions.assertEquals(3, verifiableCredentialRepository.count(), "Verifiable Credential should have been created");
    }
}

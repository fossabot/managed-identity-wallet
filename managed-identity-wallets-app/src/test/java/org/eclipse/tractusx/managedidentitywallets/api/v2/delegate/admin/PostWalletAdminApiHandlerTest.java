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
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.CreateWalletRequestPayloadV2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class PostWalletAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void testPostWalletAdminApiSuccess() {

        final CreateWalletRequestPayloadV2 payload = new CreateWalletRequestPayloadV2();
        payload.id(UUID.randomUUID().toString());
        payload.name("foo");

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(409);

        Assertions.assertEquals(2, walletRepository.count(), "Wallet should have been created");
    }

}

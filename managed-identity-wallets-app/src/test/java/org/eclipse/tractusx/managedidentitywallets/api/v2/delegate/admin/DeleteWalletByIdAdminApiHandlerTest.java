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
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.when;

public class DeleteWalletByIdAdminApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    public void testGetWalletByIdAdminApiSuccess() {

        final Wallet wallet = newWalletPersisted();

        /* GET success */
        when()
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(200);

        /* DELETE */
        when()
                .delete("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(204);

        when()
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(404);

        Assertions.assertEquals(1, walletRepository.count());
    }
}

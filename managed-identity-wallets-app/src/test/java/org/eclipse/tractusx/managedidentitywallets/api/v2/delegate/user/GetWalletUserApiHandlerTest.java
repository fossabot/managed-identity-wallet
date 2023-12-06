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
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.database.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class GetWalletUserApiHandlerTest extends RestAssuredTestCase {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Test
    public void testGetWalletUserApiSuccess() {

        final String bpn = "BPNL000000000000";

        // TODO currently the logged-in user always is BPNL000000000000
        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(new WalletId(bpn))
                .build();
        final Wallet holderWallet = walletRepository.findOne(walletQuery).orElseThrow();

        final int MAX_CREDENTIALS = 10;
        for (var i = 0; i < MAX_CREDENTIALS; i++) {
            final VerifiableCredential verifiableCredential =
                    persistenceUtil.newWalletPlusVerifiableCredentialPersisted(holderWallet);

            walletService.storeVerifiableCredential(holderWallet, verifiableCredential);
        }

        when()
                .get("/api/v2/wallet")
                .then()
                .statusCode(200)
                .body("id", equalTo(bpn));
    }
}

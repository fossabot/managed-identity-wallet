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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate;

import org.eclipse.tractusx.managedidentitywallets.ManagedIdentityWalletsApplication;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.CreateWalletRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.UpdateWalletRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.util.MiwIntegrationTest;
import org.eclipse.tractusx.managedidentitywallets.config.TestContextInitializer;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = {ManagedIdentityWalletsApplication.class})
@ContextConfiguration(initializers = {TestContextInitializer.class})
public class V2ApiDelegateImplTest extends MiwIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Test
    public void testAdminApiCreateWalletRequest() {

        final CreateWalletRequestPayloadV2 payload = new CreateWalletRequestPayloadV2();
        payload.id(UUID.randomUUID().toString());
        payload.name("foo");
        payload.description("bar");

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

        Assertions.assertEquals(1, walletRepository.count(), "Wallet should have been created");
    }

    @Test
    public void testAdminApiUpdateWalletRequest() {

        final Wallet wallet = createRandomWallet();
        final UpdateWalletRequestPayloadV2 payload = new UpdateWalletRequestPayloadV2();
        payload.id(wallet.getWalletId().getText());
        payload.name("foo");
        payload.description("bar");

        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(202);

        payload.id("foo"); // non-existing wallet
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .put("/api/v2/admin/wallets")
                .then()
                .log().all()
                .statusCode(404);

        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(wallet.getWalletId())
                .build();
        final Wallet storedWallet = walletRepository.findOne(walletQuery).orElseThrow();
        Assertions.assertEquals("foo", storedWallet.getWalletName().getText(), "Wallet name should have been updated");
        Assertions.assertEquals("bar", storedWallet.getWalletDescription().getText(), "Wallet description should have been updated");
    }

    @Test
    public void testAdminApiWalletGetByIdRequest() {

        final Wallet wallet = createRandomWallet();

        when()
                .get("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(200);

        when()
                .get("/api/v2/admin/wallets/foo")
                .then()
                .statusCode(404);
    }

    @Test
    public void testAdminApiWalletsGetRequest() {

        createRandomWallet();
        createRandomWallet();

        when()
                .get("/api/v2/admin/wallets?page=0&per_page=1")
                .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(1))
                .body("items.size()", equalTo(1))
                .body("totalElements", equalTo(2));
    }

    @Test
    public void testAdminApiWalletDeleteRequest() {

        final Wallet wallet = createRandomWallet();
        createRandomWallet();

        when()
                .delete("/api/v2/admin/wallets/" + wallet.getWalletId().getText())
                .then()
                .statusCode(204);

        when()
                .delete("/api/v2/admin/wallets/foo")
                .then()
                .statusCode(204);

        Assertions.assertEquals(1, walletRepository.count(), "Wallet should have been deleted");
    }

    @Test
    public void testAdminApiVerifiableCredentialsGetRequest() {

        final int MAX_CREDENTIALS = 10;
        for (var i = 0; i < MAX_CREDENTIALS; i++) {
            createRandomVerifiableCredential();
        }

        final String issuer = "did:test:foo";
        createVerifiableCredential("did:test:foo#1", issuer);

        when()
                .get("/api/v2/admin/verifiable-credentials?page=0&per_page=10&issuer=" + issuer)
                .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(1))
                .body("items.size()", equalTo(1))
                .body("totalElements", equalTo(1));
    }

    @Test
    public void testAdminApiVerifiableCredentialCreateRequest() {

        final VerifiableCredential verifiableCredential = newVerifiableCredential("did:test:foo#1", "did:test:foo");

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

        Assertions.assertEquals(1, verifiableCredentialRepository.count(), "Verifiable Credential should have been created");
    }

    @Test
    public void testAdminApiVerifiableCredentialGetByIdRequest() {

        final VerifiableCredential verifiableCredential = createRandomVerifiableCredential();

        when()
                .get("/api/v2/admin/verifiable-credentials/" + verifiableCredential.getId())
                .then()
                .statusCode(200);

        when()
                .get("/api/v2/admin/verifiable-credentials/foo")
                .then()
                .statusCode(404);
    }

    @Test
    public void testAdminApiVerifiableCredentialDeleteRequest() {

        final VerifiableCredential verifiableCredential = createRandomVerifiableCredential();
        createRandomVerifiableCredential();

        when()
                .delete("/api/v2/admin/verifiable-credentials/" + verifiableCredential.getId())
                .then()
                .statusCode(204);

        when()
                .delete("/api/v2/admin/verifiable-credentials/foo")
                .then()
                .statusCode(204);

        Assertions.assertEquals(1, verifiableCredentialRepository.count(), "Verifiable Credential should have been deleted");
    }
}

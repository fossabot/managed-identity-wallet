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

package org.eclipse.tractusx.managedidentitywallets.test;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.vault.VaultContainer;

import java.time.Year;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("dev")
public abstract class MiwTestCase {

    private static final String KEYCLOAK_ADMIN_USER_NAME = "admin";
    private static final String KEYCLOAK_ADMIN_PASSWORD = "admin";
    private static final String VAULT_TOKEN = "00000000-0000-0000-0000-000000000foo";
    private static final String VAULT_INIT_COMMAND = "secrets enable transit";

    public static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer()
            .withAdminUsername(KEYCLOAK_ADMIN_USER_NAME)
            .withAdminPassword(KEYCLOAK_ADMIN_PASSWORD)
            .withReuse(true);
    private static final VaultContainer<?> VAULT_CONTAINER = new VaultContainer<>("hashicorp/vault:1.13")
            .withVaultToken(VAULT_TOKEN)
            .withInitCommand(VAULT_INIT_COMMAND)
            .withReuse(true);

    static {
        KEYCLOAK_CONTAINER.start();
        VAULT_CONTAINER.start();
    }

    @Autowired
    private TestPersistenceUtil testPersistenceUtil;

    @Autowired
    private WalletEventTracker walletEventTracker;

    @Autowired
    private VerifiableCredentialEventTracker verifiableCredentialEventTracker;

    @BeforeEach
    public void beforeEach() {
        testPersistenceUtil.cleanUp();
        walletEventTracker.clear();
        verifiableCredentialEventTracker.clear();
    }

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("miw.security.auth-server-url", KEYCLOAK_CONTAINER::getAuthServerUrl);
        registry.add("miw.security.realm", () -> "miw_test");
        registry.add("miw.security.clientId", () -> "miw_private_client");
        registry.add("miw.security.auth-url", () -> "${miw.security.auth-server-url}realms/${miw.security.realm}/protocol/openid-connect/auth");
        registry.add("miw.security.token-url", () -> "${miw.security.auth-server-url}realms/${miw.security.realm}/protocol/openid-connect/token");
        registry.add("miw.security.refresh-token-url", () -> "${miw.security.token-url}");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "${miw.security.auth-server-url}realms/${miw.security.realm}");
        registry.add("spring.security.oauth2.resourceserver.jwk-set-uri", () -> "${miw.security.auth-server-url}realms/${miw.security.realm}/protocol/openid-connect/certs");
        registry.add("spring.cloud.vault.enabled", () -> "true");
        registry.add("spring.cloud.vault.token", () -> VAULT_TOKEN);
        registry.add("spring.cloud.vault.host", VAULT_CONTAINER::getHost);
        registry.add("spring.cloud.vault.port", VAULT_CONTAINER::getFirstMappedPort);
        registry.add("spring.cloud.vault.scheme", () -> "http");
        registry.add("miw.vcExpiryDate", () -> "01-01-" + Year.now().getValue() + 1);
    }
}

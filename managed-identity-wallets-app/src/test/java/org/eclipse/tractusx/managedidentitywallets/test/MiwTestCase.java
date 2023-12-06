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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.vault.VaultContainer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("dev")
public abstract class MiwTestCase {


    public static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer().withRealmImportFile("miw-test-realm.json");
    private static final String VAULT_TOKEN = "00000000-0000-0000-0000-000000000foo";
    private static final VaultContainer<?> VAULT_CONTAINER = new VaultContainer<>("hashicorp/vault:1.13")
            .withVaultToken(VAULT_TOKEN)
            .withInitCommand(
                    "secrets enable transit",
                    "write -f transit/keys/my-key",
                    "kv put secret/testing1 top_secret=password123",
                    "kv put secret/testing2 secret_one=password1 secret_two=password2 secret_three=password3 secret_three=password3 secret_four=password4"
            );

    @BeforeAll
    public static void beforeAll() {
        KEYCLOAK_CONTAINER.start();
        VAULT_CONTAINER.start();
    }

    @AfterAll
    public static void afterAll() {
        KEYCLOAK_CONTAINER.stop();
        VAULT_CONTAINER.stop();
    }

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("miw.security.auth-server-url", KEYCLOAK_CONTAINER::getAuthServerUrl);
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
    }
}

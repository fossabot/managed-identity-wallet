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
import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("dev")
// TODO Clean Up this class
public abstract class MiwTestCase {

    public static final KeycloakContainer KEYCLOAK_CONTAINER = new KeycloakContainer().withRealmImportFile("miw-test-realm.json");

    @Autowired
    private GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Autowired
    private VerifiableCredentialEventTracker verifiableCredentialEventTracker;

    @Autowired
    private WalletEventTracker walletEventTracker;

    @BeforeAll
    public static void beforeAll() {
        KEYCLOAK_CONTAINER.start();
    }

    @BeforeEach
    public void cleanUp() {

        walletEventTracker.clear();
        verifiableCredentialEventTracker.clear();
        verifiableCredentialRepository.deleteAll();

        // delete all except authority wallet
        for (Wallet wallet : walletRepository.findAll(WalletQuery.builder().build(), Pageable.unpaged())) {
            if (wallet.getWalletId().getText().equals("BPNL000000000000")) {
                continue;
            }

            walletRepository.delete(wallet.getWalletId());
        }
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
    }

    @SneakyThrows
    protected VerifiableCredential newWalletPlusVerifiableCredentialPersisted() {
        final Wallet wallet = newWalletPersisted();
        return newWalletPlusVerifiableCredentialPersisted(wallet);
    }

    @SneakyThrows
    protected VerifiableCredential newWalletPlusVerifiableCredentialPersisted(@NonNull Wallet issuer) {
        final VerifiableCredential verifiableCredential = newVerifiableCredential(issuer);

        verifiableCredentialService.create(verifiableCredential);
        return verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredential.getId().toString())).orElseThrow();
    }

    protected Wallet newWalletPersisted() {
        final String random = UUID.randomUUID().toString();
        return newWalletPersisted("id" + random, "name" + random);
    }

    @SneakyThrows
    protected Wallet newWalletPersisted(String id) {
        return newWalletPersisted(id, "name" + id);
    }

    @SneakyThrows
    protected Wallet newWalletPersisted(String id, String name) {
        final Wallet wallet = newWallet(id, name);

        walletService.create(wallet);
        return walletService.findById(wallet.getWalletId()).orElseThrow();
    }

    protected Wallet newWallet(String id, String name) {
        final WalletId walletId = new WalletId(id == null ? UUID.randomUUID().toString() : id);
        final WalletName walletName = new WalletName(name == null ? UUID.randomUUID().toString() : name);

        return Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .storedEd25519Keys(List.of())
                .build();
    }

    protected VerifiableCredential newVerifiableCredential(Wallet issuer) {

        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs args
                = GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                .issuerWallet(issuer)
                .subject(new VerifiableCredentialSubject(Map.of(
                        VerifiableCredentialSubject.ID, "" + UUID.randomUUID()
                )))
                .build();

        return genericVerifiableCredentialFactory.createVerifiableCredential(args);
    }

    protected HttpHeaders getValidUserHttpHeaders(String bpn) {
        String token = getApiV1JwtToken(StringPool.VALID_USER_NAME, bpn);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    protected HttpHeaders getInvalidUserHttpHeaders() {
        String token = getInvalidUserToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    protected String getInvalidUserToken() {
        return getApiV1JwtToken(StringPool.INVALID_USER_NAME);
    }

    protected String getApiV1JwtToken(String username, String bpn) {

        List<String> list = List.of("BPN", "bpn", "bPn"); //Do not add more field here, if you do make sure you change in keycloak realm file
        Random randomizer = new Random();
        String attributeName = list.get(randomizer.nextInt(list.size()));

        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_CONTAINER.getAuthServerUrl())
                .realm(StringPool.REALM)
                .clientId(StringPool.CLIENT_ID)
                .clientSecret(StringPool.CLIENT_SECRET)
                .grantType(StringPool.CLIENT_CREDENTIALS)
                .scope(StringPool.OPENID)
                .build();

        RealmResource realmResource = keycloak.realm(StringPool.REALM);

        List<UserRepresentation> userRepresentations = realmResource.users().search(username, true);
        UserRepresentation userRepresentation = userRepresentations.get(0);
        UserResource userResource = realmResource.users().get(userRepresentations.get(0).getId());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);
        userRepresentation.setAttributes(Map.of(attributeName, List.of(bpn)));
        userResource.update(userRepresentation);
        return getApiV1JwtToken(username);
    }

    protected String getApiV1JwtToken(String username) {

        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_CONTAINER.getAuthServerUrl())
                .realm(StringPool.REALM)
                .clientId(StringPool.CLIENT_ID)
                .clientSecret(StringPool.CLIENT_SECRET)
                .username(username)
                .password(StringPool.USER_PASSWORD)
                .build();
        String access_token = keycloakAdminClient.tokenManager().getAccessToken().getToken();

        return StringPool.BEARER_SPACE + access_token;
    }

    @Configuration
    static class MiwTestCaseConfiguration {
        @Bean
        public VerifiableCredentialEventTracker getVerifiableCredentialEventTracker() {
            return new VerifiableCredentialEventTracker();
        }

        @Bean
        public WalletEventTracker getWalletEventTracker() {
            return new WalletEventTracker();
        }
    }
}

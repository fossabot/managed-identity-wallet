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

package org.eclipse.tractusx.managedidentitywallets.test.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase.KEYCLOAK_CONTAINER;

@Getter
@Component
@RequiredArgsConstructor
public class TestAuthV1Util {

    private final TestPersistenceUtil testPersistenceUtil;

    public HttpHeaders getValidUserHttpHeaders() {
        Wallet wallet = testPersistenceUtil.newWalletPersisted();
        String token = getApiV1JwtToken(StringPool.VALID_USER_NAME, wallet.getWalletId().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    public HttpHeaders getValidUserHttpHeaders(String bpn) {
        String token = getApiV1JwtToken(StringPool.VALID_USER_NAME, bpn);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    public HttpHeaders getInvalidUserHttpHeaders() {
        String token = getInvalidUserToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);
        return headers;
    }

    public String getInvalidUserToken() {
        return getApiV1JwtToken(StringPool.INVALID_USER_NAME);
    }

    public String getApiV1JwtToken(String username, String bpn) {

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

    public String getApiV1JwtToken(String username) {

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
}

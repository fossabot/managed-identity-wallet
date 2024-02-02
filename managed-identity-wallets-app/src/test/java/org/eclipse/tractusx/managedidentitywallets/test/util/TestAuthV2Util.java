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

import io.restassured.http.Header;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.*;

import static org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase.KEYCLOAK_CONTAINER;

@Getter
@Component
@RequiredArgsConstructor
public class TestAuthV2Util {

    private static final String KEYCLOAK_URL = KEYCLOAK_CONTAINER.getAuthServerUrl();
    private static final String KEYCLOAK_DEFAULT_REALM = "miw_test";
    private static final String KEYCLOAK_DEFAULT_CLIENT_NAME = "miw_private_client";
    private static final String KEYCLOAK_DEFAULT_CLIENT_SECRET = "miw_private_client";
    private static final String KEYCLOAK_MIW_REALM = "miw_test";
    private static final String KEYCLOAK_MIW_CLIENT = "miw_private_client";
    private static final String KEYCLOAK_ADMIN_USER = "admin";
    private static final String KEYCLOAK_ADMIN_USER_PASSWORD = "admin";
    private static final String ATTRIBUTE_BPN = "bpn";

    public Header getAuthHeader(@NonNull List<String> roles) {
        return getAuthHeader(roles, null);
    }

    public Header getAuthHeader(@NonNull List<String> roles, Wallet wallet) {

        /* Create Realm */
        createRealm(KEYCLOAK_DEFAULT_REALM);

        /* Create Client */
        final Client client = createClient(KEYCLOAK_DEFAULT_CLIENT_NAME, KEYCLOAK_DEFAULT_CLIENT_SECRET);

        /* Create new User */
        final User user = createUser();

        /* Assign roles */
        roles.stream().map(r -> createClientRole(client, r)).forEach(role -> assignClientRoleToUser(user, role, client));

        if (wallet != null) {
            final Keycloak keycloak = getAdminKeycloak();

            final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
            final UsersResource usersResource = realmResource.users();
            final UserResource userResource = usersResource.get(user.getId());

            final Map<String, List<String>> attributes = Map.of(ATTRIBUTE_BPN, List.of(wallet.getWalletId().getText()));
            final UserRepresentation userRepresentation = userResource.toRepresentation();
            userRepresentation.setAttributes(attributes);
            userResource.update(userRepresentation);
        }

        final String token = getBearerToken(user);
        return new Header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private void createRealm(@NonNull String newRealmName) {
        final Keycloak keycloak = getAdminKeycloak();

        RealmsResource realmsResource = keycloak.realms();
        if (realmsResource.findAll().stream().anyMatch(realm -> realm.getRealm().equals(newRealmName))) {
            return;
        }

        RealmRepresentation newRealm = new RealmRepresentation();
        newRealm.setEnabled(true);
        newRealm.setRealm(newRealmName);
        newRealm.setDisplayName("My New Realm");

        // Create realm
        realmsResource.create(newRealm);
    }

    private Client createClient(@NonNull String clientId, @NonNull String clientSecret) {
        final Keycloak keycloak = getAdminKeycloak();

        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final ClientsResource clientsResource = realmResource.clients();

        final Optional<ClientRepresentation> existingClient = clientsResource.findAll().stream().filter(client -> client.getClientId().equals(clientId)).findFirst();
        if (existingClient.isPresent()) {
            return new Client(existingClient.get().getId(), clientId, existingClient.get().getSecret());
        }

        /* map bpn attribute in access token */
        final ProtocolMapperRepresentation protocolMapperRepresentation = new ProtocolMapperRepresentation();
        protocolMapperRepresentation.setProtocol("openid-connect");
        protocolMapperRepresentation.setProtocolMapper("oidc-usermodel-attribute-mapper");
        protocolMapperRepresentation.setName("BPN Mapper");
        protocolMapperRepresentation.setConfig(Map.of(
                "userinfo.token.claim", "true",
                "user.attribute", ATTRIBUTE_BPN,
                "id.token.claim", "true",
                "access.token.claim", "true",
                "claim.name", "bpn",
                "jsonType.label", "String"
        ));

        final ClientRepresentation client = new ClientRepresentation();
        client.setEnabled(true);
        client.setName(KEYCLOAK_DEFAULT_CLIENT_NAME);
        client.setClientId(clientId);
        client.setSecret(clientSecret);
        client.setProtocolMappers(List.of(protocolMapperRepresentation));
        client.setDirectAccessGrantsEnabled(true);

        // Create client
        var response = clientsResource.create(client);
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            throw new RuntimeException(String.format("Error creating client. Status: %s. Data: %s", response.getStatus(), response.getEntity()));
        }


        final String responseClientId = CreatedResponseUtil.getCreatedId(response);

        return new Client(responseClientId, clientId, clientSecret);
    }

    private User createUser() {
        final Keycloak keycloak = getAdminKeycloak();

        final String password = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final UsersResource usersResource = realmResource.users();

        // Define password credential
        final CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        final UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(Collections.singletonList(passwordCred));
        user.setUsername(name);
        user.setFirstName("foo");
        user.setLastName("bar");
        user.setEmail(name + "@localhost.bar");
        user.setAttributes(Map.of(ATTRIBUTE_BPN, Collections.singletonList("BPNL0000000FOO")));

        // Create user (requires manage-users role)
        final Response response = usersResource.create(user);
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            throw new RuntimeException(String.format("Error creating user. Status: %s. Data: %s", response.getStatus(), response.getEntity()));
        }

        final String userId = CreatedResponseUtil.getCreatedId(response);
        return new User(userId, name, password);
    }

    private Role createClientRole(@NonNull Client client, @NonNull String roleName) {
        final Keycloak keycloak = getAdminKeycloak();

        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final ClientsResource clientsResource = realmResource.clients();

        // Find the client
        ClientResource clientResource = realmResource.clients().get(client.getId());

        var existingRole = clientResource.roles().list().stream().filter(r -> r.getName().equals(roleName)).findFirst();
        if (existingRole.isPresent()) {
            return new Role(existingRole.get().getId(), roleName);
        }

        // Define the role
        RoleRepresentation role = new RoleRepresentation();
        role.setId(UUID.randomUUID().toString());
        role.setName(roleName);
        role.setDescription("Description of role " + roleName);
        role.setContainerId(client.getId());
        role.setClientRole(true);

        // Create the role
        clientResource.roles().create(role);

        return new Role(role.getId(), roleName);
    }

    private void assignClientRoleToUser(@NonNull User user, @NonNull Role role, @NonNull Client client) {
        final Keycloak keycloak = getAdminKeycloak();

        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final UserResource userResource = realmResource.users().get(user.getId());
        final ClientResource clientResource = realmResource.clients().get(client.getId());

        final RoleRepresentation roleRepresentation = clientResource.roles().get(role.getName()).toRepresentation();
        userResource.roles().clientLevel(client.getId()).add(Collections.singletonList(roleRepresentation));
    }

    private String getBearerToken(@NonNull User user) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_URL)
                .realm(KEYCLOAK_DEFAULT_REALM)
                .clientId(KEYCLOAK_DEFAULT_CLIENT_NAME)
                .clientSecret(KEYCLOAK_DEFAULT_CLIENT_SECRET)
                .username(user.getName())
                .password(user.getPassword())
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        AccessTokenResponse response = keycloak.tokenManager().getAccessToken();
        return response.getToken();
    }

    private static Keycloak getAdminKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_URL)
                .realm("master")
                .clientId("admin-cli")
                .username(KEYCLOAK_ADMIN_USER)
                .password(KEYCLOAK_ADMIN_USER_PASSWORD)
                .build();
    }

    @Value
    public static class User {
        String id;
        String name;
        String password;
    }

    @Value
    public static class Role {
        String id;
        String name;
    }

    @Value
    public static class Client {
        String id;
        /* also the name */
        String clientId;
        String clientSecret;
    }
}

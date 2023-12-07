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
    private static final String KEYCLOAK_DEFAULT_CLIENT = "miw_private_client";
    private static final String KEYCLOAK_DEFAULT_CLIENT_SECRET = "miw_private_client";
    private static final String KEYCLOAK_MIW_REALM = "miw_test";
    private static final String KEYCLOAK_MIW_CLIENT = "miw_private_client";
    private static final String KEYCLOAK_ADMIN_USER = "admin";
    private static final String KEYCLOAK_ADMIN_USER_PASSWORD = "admin";
    private static final String ATTRIBUTE_BPN = "bpn";

    private final TestPersistenceUtil testPersistenceUtil;

    public Header getAuthHeader(@NonNull List<String> roles) {
        return getAuthHeader(roles, null);
    }

    public Header getAuthHeader(@NonNull List<String> roles, Wallet wallet) {
        /* Create Realm */
        createRealm(KEYCLOAK_DEFAULT_REALM);

        /* Create Client */
        final Client client = createClient(KEYCLOAK_DEFAULT_CLIENT, KEYCLOAK_DEFAULT_CLIENT_SECRET);

        /* Create new User */
        final User user = createUser();

        /* Assign roles */
        roles.stream().map(this::createRole).forEach(role -> assignRole(user, role, client));


        if (wallet != null) {
            final Keycloak keycloak = getKeycloak();

            final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
            final UsersResource usersResource = realmResource.users();
            final UserResource userResource = usersResource.get(user.getId());

            final Map<String, List<String>> attributes = Map.of(ATTRIBUTE_BPN, List.of(wallet.getWalletId().getText()));
            final UserRepresentation userRepresentation = userResource.toRepresentation();
            userRepresentation.setAttributes(attributes);
            userResource.update(userRepresentation);
        }

        final String token = getBearerToken(user);
        return new Header(HttpHeaders.AUTHORIZATION, token);
    }


    public Client createClient(@NonNull String clientId, @NonNull String clientSecret) {
        final Keycloak keycloak = getKeycloak("master", "admin-cli");

        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final ClientsResource clientsResource = realmResource.clients();

        final Optional<ClientRepresentation> existingClient = clientsResource.findAll().stream().filter(client -> client.getClientId().equals(clientId)).findFirst();
        if (existingClient.isPresent()) {
            return new Client(existingClient.get().getId(), clientId);
        }

        final ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setSecret(clientSecret);
        client.setEnabled(true);

        // Create client
        var response = clientsResource.create(client);
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            throw new RuntimeException(String.format("Error creating client. Status: %s. Data: %s", response.getStatus(), response.getEntity()));
        }


        final String responseClientId = CreatedResponseUtil.getCreatedId(response);

        return new Client(responseClientId, clientId);
    }

    public void createRealm(String newRealmName) {
        final Keycloak keycloak = getKeycloak("master", "admin-cli");

        RealmsResource realmsResource = keycloak.realms();
        if (realmsResource.findAll().stream().anyMatch(realm -> realm.getRealm().equals(newRealmName))) {
            return;
        }

        RealmRepresentation newRealm = new RealmRepresentation();
        newRealm.setRealm(newRealmName);
        newRealm.setDisplayName("My New Realm");

        // Create realm
        realmsResource.create(newRealm);
    }

    private User createUser() {
        final Keycloak keycloak = getKeycloak("master", "admin-cli");

        final String password = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final UsersResource usersResource = realmResource.users();

        final UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(name);
        user.setFirstName("foo");
        user.setLastName("bar");
        user.setEmail("foo@localhost.bar");

        // Create user (requires manage-users role)
        final Response response = usersResource.create(user);
        if (response.getStatus() < 200 || response.getStatus() > 299) {
            throw new RuntimeException(String.format("Error creating user. Status: %s. Data: %s", response.getStatus(), response.getEntity()));
        }

        final String userId = CreatedResponseUtil.getCreatedId(response);

        // Define password credential
        final CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        // Set password credential
        usersResource.get(userId).resetPassword(passwordCred);
        return new User(userId, name, password);
    }

    private Role createRole(@NonNull String roleName) {
        final String roleId = UUID.randomUUID().toString();
        final Keycloak keycloak = getKeycloak("master", "admin-cli");

        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final RolesResource rolesResource = realmResource.roles();
        final boolean exists = rolesResource.list().stream().anyMatch(role -> role.getName().equals(roleName));
        if (!exists) {
            final RoleRepresentation role = new RoleRepresentation();
            role.setId(roleId);
            role.setName(roleName);
            role.setDescription("Description of role " + roleName);

            // Create role
            rolesResource.create(role);
        }

        return new Role(roleId, roleName);
    }

    private void assignRole(@NonNull User user, @NonNull Role role, @NonNull Client client) {
        final Keycloak keycloak = getKeycloak("master", "admin-cli");

        final RealmResource realmResource = keycloak.realm(KEYCLOAK_DEFAULT_REALM);
        final UsersResource usersResource = realmResource.users();
        final RolesResource rolesResource = realmResource.roles();



//        final ClientResource clientResource = realmResource.clients().get(client.getId());
//        final RolesResource rolesResource = clientResource.roles();

        final RoleRepresentation roleRep = rolesResource.list().stream().filter(r -> r.getName().equals(role.getName())).findFirst().orElseThrow();

        // Assign role to user
        final UserResource userResource = usersResource.get(user.getId());

        final RoleMappingResource userRoleMappingResource = userResource.roles();
        userRoleMappingResource.clientLevel(client.getId()).add(Collections.singletonList(roleRep));

//        var roles = rolesResource.list();
//
//        final RoleRepresentation roleRepresentation = rolesResource.get(role.getId()).toRepresentation();

    }

    public String getBearerToken(User user) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_URL)
                .realm(KEYCLOAK_DEFAULT_REALM)
                .clientId(KEYCLOAK_DEFAULT_CLIENT)
                .username(user.getId())
                .password(user.getPassword())
                .grantType(OAuth2Constants.PASSWORD)
                .build();

        AccessTokenResponse response = keycloak.tokenManager().getAccessToken();
        return response.getToken();
    }

    private static Keycloak getKeycloak() {
        return getKeycloak(KEYCLOAK_DEFAULT_REALM, KEYCLOAK_DEFAULT_CLIENT);
    }

    private static Keycloak getKeycloak(@NonNull String realm, @NonNull String clientId) {
        return KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK_URL)
                .realm(realm)
                .clientId(clientId)
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
        String clientId;
    }
}

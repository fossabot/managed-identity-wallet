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

package org.eclipse.tractusx.managedidentitywallets.api.v1.vc;

import lombok.*;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueMembershipCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.MembershipVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.VerifiablePresentationFactory;
import org.eclipse.tractusx.managedidentitywallets.models.JsonWebToken;
import org.eclipse.tractusx.managedidentitywallets.models.JsonWebTokenAudience;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentationBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class PresentationValidationTest extends MiwTestCase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MIWSettings miwSettings;
    @Autowired
    private MembershipVerifiableCredentialFactory membershipVerifiableCredentialFactory;
    @Autowired
    private VerifiablePresentationFactory verifiablePresentationFactory;
    @Autowired
    private VerifiableCredentialService verifiableCredentialService;
    @Autowired
    private WalletService walletService;

    private final String bpnTenant_1 = UUID.randomUUID().toString();
    private final String bpnTenant_2 = UUID.randomUUID().toString();
    private Wallet tenant_1;
    private Wallet tenant_2;
    private VerifiableCredential membershipCredential_1;
    private VerifiableCredential membershipCredential_2;

    @BeforeEach
    public void setup() {

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setBpn(bpnTenant_1);
        createWalletRequest.setName("My Test Tenant Wallet");
        tenant_1 = newWalletPersisted(bpnTenant_1);

        CreateWalletRequest createWalletRequest2 = new CreateWalletRequest();
        createWalletRequest2.setBpn(bpnTenant_2);
        createWalletRequest2.setName("My Test Tenant Wallet");
        tenant_2 = newWalletPersisted(bpnTenant_2);

        IssueMembershipCredentialRequest issueMembershipCredentialRequest = new IssueMembershipCredentialRequest();
        issueMembershipCredentialRequest.setBpn(bpnTenant_1);
        membershipCredential_1 = membershipVerifiableCredentialFactory.createMembershipVerifiableCredential(tenant_1);
        verifiableCredentialService.create(membershipCredential_1);
        walletService.storeVerifiableCredential(tenant_1, membershipCredential_1);

        IssueMembershipCredentialRequest issueMembershipCredentialRequest2 = new IssueMembershipCredentialRequest();
        issueMembershipCredentialRequest2.setBpn(bpnTenant_2);
        membershipCredential_2 = membershipVerifiableCredentialFactory.createMembershipVerifiableCredential(tenant_2);
        verifiableCredentialService.create(membershipCredential_2);
        walletService.storeVerifiableCredential(tenant_2, membershipCredential_2);
    }

    @Test
    void testSuccessfulValidation() {
        JsonWebToken presentation = createPresentationJwt(membershipCredential_1, tenant_1);
        VerifiablePresentationValidationResponse response = validateJwtOfCredential(presentation);
        Assertions.assertTrue(response.valid);
    }

    @Test
    @SneakyThrows
    public void testSuccessfulValidationForMultipleVC() {
        final JsonWebToken creationResponse = createPresentationJwt(List.of(membershipCredential_1, membershipCredential_2), tenant_1);
        // get the payload of the json web token
        final String encodedJwtPayload = creationResponse.getText().split("\\.")[1];
        Map<String, Object> decodedJwtPayload = OBJECT_MAPPER.readValue(Base64.getUrlDecoder().decode(encodedJwtPayload), Map.class);
        VerifiablePresentation presentation = new VerifiablePresentation((Map) decodedJwtPayload.get("vp"));
        VerifiablePresentationValidationResponse response = validateJwtOfCredential(creationResponse);

        Assertions.assertTrue(response.valid);

        Assertions.assertEquals(2, presentation.getVerifiableCredentials().size());
    }

    @Test
    public void testValidationFailureOfCredentialWitInvalidExpirationDate() {
        // test is related to this old issue where the signature check still succeeded
        // https://github.com/eclipse-tractusx/SSI-agent-lib/issues/4
        VerifiableCredential expiredVc = membershipVerifiableCredentialFactory.createMembershipVerifiableCredential(tenant_1, OffsetDateTime.now().minusSeconds(1));
        JsonWebToken presentation = createPresentationJwt(expiredVc, tenant_1);
        // e.g. an attacker tries to extend the validity of a verifiable credential

        VerifiablePresentationValidationResponse response = validateJwtOfCredential(presentation);
        Assertions.assertFalse(response.valid);
    }

    @Test
    public void testValidationFailureOfCredentialWitInvalidExpirationDateInSecondCredential() {
        final VerifiableCredential expiredCredential = membershipVerifiableCredentialFactory.createMembershipVerifiableCredential(tenant_1, OffsetDateTime.now().minusSeconds(1));
        final JsonWebToken presentation = createPresentationJwt(List.of(membershipCredential_1, expiredCredential), tenant_1);
        VerifiablePresentationValidationResponse response = validateJwtOfCredential(presentation);
        Assertions.assertFalse(response.valid);
    }

    @Test
    @SneakyThrows
    void testValidationFailureOfPresentationPayloadManipulation() {
        JsonWebToken presentation = createPresentationJwt(membershipCredential_1, tenant_1);

        String jwt = presentation.getText();
        String payload = jwt.split("\\.")[1];
        Base64.Decoder decoder = Base64.getUrlDecoder();
        Base64.Encoder encoder = Base64.getUrlEncoder();

        byte[] payloadDecoded = decoder.decode(payload);
        Map<String, Object> payloadMap = OBJECT_MAPPER.readValue(payloadDecoded, Map.class);

        // replace with credential of another tenant
        VerifiablePresentation newPresentation = new VerifiablePresentationBuilder()
                .context(List.of(VerifiablePresentation.DEFAULT_CONTEXT))
                .id(URI.create("did:web:" + UUID.randomUUID()))
                .type(List.of(VerifiablePresentationType.VERIFIABLE_PRESENTATION))
                .verifiableCredentials(List.of(membershipCredential_2))
                .build();
        payloadMap.put("vp", newPresentation);
        String newPayloadJson = OBJECT_MAPPER.writeValueAsString(payloadMap);
        String newPayloadEncoded = encoder.encodeToString(newPayloadJson.getBytes());

        String newJwt = jwt.split("\\.")[0] + "." + newPayloadEncoded + "." + jwt.split("\\.")[2];

        VerifiablePresentationValidationResponse response = validateJwtOfCredential(new JsonWebToken(newJwt));
        Assertions.assertNotEquals(jwt, newJwt);
        Assertions.assertFalse(response.valid, String.format("The validation should fail because the vp is manipulated.\nOriginal JWT: %s\nNew JWT: %s", jwt, newJwt));
    }

    @SneakyThrows
    private VerifiablePresentationValidationResponse validateJwtOfCredential(JsonWebToken presentationJwt) {
        HttpHeaders headers = getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());
        headers.set("Content-Type", "application/json");
        HttpEntity<Map> entity = new HttpEntity<>(Map.of(StringPool.VP, presentationJwt.getText()), headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.API_PRESENTATIONS_VALIDATION + "?asJwt=true", HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return OBJECT_MAPPER.readValue(response.getBody(), VerifiablePresentationValidationResponse.class);
        }

        throw new RuntimeException(String.format("JWT:\n%s\nResponse: %s",
                presentationJwt,
                OBJECT_MAPPER.writeValueAsString(response)));
    }

    private JsonWebToken createPresentationJwt(List<VerifiableCredential> verifiableCredential, Wallet issuer) {
        return verifiablePresentationFactory.createPresentationAsJwt(issuer, verifiableCredential, new JsonWebTokenAudience("audience"));
    }

    private JsonWebToken createPresentationJwt(VerifiableCredential verifiableCredential, Wallet issuer) {
        return verifiablePresentationFactory.createPresentationAsJwt(issuer, List.of(verifiableCredential), new JsonWebTokenAudience("audience"));
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class VerifiablePresentationValidationResponse {
        boolean valid;
        String vp;
    }
}

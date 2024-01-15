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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.TestUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV1Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

class FrameworkHoldersCredentialTest extends MiwTestCase {

    @Autowired
    private TestAuthV1Util authV1Util;
    @Autowired
    private TestPersistenceUtil persistenceUtil;
    @Autowired
    private VerifiableCredentialService verifiableCredentialService;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MIWSettings miwSettings;
    @Autowired
    private DidFactory didFactory;

    @Test
    void issueFrameworkCredentialTest403() {
        String bpn = UUID.randomUUID().toString();
        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();
        HttpHeaders headers = authV1Util.getNonExistingUserHttpHeaders();

        IssueFrameworkCredentialRequest request = IssueFrameworkCredentialRequest.builder().holderIdentifier(bpn).type("type").contractTemplate("http://localhost/template").contractVersion("0.0.1").build();

        HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueFrameworkCredentialWithInvalidBpnAccessTest403() throws JSONException {
        String bpn = UUID.randomUUID().toString();
        persistenceUtil.newWalletPersisted(bpn);

        String type = "BehaviorTwinCredential";

        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(bpn);

        IssueFrameworkCredentialRequest twinRequest = TestUtils.getIssueFrameworkCredentialRequest(bpn, type);

        HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(twinRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueFrameWorkVCToBaseWalletTest201() throws JSONException {
        String bpn = miwSettings.getAuthorityWalletBpn();
        String type = "PcfCredential";

        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());

        IssueFrameworkCredentialRequest twinRequest = TestUtils.getIssueFrameworkCredentialRequest(bpn, type);

        HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(twinRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        //check summary credential
        final VerifiableCredentialQuery summaryQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypesOr(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)))
                .holderWalletId(new WalletId(miwSettings.getAuthorityWalletBpn()))
                .build();
        final VerifiableCredential summaryCredential = verifiableCredentialService.findOne(summaryQuery).orElseThrow();

        TestUtils.checkVC(summaryCredential, miwSettings);
        TestUtils.checkSummaryCredential(summaryCredential, type);
    }

    @ParameterizedTest
    @MethodSource("getTypes")
    void issueFrameWorkVCTest201(IssueFrameworkCredentialRequest request) throws JsonProcessingException, JSONException {
        String bpn = request.getHolderIdentifier();
        persistenceUtil.newWalletPersisted(bpn);

        String type = request.getType();
        createAndValidateVC(bpn, type);

        //check in issuer tables
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypesOr(List.of(new VerifiableCredentialType(type)))
                .holderWalletId(new WalletId(bpn))
                .build();
        final Optional<VerifiableCredential> verifiableCredential = verifiableCredentialService.findOne(verifiableCredentialQuery);

        Assertions.assertTrue(verifiableCredential.isPresent());
    }

    static Stream<IssueFrameworkCredentialRequest> getTypes() {
        return Stream.of(
                IssueFrameworkCredentialRequest.builder().holderIdentifier(UUID.randomUUID().toString()).type("BehaviorTwinCredential").build(),
                IssueFrameworkCredentialRequest.builder().holderIdentifier(UUID.randomUUID().toString()).type("PcfCredential").build(),
                IssueFrameworkCredentialRequest.builder().holderIdentifier(UUID.randomUUID().toString()).type("QualityCredential").build(),
                IssueFrameworkCredentialRequest.builder().holderIdentifier(UUID.randomUUID().toString()).type("ResiliencyCredential").build(),
                IssueFrameworkCredentialRequest.builder().holderIdentifier(UUID.randomUUID().toString()).type("SustainabilityCredential").build(),
                IssueFrameworkCredentialRequest.builder().holderIdentifier(UUID.randomUUID().toString()).type("TraceabilityCredential").build()
        );
    }


    @Test
    @DisplayName("Issue framework with invalid type")
    void issueFrameworkCredentialTest400() throws JsonProcessingException, JSONException {
        String bpn = UUID.randomUUID().toString();
        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);


        String type = "cx-traceability1";

        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());

        IssueFrameworkCredentialRequest twinRequest = TestUtils.getIssueFrameworkCredentialRequest(bpn, type);

        HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(twinRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());

    }

    private void createAndValidateVC(String bpn, String type) throws JsonProcessingException {
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());

        IssueFrameworkCredentialRequest twinRequest = TestUtils.getIssueFrameworkCredentialRequest(bpn, type);

        HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(twinRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        validate(bpn, type, response, miwSettings);

    }

    private void validate(String holderBpn, String type, ResponseEntity<String> response, MIWSettings miwSettings) throws JsonProcessingException {

        Did did = didFactory.generateDid(new WalletId(holderBpn));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(response.getBody(), Map.class);
        VerifiableCredential verifiableCredential = new VerifiableCredential(map);
        Assertions.assertTrue(verifiableCredential.getTypes().contains(MIWVerifiableCredentialType.USE_CASE_FRAMEWORK_CONDITION));


        TestUtils.checkVC(verifiableCredential, miwSettings);

        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.TYPE), type);
        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.HOLDER_IDENTIFIER), holderBpn);

        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.ID), did.toString());

        TestUtils.checkVC(verifiableCredential, miwSettings);

        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.TYPE), type);
        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.ID), did.toString());
        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.HOLDER_IDENTIFIER), holderBpn);

        //check summary credential
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypesOr(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)))
                .holderWalletId(new WalletId(holderBpn))
                .build();
        final VerifiableCredential summaryCredential = verifiableCredentialService.findOne(verifiableCredentialQuery)
                .orElseThrow();
        TestUtils.checkSummaryCredential(summaryCredential, type);
    }
}

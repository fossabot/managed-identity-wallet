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
import org.eclipse.tractusx.managedidentitywallets.api.v1.controller.IssuersCredentialController;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.TestUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidResolver;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class HoldersCredentialTest extends MiwTestCase {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MIWSettings miwSettings;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IssuersCredentialController credentialController;

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Autowired
    private WalletService walletService;

    @Test
    void issueCredentialTestWithInvalidBPNAccess403() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        Wallet wallet = newWalletPersisted(bpn);
        HttpHeaders headers = getValidUserHttpHeaders("not valid BPN");

        ResponseEntity<String> response = issueVC(wallet, headers);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueCredentialTest200() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        Wallet wallet = newWalletPersisted(bpn);
        HttpHeaders headers = getValidUserHttpHeaders(bpn);

        ResponseEntity<String> response = issueVC(wallet, headers);

        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        VerifiableCredential verifiableCredential = new VerifiableCredential(new ObjectMapper().readValue(response.getBody(), Map.class));
        Assertions.assertNotNull(verifiableCredential.getProof());

        final Optional<VerifiableCredential> persistedVc = verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredential.getId().toString()));

        Assertions.assertFalse(persistedVc.isEmpty());
        TestUtils.checkVC(persistedVc.get(), miwSettings);
    }

    @Test
    void getCredentialsTest403() {
        HttpHeaders headers = getInvalidUserHttpHeaders();
        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(RestURI.CREDENTIALS, HttpMethod.GET, entity, Map.class);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void getCredentials200() throws com.fasterxml.jackson.core.JsonProcessingException {
        String baseDID = miwSettings.getAuthorityWalletDid();
        String bpn = UUID.randomUUID().toString();
        HttpHeaders headers = getValidUserHttpHeaders(bpn);
        //save wallet
        final Wallet wallet = newWalletPersisted(bpn);
        newVerifiableCredential(wallet);
        String vcList = """
                [
                {"type":"TraceabilityCredential"},
                {"type":"SustainabilityCredential"},
                {"type":"ResiliencyCredential"},
                {"type":"QualityCredential"},
                {"type":"PcfCredential"}
                ]
                """;
        JSONArray jsonArray = new JSONArray(vcList);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            IssueFrameworkCredentialRequest request = TestUtils.getIssueFrameworkCredentialRequest(bpn, jsonObject.get(StringPool.TYPE).toString());
            HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(request, getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn())); //ony base wallet can issue VC
            ResponseEntity<String> exchange = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, String.class);
            Assertions.assertEquals(exchange.getStatusCode().value(), HttpStatus.CREATED.value());
        }

        HttpEntity<Map> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.CREDENTIALS + "?issuerIdentifier={did}"
                , HttpMethod.GET, entity, String.class, baseDID);
        List<VerifiableCredential> credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertEquals(7, Objects.requireNonNull(credentialList).size()); //5  framework + 1 BPN + 1 Summary

        response = restTemplate.exchange(RestURI.CREDENTIALS + "?credentialId={id}"
                , HttpMethod.GET, entity, String.class, credentialList.get(0).getId());
        credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertEquals(1, Objects.requireNonNull(credentialList).size());

        List<String> list = new ArrayList<>();
        list.add(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL);
        response = restTemplate.exchange(RestURI.CREDENTIALS + "?type={list}"
                , HttpMethod.GET, entity, String.class, String.join(",", list));
        credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertEquals(1, credentialList.size());
        VerifiableCredentialSubject subject = credentialList.get(0).getCredentialSubject().get(0);
        List<String> itemList = (List<String>) subject.get(StringPool.ITEMS);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Assertions.assertTrue(itemList.contains(jsonObject.get(StringPool.TYPE).toString()));
        }
    }

    @Test
    void validateCredentialsWithInvalidVC() throws com.fasterxml.jackson.core.JsonProcessingException {
        //data setup
        Map<String, Object> map = issueVC();

        //service call
        try (MockedStatic<LinkedDataProofValidation> utils = Mockito.mockStatic(LinkedDataProofValidation.class)) {

            //mock setup
            LinkedDataProofValidation mock = Mockito.mock(LinkedDataProofValidation.class);
            utils.when(() -> {
                LinkedDataProofValidation.newInstance(Mockito.any(DidResolver.class));
            }).thenReturn(mock);
            Mockito.when(mock.verifiy(Mockito.any(VerifiableCredential.class))).thenReturn(false);

            Map<String, Object> stringObjectMap = credentialController.credentialsValidation(map, false).getBody();
            Assertions.assertFalse(Boolean.parseBoolean(stringObjectMap.get(StringPool.VALID).toString()));
        }
    }

    @Test
    @DisplayName("validate VC with date check true, it should return true")
    void validateCredentialsWithExpiryCheckTrue() throws com.fasterxml.jackson.core.JsonProcessingException {

        //data setup
        Map<String, Object> map = issueVC();

        //service call
        try (MockedStatic<LinkedDataProofValidation> utils = Mockito.mockStatic(LinkedDataProofValidation.class)) {

            //mock setup
            LinkedDataProofValidation mock = Mockito.mock(LinkedDataProofValidation.class);
            utils.when(() -> {
                LinkedDataProofValidation.newInstance(Mockito.any(DidResolver.class));
            }).thenReturn(mock);
            Mockito.when(mock.verifiy(Mockito.any(VerifiableCredential.class))).thenReturn(true);

            Map<String, Object> stringObjectMap = credentialController.credentialsValidation(map, true).getBody();
            Assertions.assertTrue(Boolean.parseBoolean(stringObjectMap.get(StringPool.VALID).toString()));
            Assertions.assertTrue(Boolean.parseBoolean(stringObjectMap.get(StringPool.VALIDATE_EXPIRY_DATE).toString()));
        }
    }

    @Test
    @DisplayName("validate expired VC with date check false, it should return true")
    void validateCredentialsWithExpiryCheckFalse() throws com.fasterxml.jackson.core.JsonProcessingException {

        //data setup
        Map<String, Object> map = issueVC();
        //modify expiry date
        Instant instant = Instant.now().minusSeconds(60);
        map.put("expirationDate", instant.toString());


        //service call
        try (MockedStatic<LinkedDataProofValidation> utils = Mockito.mockStatic(LinkedDataProofValidation.class)) {

            //mock setup
            LinkedDataProofValidation mock = Mockito.mock(LinkedDataProofValidation.class);
            utils.when(() -> {
                LinkedDataProofValidation.newInstance(Mockito.any(DidResolver.class));
            }).thenReturn(mock);
            Mockito.when(mock.verifiy(Mockito.any(VerifiableCredential.class))).thenReturn(true);

            Map<String, Object> stringObjectMap = credentialController.credentialsValidation(map, false).getBody();
            Assertions.assertTrue(Boolean.parseBoolean(stringObjectMap.get(StringPool.VALID).toString()));
        }
    }


    @Test
    @DisplayName("validate expired VC with date check true, it should return false")
    void validateExpiredCredentialsWithExpiryCheckTrue() throws com.fasterxml.jackson.core.JsonProcessingException {

        //data setup
        Map<String, Object> map = issueVC();
        //modify expiry date
        Instant instant = Instant.now().minusSeconds(60);
        map.put("expirationDate", instant.toString());

        //service call
        try (MockedStatic<LinkedDataProofValidation> utils = Mockito.mockStatic(LinkedDataProofValidation.class)) {

            //mock setup
            LinkedDataProofValidation mock = Mockito.mock(LinkedDataProofValidation.class);
            utils.when(() -> {
                LinkedDataProofValidation.newInstance(Mockito.any(DidResolver.class));
            }).thenReturn(mock);
            Mockito.when(mock.verifiy(Mockito.any(VerifiableCredential.class))).thenReturn(true);

            Map<String, Object> stringObjectMap = credentialController.credentialsValidation(map, true).getBody();
            Assertions.assertFalse(Boolean.parseBoolean(stringObjectMap.get(StringPool.VALID).toString()));
            Assertions.assertFalse(Boolean.parseBoolean(stringObjectMap.get(StringPool.VALIDATE_EXPIRY_DATE).toString()));

        }
    }


    private Map<String, Object> issueVC() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        final Wallet wallet = newWalletPersisted(bpn);

        final VerifiableCredential verifiableCredential = newVerifiableCredential(wallet);
        Map<String, Object> map = objectMapper.readValue(verifiableCredential.toJson(), Map.class);
        return map;
    }


    private ResponseEntity<String> issueVC(Wallet wallet, HttpHeaders headers) throws JsonProcessingException {

        VerifiableCredential verifiableCredential = newVerifiableCredential(wallet);

        Map<String, Objects> map = objectMapper.readValue(verifiableCredential.toJson(), Map.class);
        HttpEntity<Map> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.exchange(RestURI.CREDENTIALS, HttpMethod.POST, entity, String.class);
        return response;
    }
}

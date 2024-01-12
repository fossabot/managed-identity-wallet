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
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.TestUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV1Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.net.URI;
import java.util.*;

class IssuersCredentialTest extends MiwTestCase {

    @Autowired
    private TestAuthV1Util authV1Util;
    @Autowired
    private TestPersistenceUtil persistenceUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MIWSettings miwSettings;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private VerifiableCredentialService verifiableCredentialService;
    @Autowired
    private DidFactory didFactory;
    @Autowired
    private WalletService walletService;

    @Test
    void getCredentials200() throws com.fasterxml.jackson.core.JsonProcessingException {
        String baseBPN = miwSettings.getAuthorityWalletBpn();
        String holderBpn = UUID.randomUUID().toString();
        String holderDID = didFactory.generateDid(new WalletId(holderBpn)).toString();
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(baseBPN);
        //save wallet
        final Wallet wallet = persistenceUtil.newWalletPersisted(holderBpn);
        persistenceUtil.newVerifiableCredential(wallet);
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
            IssueFrameworkCredentialRequest request = TestUtils.getIssueFrameworkCredentialRequest(holderBpn, jsonObject.get(StringPool.TYPE).toString());
            HttpEntity<IssueFrameworkCredentialRequest> entity = new HttpEntity<>(request, authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn())); //ony base wallet can issue VC
            ResponseEntity<String> exchange = restTemplate.exchange(RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, HttpMethod.POST, entity, String.class);
            Assertions.assertEquals(exchange.getStatusCode().value(), HttpStatus.CREATED.value());
        }

        HttpEntity<Map> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.ISSUERS_CREDENTIALS + "?holderIdentifier={did}"
                , HttpMethod.GET, entity, String.class, holderBpn);

        List<VerifiableCredential> credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertEquals(7, Objects.requireNonNull(credentialList).size());  //5 framework CV + 1 bpn + 1 Summary VC


        response = restTemplate.exchange(RestURI.ISSUERS_CREDENTIALS + "?credentialId={id}"
                , HttpMethod.GET, entity, String.class, credentialList.get(0).getId());
        credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertEquals(1, Objects.requireNonNull(credentialList).size());

        List<String> list = new ArrayList<>();
//        list.add(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL);
//        response = restTemplate.exchange(RestURI.ISSUERS_CREDENTIALS + "?type={list}"
//                , HttpMethod.GET, entity, String.class, String.join(",", list));
//        credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);
//        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

        //all VC must be type of MEMBERSHIP_CREDENTIAL_CX
//        credentialList.forEach(vc -> {
//            Assertions.assertTrue(vc.getTypes().contains(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL));
//        });

        list = new ArrayList<>();
        list.add(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL);
        response = restTemplate.exchange(RestURI.ISSUERS_CREDENTIALS + "?type={list}&holderIdentifier={did}"
                , HttpMethod.GET, entity, String.class, String.join(",", list), holderBpn);
        credentialList = TestUtils.getVerifiableCredentials(response, objectMapper);
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertEquals(1, Objects.requireNonNull(credentialList).size()); // 1 summary vc

        for (VerifiableCredential vc : credentialList) {
            Assertions.assertEquals(3, vc.getContext().size(), "Each credential requires 3 contexts");
        }
    }

    @Test
    @SneakyThrows
    void issueCredentialsTestWithInvalidRole403() {

        HttpHeaders headers = authV1Util.getNonExistingUserHttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        final Wallet issuerWallet = persistenceUtil.newWalletPersisted(UUID.randomUUID().toString());
        VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(issuerWallet);
        Map<String, Objects> map = objectMapper.readValue(verifiableCredential.toJson(), Map.class);
        HttpEntity<Map> entity = new HttpEntity<>(map, headers);

        final Wallet holderWallet = persistenceUtil.newWalletPersisted();

        ResponseEntity<Map> response = restTemplate.exchange(RestURI.ISSUERS_CREDENTIALS + "?holderDid={did}", HttpMethod.POST, entity, Map.class, holderWallet.getWalletId().toString());

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueCredentialsWithoutBaseWalletBPN403() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(bpn);

        ResponseEntity<String> response = issueVC(wallet, wallet, headers);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueCredentialsToBaseWallet200() throws JsonProcessingException {
        final Wallet issuerWallet = walletService.findById(new WalletId(miwSettings.getAuthorityWalletBpn())).orElseThrow();
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());

        ResponseEntity<String> response = issueVC(issuerWallet, issuerWallet, headers);

        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        VerifiableCredential verifiableCredential = new VerifiableCredential(new ObjectMapper().readValue(response.getBody(), Map.class));
        Assertions.assertNotNull(verifiableCredential.getProof());

        final Optional<VerifiableCredential> persistedVc = verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredential.getId().toString()));

        Assertions.assertFalse(persistedVc.isEmpty());
    }

    @Test
    void issueCredentials200() throws com.fasterxml.jackson.core.JsonProcessingException {

        String bpn = UUID.randomUUID().toString();
        Wallet holderWallet = persistenceUtil.newWalletPersisted(bpn);
        final Wallet issuerWallet = walletService.findById(new WalletId(miwSettings.getAuthorityWalletBpn())).orElseThrow();
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());

        ResponseEntity<String> response = issueVC(issuerWallet, holderWallet, headers);

        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        VerifiableCredential verifiableCredential = new VerifiableCredential(new ObjectMapper().readValue(response.getBody(), Map.class));
        Assertions.assertNotNull(verifiableCredential.getProof());

        final Optional<VerifiableCredential> persistedVc = verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredential.getId().toString()));

        Assertions.assertFalse(persistedVc.isEmpty());
    }


    private ResponseEntity<String> issueVC(Wallet issuerWallet, Wallet holderWallet, HttpHeaders headers) throws JsonProcessingException {

        final Did issuerDid = didFactory.generateDid(issuerWallet);
        VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(issuerWallet);

        // remove proof and replace ID
        verifiableCredential.put(VerifiableCredential.ID, URI.create(issuerDid + "#" + UUID.randomUUID()));
        verifiableCredential.put(VerifiableCredential.PROOF, null);

        Map<String, Objects> map = objectMapper.readValue(verifiableCredential.toJson(), Map.class);
        HttpEntity<Map> entity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(RestURI.ISSUERS_CREDENTIALS + "?holderDid={did}", HttpMethod.POST, entity, String.class, holderWallet.getWalletId().toString());
    }
}

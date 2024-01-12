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
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueMembershipCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.TestUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.SummaryVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.database.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV1Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.*;

class MembershipHoldersCredentialTest extends MiwTestCase {

    @Autowired
    private TestAuthV1Util authV1Util;
    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MIWSettings miwSettings;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VerifiableCredentialRepository verifiableCredentialRepository;

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Autowired
    private SummaryVerifiableCredentialFactory summaryVerifiableCredentialFactory;

    @Test
    void issueMembershipCredentialTest403() {
        String bpn = UUID.randomUUID().toString();

        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();

        HttpHeaders headers = authV1Util.getNonExistingUserHttpHeaders();

        IssueMembershipCredentialRequest request = IssueMembershipCredentialRequest.builder().bpn(bpn).build();

        HttpEntity<IssueMembershipCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_MEMBERSHIP, HttpMethod.POST, entity, Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void testIssueSummeryVCAfterDeleteSummaryVCFromHolderWallet() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();

        // create wallet, in background bpn and summary credential generated
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);

        //delete all VC
        verifiableCredentialRepository.deleteAll();

        //issue membership
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(baseBpn);
        ResponseEntity<String> response = TestUtils.issueMembershipVC(restTemplate, bpn, headers);
        Assertions.assertEquals(response.getStatusCode().value(), HttpStatus.CREATED.value());

        //check summary VC in holder wallet
        VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)))
                .build();
        Optional<VerifiableCredential> summaryVc = verifiableCredentialService.findOne(query);
        Assertions.assertFalse(summaryVc.isEmpty());

        //check items, it should be 1
        List<String> items = (List<String>) summaryVc.get().getCredentialSubject().get(0).get(StringPool.ITEMS);

        Assertions.assertEquals(1, items.size(), "Items should be 1. Items: " + StringUtils.join(items, ","));
        Assertions.assertTrue(items.contains(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL), "Items should contain MembershipCredential. Items: " + StringUtils.join(items, ","));
    }

    @Test
    void testStoredSummaryVCTest() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();

        // create wallet, in background bpn and summary credential generated
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);

        String vc = summaryVerifiableCredentialFactory.createSummaryVerifiableCredential(wallet).toJson();
        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(bpn);

        Map<String, Objects> map = objectMapper.readValue(vc, Map.class);
        HttpEntity<Map> entity = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER_CREDENTIALS, HttpMethod.POST, entity, Map.class, bpn);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        //issue  membership
        HttpHeaders baseHeaders = authV1Util.getValidUserHttpHeaders(baseBpn);
        ResponseEntity<String> response1 = TestUtils.issueMembershipVC(restTemplate, bpn, baseHeaders);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response1.getStatusCode().value());

        //stored VC should not be deleted
        VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)))
                .build();
        Optional<VerifiableCredential> summaryVc = verifiableCredentialService.findOne(query);
        Assertions.assertFalse(summaryVc.isEmpty());
    }

    @Test
    void issueMembershipCredentialToBaseWalletTest201() throws JsonProcessingException, JSONException {

        String bpn = UUID.randomUUID().toString();
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);

        HttpHeaders baseHeaders = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());
        ResponseEntity<String> response = TestUtils.issueMembershipVC(restTemplate, miwSettings.getAuthorityWalletBpn(), baseHeaders);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        VerifiableCredential verifiableCredential = getVerifiableCredential(response);

        TestUtils.checkVC(verifiableCredential, miwSettings);

        validateTypes(verifiableCredential);

        //check in tables
        VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(new VerifiableCredentialId(verifiableCredential.getId().toString()))
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL)))
                .build();
        Optional<VerifiableCredential> membershipVc = verifiableCredentialService.findOne(verifiableCredentialQuery);

        //check summary credential
        Assertions.assertTrue(membershipVc.isPresent());
    }


    @Test
    void issueMembershipCredentialTest201() throws JsonProcessingException, JSONException {

        String bpn = UUID.randomUUID().toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();

        //create wallet
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);


        HttpHeaders baseHeaders = authV1Util.getValidUserHttpHeaders(baseBpn);
        ResponseEntity<String> response = TestUtils.issueMembershipVC(restTemplate, bpn, baseHeaders);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        VerifiableCredential verifiableCredential = getVerifiableCredential(response);

        TestUtils.checkVC(verifiableCredential, miwSettings);

        validateTypes(verifiableCredential);

        //check in tables
        VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(wallet.getWalletId())
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL)))
                .build();
        Optional<VerifiableCredential> membershipVc = verifiableCredentialService.findOne(verifiableCredentialQuery);

        //check summary credential
        Assertions.assertTrue(membershipVc.isPresent());
    }


    @Test
    void issueMembershipCredentialWithInvalidBpnAccess409() {
        String bpn = UUID.randomUUID().toString();

        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();

        //save wallet
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);

        HttpHeaders headers = authV1Util.getValidUserHttpHeaders(bpn);
        IssueMembershipCredentialRequest request = IssueMembershipCredentialRequest.builder().bpn(bpn).build();
        HttpEntity<IssueMembershipCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_MEMBERSHIP, HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueMembershipCredentialWithDuplicateBpn409() {

        String bpn = UUID.randomUUID().toString();

        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();

        //save wallet
        Wallet wallet = persistenceUtil.newWalletPersisted(bpn);

        HttpHeaders baseHeaders = authV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn());
        ResponseEntity<String> response = TestUtils.issueMembershipVC(restTemplate, bpn, baseHeaders);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        ResponseEntity<String> duplicateResponse = TestUtils.issueMembershipVC(restTemplate, bpn, baseHeaders);

        Assertions.assertEquals(HttpStatus.CONFLICT.value(), duplicateResponse.getStatusCode().value());
    }

    @NotNull
    private VerifiableCredential getVerifiableCredential(ResponseEntity<String> response) throws JsonProcessingException {
        Map<String, Object> map = objectMapper.readValue(response.getBody(), Map.class);
        return new VerifiableCredential(map);
    }

    private void validateTypes(VerifiableCredential verifiableCredential) {
        Assertions.assertTrue(verifiableCredential.getTypes().contains(MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL));
        Assertions.assertEquals("Test-X", verifiableCredential.getCredentialSubject().get(0).get(StringPool.MEMBER_OF));
    }
}

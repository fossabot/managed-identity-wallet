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

package org.eclipse.tractusx.managedidentitywallets.api.v1.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueMembershipCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestUtils {

    public static void checkVC(VerifiableCredential verifiableCredential, MIWSettings miwSettings) {
        //check expiry date
        Assertions.assertEquals(0, verifiableCredential.getExpirationDate().compareTo(miwSettings.getVcExpiryDate().toInstant()));
    }

    public static ResponseEntity<String> issueMembershipVC(TestRestTemplate restTemplate, String bpn, HttpHeaders headers) {
        IssueMembershipCredentialRequest request = IssueMembershipCredentialRequest.builder().bpn(bpn).build();
        HttpEntity<IssueMembershipCredentialRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_MEMBERSHIP, HttpMethod.POST, entity, String.class);
    }

    public static IssueFrameworkCredentialRequest getIssueFrameworkCredentialRequest(String bpn, String type) {
        IssueFrameworkCredentialRequest twinRequest = IssueFrameworkCredentialRequest.builder()
                .contractTemplate("http://localhost")
                .contractVersion("v1")
                .type(type)
                .holderIdentifier(bpn)
                .build();
        return twinRequest;
    }

//    public static Wallet getWalletFromString(String body) throws JsonProcessingException {
//        JSONObject jsonObject = new JSONObject(body);
//        //convert DidDocument
//        JSONObject didDocument = jsonObject.getJSONObject(StringPool.DID_DOCUMENT);
//        jsonObject.remove(StringPool.DID_DOCUMENT);
//
//        JSONArray credentialArray = null;
//        if (!jsonObject.isNull(StringPool.VERIFIABLE_CREDENTIALS)) {
//            credentialArray = jsonObject.getJSONArray(StringPool.VERIFIABLE_CREDENTIALS);
//            jsonObject.remove(StringPool.VERIFIABLE_CREDENTIALS);
//        }
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Wallet wallet1 = objectMapper.readValue(jsonObject.toString(), Wallet.class);
//        wallet1.setDidDocument(DidDocument.fromJson(didDocument.toString()));
//
//        //convert VC
//        if (credentialArray != null) {
//            List<VerifiableCredential> verifiableCredentials = new ArrayList<>(credentialArray.length());
//            for (int i = 0; i < credentialArray.length(); i++) {
//                JSONObject object = credentialArray.getJSONObject(i);
//                verifiableCredentials.add(new VerifiableCredential(objectMapper.readValue(object.toString(), Map.class)));
//            }
//            wallet1.setVerifiableCredentials(verifiableCredentials);
//        }
//        System.out.println("wallet -- >" + wallet1.getBpn());
//        return wallet;
//    }

//    public static String getSummaryCredentialId(String holderDID, VerifiableCredentialRepository holdersCredentialRepository) {
//        List<HoldersCredential> holderVCs = holdersCredentialRepository.getByHolderDidAndType(holderDID, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL);
//        Assertions.assertEquals(1, holderVCs.size());
//        return holderVCs.get(0).getData().getId().toString();
//    }

    public static void checkSummaryCredential(VerifiableCredential vc, String type) {

        //get VC from holder of Summary type
        VerifiableCredentialSubject subject = vc.getCredentialSubject().get(0);

        //check if type is in items
        List<String> list = (List<String>) subject.get(StringPool.ITEMS);
        Assertions.assertTrue(list.contains(type));
    }

    @NotNull
    public static List<VerifiableCredential> getVerifiableCredentials(ResponseEntity<String> response, ObjectMapper objectMapper) throws JsonProcessingException {
        Map<String, Object> map = objectMapper.readValue(response.getBody(), Map.class);

        List<Map<String, Object>> vcs = (List<Map<String, Object>>) map.get("content");

        List<VerifiableCredential> credentialList = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : vcs) {
            credentialList.add(new VerifiableCredential(stringObjectMap));
        }
        return credentialList;
    }
}

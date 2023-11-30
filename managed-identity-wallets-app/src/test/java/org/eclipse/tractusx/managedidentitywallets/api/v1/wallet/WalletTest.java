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

package org.eclipse.tractusx.managedidentitywallets.api.v1.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.TestUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.net.URI;
import java.util.*;


class WalletTest extends MiwTestCase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MIWSettings miwSettings;

    @Autowired
    private WalletService walletService;

    @Autowired
    private DidFactory didFactory;

    @Autowired
    private GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;

    @Test
    void createDuplicateAuthorityWalletTest() {
        Assertions.assertThrows(WalletAlreadyExistsException.class, () -> {
            newWalletPersisted(miwSettings.getAuthorityWalletBpn());
        });
    }

    @Test
    void authorityWalletExistTest() {
        final boolean exists = walletService.existsById(new WalletId(miwSettings.getAuthorityWalletBpn()));
        Assertions.assertTrue(exists, "Authority wallet does not exist");
    }


    @Test
    void createWalletTest403() {
        String bpn = UUID.randomUUID().toString();
        String name = "Sample Wallet";
        HttpHeaders headers = getInvalidUserHttpHeaders();

        CreateWalletRequest request = CreateWalletRequest.builder().bpn(bpn).name(name).build();

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<WalletEntity> response = restTemplate.exchange(RestURI.WALLETS, HttpMethod.POST, entity, WalletEntity.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void createWalletTestWithUserToken403() {
        String bpn = UUID.randomUUID().toString();
        String name = "Sample Wallet";
        HttpHeaders headers = getValidUserHttpHeaders(bpn);

        CreateWalletRequest request = CreateWalletRequest.builder().bpn(bpn).name(name).build();

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<WalletEntity> response = restTemplate.exchange(RestURI.WALLETS, HttpMethod.POST, entity, WalletEntity.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }


    @Test
    void createWalletTest201() throws JsonProcessingException, JSONException {

        String bpn = UUID.randomUUID().toString();
        String name = "Sample Wallet";
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        ResponseEntity<String> response = TestUtils.createWallet(bpn, name, restTemplate, headers);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        Wallet wallet = TestUtils.getWalletFromString(response.getBody());

        Assertions.assertNotNull(response.getBody());
        Assertions.assertNotNull(wallet.getDidDocument());
        List<URI> context = wallet.getDidDocument().getContext();
        miwSettings.getDidDocumentContextUrls().forEach(uri -> {
            Assertions.assertTrue(context.contains(uri));
        });
        Assertions.assertEquals(wallet.getBpn(), bpn);
        Assertions.assertEquals(wallet.getName(), name);

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<String> getWalletResponse = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER + "?withCredentials={withCredentials}", HttpMethod.GET, entity, String.class, bpn, "true");
        Assertions.assertEquals(getWalletResponse.getStatusCode().value(), HttpStatus.OK.value());
        Wallet body = TestUtils.getWalletFromString(getWalletResponse.getBody());
        Assertions.assertEquals(2, body.getVerifiableCredentials().size());


        Assertions.assertEquals(body.getBpn(), bpn);
        Assertions.assertEquals(body.getName(), name);
        Assertions.assertNotNull(body);
        Assertions.assertEquals(body.getBpn(), bpn);


        VerifiableCredential verifiableCredential = body.getVerifiableCredentials().stream()
                .filter(vp -> vp.getTypes().contains(MIWVerifiableCredentialType.BPN_CREDENTIAL))
                .findFirst()
                .orElse(null);
        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.ID), wallet.getDid());
        Assertions.assertEquals(verifiableCredential.getCredentialSubject().get(0).get(StringPool.BPN), wallet.getBpn());
        Assertions.assertEquals(MIWVerifiableCredentialType.BPN_CREDENTIAL, verifiableCredential.getCredentialSubject().get(0).get(StringPool.TYPE));

        VerifiableCredential summaryVerifiableCredential = body.getVerifiableCredentials().stream()
                .filter(vc -> vc.getTypes().contains(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)).findFirst()
                .orElse(null);
        VerifiableCredentialSubject subject = summaryVerifiableCredential.getCredentialSubject().get(0);
        List<String> list = (List<String>) subject.get(StringPool.ITEMS);
        Assertions.assertTrue(list.contains(MIWVerifiableCredentialType.BPN_CREDENTIAL));
    }

    @Test
    void storeCredentialsTest201() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        TestUtils.createWallet(bpn, "name", restTemplate, headers);

        ResponseEntity<Map> response = storeCredential(bpn);

        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
    }

    @Test
    void storeCredentialsWithDifferentBPNAccess403() throws JsonProcessingException {
        //make sure authority wallet is created
        authorityWalletExistTest();
        String did = "did:web:localhost:" + miwSettings.getAuthorityWalletBpn();

        HttpHeaders headers = getValidUserHttpHeaders("Invalid BPN");

        final var wallet = newWalletPersisted();
        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs args = GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                .issuerWallet(wallet)
                .subject(new VerifiableCredentialSubject(Map.of("id", "foo")))
                .build();
        VerifiableCredential vc = genericVerifiableCredentialFactory.createVerifiableCredential(args);

        HttpEntity<Map> entity = new HttpEntity<>(vc, headers);

        ResponseEntity<Map> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER_CREDENTIALS, HttpMethod.POST, entity, Map.class, miwSettings.getAuthorityWalletBpn());
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void storeCredentialsWithDifferentHolder403() throws JsonProcessingException {

        String bpn = UUID.randomUUID().toString();
        String did = DidWebFactory.fromHostnameAndPath(miwSettings.getHost(), bpn).toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders("Some random pbn");

        TestUtils.createWallet(bpn, "name", restTemplate, headers);

        ResponseEntity<Map> response = storeCredential(bpn, headers);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());

    }

    @Test
    void createWalletWithDuplicateBpn409() throws JsonProcessingException {

        String bpn = UUID.randomUUID().toString();
        String name = "Sample Wallet";
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        //save wallet
        ResponseEntity<String> response = TestUtils.createWallet(bpn, name, restTemplate, headers);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        TestUtils.getWalletFromString(response.getBody());
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        //try with again with same BPN
        ResponseEntity<String> response1 = TestUtils.createWallet(bpn, name, restTemplate, headers);
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), response1.getStatusCode().value());
    }

    @Test
    void getWalletByIdentifierTest403() {
        String bpn = UUID.randomUUID().toString();
        HttpHeaders headers = getInvalidUserHttpHeaders();

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<WalletEntity> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER, HttpMethod.GET, entity, WalletEntity.class, bpn);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void getWalletByIdentifierWithInvalidBPNTest403() {
        String bpn = UUID.randomUUID().toString();
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        TestUtils.createWallet(bpn, "sample name", restTemplate, headers);

        //create token with different BPN
        headers = getValidUserHttpHeaders("invalid BPN");
        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<WalletEntity> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER, HttpMethod.GET, entity, WalletEntity.class, bpn);

        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void getWalletByIdentifierBPNTest200() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        String name = "Sample Name";
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        //Create entry
        Wallet wallet = TestUtils.getWalletFromString(TestUtils.createWallet(bpn, name, restTemplate, headers).getBody());

        //get wallet without credentials
        headers = getValidUserHttpHeaders(bpn);

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<String> getWalletResponse = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER + "?withCredentials={withCredentials}", HttpMethod.GET, entity, String.class, bpn, "false");

        Wallet body = TestUtils.getWalletFromString(getWalletResponse.getBody());
        Assertions.assertEquals(HttpStatus.OK.value(), getWalletResponse.getStatusCode().value());
        Assertions.assertNotNull(getWalletResponse.getBody());
        Assertions.assertEquals(body.getBpn(), bpn);
    }

    @Test
    void getWalletByIdentifierBPNWithCredentialsTest200() throws JsonProcessingException {
        String bpn = UUID.randomUUID().toString();
        String name = "Sample Name";
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        //Create entry
        Wallet wallet = TestUtils.getWalletFromString(TestUtils.createWallet(bpn, name, restTemplate, headers).getBody());

        //store credentials
        ResponseEntity<Map> response = storeCredential(bpn);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        ///get wallet with credentials
        headers = getValidUserHttpHeaders(bpn);

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<String> getWalletResponse = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER + "?withCredentials={withCredentials}", HttpMethod.GET, entity, String.class, bpn, "true");

        Wallet body = TestUtils.getWalletFromString(getWalletResponse.getBody());
        Assertions.assertEquals(HttpStatus.OK.value(), getWalletResponse.getStatusCode().value());
        Assertions.assertNotNull(getWalletResponse.getBody());
        Assertions.assertEquals(3, body.getVerifiableCredentials().size()); //BPN VC + Summery VC + Stored VC
        Assertions.assertEquals(body.getBpn(), bpn);
    }

    @Test
    @Disabled("the endpoint has an issue that prevents resolving did with a port number")
    void getWalletByIdentifierDidTest200() throws JsonProcessingException {

        String bpn = UUID.randomUUID().toString();
        String name = "Sample Name";
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);

        //Create entry
        Wallet wallet = TestUtils.getWalletFromString(TestUtils.createWallet(bpn, name, restTemplate, headers).getBody());

        headers = getValidUserHttpHeaders(bpn);
        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);
        Did did = didFactory.generateDid(new WalletId(bpn));

        ResponseEntity<String> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER, HttpMethod.GET, entity, String.class, did.toString());

        Wallet body = TestUtils.getWalletFromString(response.getBody());
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(body.getBpn(), bpn);
    }

    @Test
    void getWalletInvalidBpn404() {
        HttpHeaders headers = getValidUserHttpHeaders();

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<WalletEntity> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER, HttpMethod.GET, entity, WalletEntity.class, UUID.randomUUID().toString());

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

    @Test
    void getWallets403() {
        HttpHeaders headers = getInvalidUserHttpHeaders();

        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);
        ResponseEntity<List<WalletEntity>> response = restTemplate.exchange(RestURI.WALLETS, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }


    @Test
    void getWallets200() throws JsonProcessingException {

        String bpn = UUID.randomUUID().toString();
        String name = "Sample Name";
        String baseBpn = miwSettings.getAuthorityWalletBpn();
        HttpHeaders headers = getValidUserHttpHeaders(baseBpn);
        //Create entry
        TestUtils.createWallet(bpn, name, restTemplate, headers);

        headers = getValidUserHttpHeaders();
        HttpEntity<CreateWalletRequest> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(RestURI.WALLETS, HttpMethod.GET, entity, String.class);
        List<Wallet> body = getWalletsFromString(response.getBody());
        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
        Assertions.assertTrue(Objects.requireNonNull(body).size() > 0);
    }


    private ResponseEntity<Map> storeCredential(String bpn, HttpHeaders headers) throws JsonProcessingException {


        final var wallet = newWalletPersisted();
        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs args = GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                .issuerWallet(wallet)
                .subject(new VerifiableCredentialSubject(Map.of("id", "foo")))
                .build();
        VerifiableCredential vc = genericVerifiableCredentialFactory.createVerifiableCredential(args);
        HttpEntity<Map> entity = new HttpEntity<>(vc, headers);

        ResponseEntity<Map> response = restTemplate.exchange(RestURI.API_WALLETS_IDENTIFIER_CREDENTIALS, HttpMethod.POST, entity, Map.class, bpn);
        return response;
    }

    private ResponseEntity<Map> storeCredential(String bpn) throws JsonProcessingException {
        HttpHeaders headers = getValidUserHttpHeaders(bpn);
        return storeCredential(bpn, headers);
    }


    private List<Wallet> getWalletsFromString(String body) throws JsonProcessingException {
        List<Wallet> walletList = new ArrayList<>();

        JSONArray array = new JSONArray(new JSONObject(body).getJSONArray("content"));
        if (array.length() == 0) {
            return walletList;
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject wallet = array.getJSONObject(i);
            walletList.add(TestUtils.getWalletFromString(wallet.toString()));
        }
        return walletList;
    }

}

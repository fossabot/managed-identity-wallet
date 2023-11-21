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
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueDismantlerCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.TestUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.http.*;

import java.util.*;

class DismantlerHoldersCredentialTest extends MiwTestCase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MIWSettings miwSettings;

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

//    @LocalServerPort
//    private int port;
//
//    @BeforeEach
//    public void beforeEach() {
//        restTemplate.setUriTemplateHandler(new RootUriTemplateHandler("http://localhost:" + port));
//    }

    @Test
    void issueDismantlerCredentialTest403() {
        final String bpn = UUID.randomUUID().toString();

        final HttpHeaders headers = getInvalidUserHttpHeaders();

        final IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder().bpn(bpn).activityType("yes").build();

        final HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);

        final ResponseEntity<VerifiableCredential> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, VerifiableCredential.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueDismantlerCredentialToBaseWalletTest201() throws JSONException {
        final ResponseEntity<String> response = issueDismantlerCredential(miwSettings.getAuthorityWalletBpn());
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL)))
                .holderWalletId(new WalletId(miwSettings.getAuthorityWalletBpn()))
                .build();
        final List<VerifiableCredential> credentials = verifiableCredentialService.findAll(query).stream().toList();

        Assertions.assertEquals(1, credentials.size());
    }

    @Test
    void issueDismantlerCredentialTest201() throws JsonProcessingException, JSONException {

        //create wallet
        Wallet newWallet = newWalletPersisted();
        final String bpn = newWallet.getWalletId().toString();

        ResponseEntity<String> response = issueDismantlerCredential(bpn);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(response.getBody(), Map.class);
        VerifiableCredential verifiableCredential = new VerifiableCredential(map);
        Assertions.assertTrue(verifiableCredential.getTypes().contains(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL));

        TestUtils.checkVC(verifiableCredential, miwSettings);

        Assertions.assertEquals(StringPool.VEHICLE_DISMANTLE, verifiableCredential.getCredentialSubject().get(0).get(StringPool.ACTIVITY_TYPE).toString());


        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypes(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL)))
                .holderWalletId(newWallet.getWalletId())
                .build();
        final Optional<VerifiableCredential> createdVc = verifiableCredentialService.findOne(verifiableCredentialQuery);

        Assertions.assertTrue(createdVc.isPresent());
    }

    @Test
    void issueDismantlerCredentialWithInvalidBpnAccess409() {
        String bpn = UUID.randomUUID().toString();

        //create entry
        newWalletPersisted(bpn);

        HttpHeaders headers = getValidUserHttpHeaders(bpn); //token must contain base wallet BPN

        IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder()
                .activityType(StringPool.VEHICLE_DISMANTLE)
                .bpn(bpn)
                .allowedVehicleBrands(Set.of("BMW"))
                .build();

        HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, String.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueDismantlerCredentialWithoutAllowedVehicleBrands() {
        String bpn = UUID.randomUUID().toString();
        Wallet wallet = newWalletPersisted(bpn);

        HttpHeaders headers = getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn()); //token must contain base wallet BPN

        IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder()
                .activityType(StringPool.VEHICLE_DISMANTLE)
                .bpn(bpn)
                .allowedVehicleBrands(null)
                .build();

        HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, String.class);

        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
    }

    @Test
    void issueDismantlerCredentialWithDuplicateBpn409() {

        String bpn = UUID.randomUUID().toString();

        //create entry
        Wallet wallet = newWalletPersisted(bpn);
        ResponseEntity<String> response = issueDismantlerCredential(bpn);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        //issue duplicate
        ResponseEntity<String> duplicateResponse = issueDismantlerCredential(bpn);
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), duplicateResponse.getStatusCode().value());
    }

    private ResponseEntity<String> issueDismantlerCredential(String bpn) {

        HttpHeaders headers = getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn()); //token must contain base wallet BPN

        IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder()
                .activityType(StringPool.VEHICLE_DISMANTLE)
                .bpn(bpn)
                .allowedVehicleBrands(Set.of("BMW"))
                .build();


        HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, String.class);
    }
}

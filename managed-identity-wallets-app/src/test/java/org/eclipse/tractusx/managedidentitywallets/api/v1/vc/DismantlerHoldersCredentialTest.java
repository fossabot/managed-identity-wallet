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
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.database.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestAuthV1Util;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.*;

class DismantlerHoldersCredentialTest extends MiwTestCase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MIWSettings miwSettings;

    @Autowired
    private TestPersistenceUtil testPersistenceUtil;

    @Autowired
    private TestAuthV1Util testAuthV1Util;

    @Autowired
    private VerifiableCredentialService verifiableCredentialService;

    @Test
    void issueDismantlerCredentialTest403NotAuthority() {
        final Wallet nonAuhorityWallet = testPersistenceUtil.newWalletPersisted();
        final HttpHeaders nonAuthorityHeaders = testAuthV1Util.getValidUserHttpHeaders(nonAuhorityWallet.getWalletId().toString());

        final IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder().bpn(nonAuhorityWallet.getWalletId().toString()).activityType("yes").build();

        final HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, nonAuthorityHeaders);

        final ResponseEntity<Object> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueDismantlerCredentialTest403() {
        final String bpn = UUID.randomUUID().toString();

        final HttpHeaders headers = testAuthV1Util.getNonExistingUserHttpHeaders();

        final IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder().bpn(bpn).activityType("yes").build();

        final HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);

        final ResponseEntity<Object> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, Object.class);
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
    }

    @Test
    void issueDismantlerCredentialToBaseWalletTest201() throws JSONException {
        final ResponseEntity<String> response = issueDismantlerCredential(miwSettings.getAuthorityWalletBpn());
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypesOr(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL)))
                .holderWalletId(new WalletId(miwSettings.getAuthorityWalletBpn()))
                .build();
        final List<VerifiableCredential> credentials = verifiableCredentialService.findAll(query).stream().toList();

        Assertions.assertEquals(1, credentials.size());
    }

    @Test
    void issueDismantlerCredentialTest201() throws JsonProcessingException, JSONException {

        //create wallet
        Wallet newWallet = testPersistenceUtil.newWalletPersisted();
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
                .verifiableCredentialTypesOr(List.of(new VerifiableCredentialType(MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL)))
                .holderWalletId(newWallet.getWalletId())
                .build();
        final Optional<VerifiableCredential> createdVc = verifiableCredentialService.findOne(verifiableCredentialQuery);

        Assertions.assertTrue(createdVc.isPresent());
    }

    @Test
    void issueDismantlerCredentialWithInvalidBpnAccess409() {
        String bpn = UUID.randomUUID().toString();

        //create entry
        testPersistenceUtil.newWalletPersisted(bpn);

        HttpHeaders headers = testAuthV1Util.getValidUserHttpHeaders(bpn); //token must contain base wallet BPN

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
        Wallet wallet = testPersistenceUtil.newWalletPersisted(bpn);

        HttpHeaders headers = testAuthV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn()); //token must contain base wallet BPN

        IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder()
                .activityType(StringPool.VEHICLE_DISMANTLE)
                .bpn(bpn)
                .allowedVehicleBrands(null)
                .build();

        HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, String.class);

        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value(), "Credential should be created. " + response.getBody());
    }

    @Test
    void issueDismantlerCredentialWithDuplicateBpn409() {

        String bpn = UUID.randomUUID().toString();

        //create entry
        Wallet wallet = testPersistenceUtil.newWalletPersisted(bpn);
        ResponseEntity<String> response = issueDismantlerCredential(bpn);
        Assertions.assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        //issue duplicate
        ResponseEntity<String> duplicateResponse = issueDismantlerCredential(bpn);
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), duplicateResponse.getStatusCode().value());
    }

    private ResponseEntity<String> issueDismantlerCredential(String bpn) {

        HttpHeaders headers = testAuthV1Util.getValidUserHttpHeaders(miwSettings.getAuthorityWalletBpn()); //token must contain base wallet BPN

        IssueDismantlerCredentialRequest request = IssueDismantlerCredentialRequest.builder()
                .activityType(StringPool.VEHICLE_DISMANTLE)
                .bpn(bpn)
                .allowedVehicleBrands(Set.of("BMW"))
                .build();


        HttpEntity<IssueDismantlerCredentialRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(RestURI.CREDENTIALS_ISSUER_DISMANTLER, HttpMethod.POST, entity, String.class);
    }
}

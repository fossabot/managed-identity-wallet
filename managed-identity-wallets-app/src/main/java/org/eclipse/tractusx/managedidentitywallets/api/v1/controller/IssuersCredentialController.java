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

package org.eclipse.tractusx.managedidentitywallets.api.v1.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.apidocs.HoldersCredentialControllerApiDocs;
import org.eclipse.tractusx.managedidentitywallets.api.v1.apidocs.IssuersCredentialControllerApiDocs;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.service.IssuersCredentialService;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueDismantlerCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueMembershipCredentialRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * The type Issuers credential controller.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class IssuersCredentialController extends BaseController {

    /**
     * The constant API_TAG_VERIFIABLE_CREDENTIAL_ISSUER.
     */
    public static final String API_TAG_VERIFIABLE_CREDENTIAL_ISSUER = "Verifiable Credential - Issuer";
    /**
     * The constant API_TAG_VERIFIABLE_CREDENTIAL_VALIDATION.
     */
    public static final String API_TAG_VERIFIABLE_CREDENTIAL_VALIDATION = "Verifiable Credential - Validation";

    private final IssuersCredentialService issuersCredentialService;


    /**
     * Gets credentials.
     *
     * @param credentialId     the credential id
     * @param holderIdentifier the holder identifier
     * @param type             the type
     * @param pageNumber       the page number
     * @param size             the size
     * @param sortColumn       the sort column
     * @param sortTpe          the sort tpe
     * @return the credentials
     */
    @HoldersCredentialControllerApiDocs.GetCredentialsApiDocs
    @GetMapping(path = RestURI.ISSUERS_CREDENTIALS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<VerifiableCredential>> getCredentials(@Parameter(name = "credentialId", description = "Credential Id", examples = { @ExampleObject(name = "Credential Id", value = "did:web:localhost:BPNL000000000000#12528899-160a-48bd-ba15-f396c3959ae9") }) @RequestParam(required = false) String credentialId,
                                                                     @Parameter(name = "holderIdentifier", description = "Holder identifier(did of BPN)", examples = { @ExampleObject(name = "bpn", value = "BPNL000000000001", description = "bpn"), @ExampleObject(description = "did", name = "did", value = "did:web:localhost:BPNL000000000001") }) @RequestParam(required = false) String holderIdentifier,
                                                                     @Parameter(name = "type", description = "Type of VC", examples = { @ExampleObject(name = "SummaryCredential", value = "SummaryCredential", description = "SummaryCredential"), @ExampleObject(description = "BpnCredential", name = "BpnCredential", value = "BpnCredential") }) @RequestParam(required = false) List<String> type,
                                                                     @Min(0) @Max(Integer.MAX_VALUE) @Parameter(description = "Page number, Page number start with zero") @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                     @Min(0) @Max(Integer.MAX_VALUE) @Parameter(description = "Number of records per page") @RequestParam(required = false, defaultValue = Integer.MAX_VALUE + "") int size,
                                                                     @Parameter(name = "sortColumn", description = "Sort column name",
                                                                             examples = {
                                                                                     @ExampleObject(value = "createdAt", name = "creation date"),
                                                                                     @ExampleObject(value = "holderDid", name = "Holder did"),
                                                                                     @ExampleObject(value = "type", name = "Credential type"),
                                                                                     @ExampleObject(value = "credentialId", name = "Credential id")
                                                                             }
                                                                     ) @RequestParam(required = false, defaultValue = "createdAt") String sortColumn,
                                                                     @Parameter(name = "sortTpe", description = "Sort order", examples = { @ExampleObject(value = "desc", name = "Descending order"), @ExampleObject(value = "asc", name = "Ascending order") }) @RequestParam(required = false, defaultValue = "desc") String sortTpe) {
        final String bpn = getBpn();
        log.debug("Received request to get credentials. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.OK).body(issuersCredentialService.getCredentials(credentialId, holderIdentifier, type, sortColumn, sortTpe, pageNumber, size, bpn));
    }

    /**
     * Issue membership credential response entity.
     *
     * @param issueMembershipCredentialRequest the issue membership credential request
     * @return the response entity
     */
    @IssuersCredentialControllerApiDocs.IssueMembershipCredentialApiDoc
    @PostMapping(path = RestURI.CREDENTIALS_ISSUER_MEMBERSHIP, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VerifiableCredential> issueMembershipCredential(@Valid @RequestBody IssueMembershipCredentialRequest issueMembershipCredentialRequest) {
        final String bpn = getBpn();
        log.debug("Received request to issue membership credential. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(issuersCredentialService.issueMembershipCredential(issueMembershipCredentialRequest, bpn));
    }

    /**
     * Issue dismantler credential response entity.
     *
     * @param request   the request
     * @return the response entity
     */
    @IssuersCredentialControllerApiDocs.IssueDismantlerCredentialApiDoc
    @PostMapping(path = RestURI.CREDENTIALS_ISSUER_DISMANTLER, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VerifiableCredential> issueDismantlerCredential(@Valid @RequestBody IssueDismantlerCredentialRequest request) {
        final String bpn = getBpn();
        log.debug("Received request to issue dismantler credential. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(issuersCredentialService.issueDismantlerCredential(request, bpn));
    }

   /**
     * Issue framework credential response entity.
     *
     * @param request   the request
     * @return the response entity
     */
    @IssuersCredentialControllerApiDocs.IssueFrameworkCredentialApiDocs
    @PostMapping(path = RestURI.API_CREDENTIALS_ISSUER_FRAMEWORK, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VerifiableCredential> issueFrameworkCredential(@Valid @RequestBody IssueFrameworkCredentialRequest request) {
        final String bpn = getBpn();
        log.debug("Received request to issue framework credential. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(issuersCredentialService.issueFrameworkCredential(request, bpn));
    }

    /**
     * Credentials validation response entity.
     *
     * @param data                     the data
     * @param withCredentialExpiryDate the with credential expiry date
     * @return the response entity
     */
    @PostMapping(path = RestURI.CREDENTIALS_VALIDATION, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @IssuersCredentialControllerApiDocs.ValidateVerifiableCredentialApiDocs
    public ResponseEntity<Map<String, Object>> credentialsValidation(@RequestBody Map<String, Object> data,
                                                                     @Parameter(description = "Check expiry of VC") @RequestParam(name = "withCredentialExpiryDate", defaultValue = "false", required = false) boolean withCredentialExpiryDate) {
        log.debug("Received request to validate verifiable credentials");
        return ResponseEntity.status(HttpStatus.OK).body(issuersCredentialService.credentialsValidation(data, withCredentialExpiryDate));
    }

    /**
     * Issue credential response entity.
     *
     * @param holderDid the holder did
     * @param data      the data
     * @return the response entity
     */
    @PostMapping(path = RestURI.ISSUERS_CREDENTIALS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @IssuersCredentialControllerApiDocs.IssueVerifiableCredentialUsingBaseWalletApiDocs
    public ResponseEntity<VerifiableCredential> issueCredentialUsingBaseWallet(@Parameter(description = "Holder DID", examples = {@ExampleObject(description = "did", name = "did", value = "did:web:localhost:BPNL000000000000")}) @RequestParam(name = "holderDid") String holderDid, @RequestBody Map<String, Object> data) {
        final String bpn = getBpn();
        log.debug("Received request to issue verifiable credential. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(issuersCredentialService.issueCredentialUsingBaseWallet(holderDid, data, bpn));
    }
}

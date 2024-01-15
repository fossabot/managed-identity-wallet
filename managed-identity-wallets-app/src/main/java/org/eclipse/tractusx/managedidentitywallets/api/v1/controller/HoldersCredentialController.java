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


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.apidocs.HoldersCredentialControllerApiDocs;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.RestURI;
import org.eclipse.tractusx.managedidentitywallets.api.v1.service.HoldersCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * The type Holders credential controller.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Verifiable Credential - Holder")
public class HoldersCredentialController extends BaseController {

    private final HoldersCredentialService holdersCredentialService;


    /**
     * Gets credentials.
     *
     * @param credentialId     the credential id
     * @param issuerIdentifier the issuer identifier
     * @param type             the type
     * @param sortColumn       the sort column
     * @param sortTpe          the sort tpe
     * @return the credentials
    */
    @HoldersCredentialControllerApiDocs.GetCredentialsApiDocs
    @GetMapping(path = RestURI.CREDENTIALS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<VerifiableCredential>> getCredentials(@Parameter(name = "credentialId", description = "Credential Id", examples = {@ExampleObject(name = "Credential Id", value = "did:web:localhost:BPNL000000000000#12528899-160a-48bd-ba15-f396c3959ae9")}) @RequestParam(required = false) String credentialId,
                                                                         @Parameter(name = "issuerIdentifier", description = "Issuer identifier(did of BPN)", examples = {@ExampleObject(name = "bpn", value = "BPNL000000000000", description = "bpn"), @ExampleObject(description = "did", name = "did", value = "did:web:localhost:BPNL000000000000")}) @RequestParam(required = false) String issuerIdentifier,
                                                                         @Parameter(name = "type", description = "Type of VC", examples = {@ExampleObject(name = "SummaryCredential", value = "SummaryCredential", description = "SummaryCredential"), @ExampleObject(description = "BpnCredential", name = "BpnCredential", value = "BpnCredential")}) @RequestParam(required = false) List<String> type,
                                                                         @Parameter(name = "sortColumn", description = "Sort column name",
                                                                                 examples = {
                                                                                         @ExampleObject(value = "createdAt", name = "creation date"),
                                                                                         @ExampleObject(value = "issuerDid", name = "Issuer did"),
                                                                                         @ExampleObject(value = "type", name = "Credential type"),
                                                                                         @ExampleObject(value = "credentialId", name = "Credential id"),
                                                                                         @ExampleObject(value = "selfIssued", name = "Self issued credential"),
                                                                                         @ExampleObject(value = "stored", name = "Stored credential")
                                                                                 }
                                                                         ) @RequestParam(required = false, defaultValue = "createdAt") String sortColumn,
                                                                         @Parameter(name = "sortTpe", description = "Sort order", examples = {@ExampleObject(value = "desc", name = "Descending order"), @ExampleObject(value = "asc", name = "Ascending order")}) @RequestParam(required = false, defaultValue = "desc") String sortTpe,
                                                                         @Min(0) @Max(Integer.MAX_VALUE) @Parameter(description = "Page number, Page number start with zero") @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                                         @Min(0) @Max(Integer.MAX_VALUE) @Parameter(description = "Number of records per page") @RequestParam(required = false, defaultValue = Integer.MAX_VALUE + "") int size) {
        final String bpn = getBpn();
        log.debug("Received request to get credentials. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.OK).body(holdersCredentialService.getCredentials(credentialId, issuerIdentifier, type, sortColumn, sortTpe, pageNumber, size, bpn));
    }


    /**
     * Issue credential response entity.
     *
     * @param data the data
     * @return the response entity
     */

    @HoldersCredentialControllerApiDocs.IssueCredentialApiDoc
    @PostMapping(path = RestURI.CREDENTIALS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VerifiableCredential> issueCredential(@RequestBody Map<String, Object> data) {
        final String bpn = getBpn();
        log.debug("Received request to issue credential. BPN: {}", bpn);
        return ResponseEntity.status(HttpStatus.CREATED).body(holdersCredentialService.issueCredential(data, bpn));
    }
}

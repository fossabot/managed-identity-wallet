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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.AbstractApiHandler;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.VerifiableCredentialsMapper;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class PostVerifiableCredentialAdminApiHandler extends AbstractApiHandler {

    private final VerifiableCredentialsMapper verifiableCredentialsMapper;
    private final VerifiableCredentialService verifiableCredentialService;

    public ResponseEntity<Map<String, Object>> execute(Map<String, Object> requestBody) {
        logInvocationIfDebug("createVerifiableCredential(requestBody={})", requestBody);

        if (!verifiableCredentialsMapper.isVerifiableCredential(requestBody)) {
            return ResponseEntity.badRequest().build();
        }

        final VerifiableCredential verifiableCredential = verifiableCredentialsMapper.map(requestBody);
        verifiableCredentialService.create(verifiableCredential);

        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId(verifiableCredential.getId().toString());
        final Optional<VerifiableCredential> createdVerifiableCredential = verifiableCredentialService.findById(verifiableCredentialId);
        if (createdVerifiableCredential.isPresent()) {
            final URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(verifiableCredentialId.getText())
                    .toUri();
            return ResponseEntity.created(location).body(createdVerifiableCredential.get());
        } else {
            log.error("Verifiable Credential {} was not created", verifiableCredential.getId());
            return ResponseEntity.internalServerError().build();
        }
    }

}

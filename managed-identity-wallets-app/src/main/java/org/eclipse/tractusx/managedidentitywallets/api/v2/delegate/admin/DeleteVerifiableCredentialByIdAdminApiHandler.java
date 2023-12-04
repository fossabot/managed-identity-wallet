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
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@Secured("ROLE_MIW_ADMIN")
class DeleteVerifiableCredentialByIdAdminApiHandler extends AbstractApiHandler {

    private final VerifiableCredentialService verifiableCredentialService;

    public ResponseEntity<Void> execute(String verifiableCredentialId) {
        if (log.isDebugEnabled()) {
            log.debug("deleteVerifiableCredentialById(verifiableCredentialId={})", verifiableCredentialId);
        }

        verifiableCredentialService.findById(new VerifiableCredentialId(verifiableCredentialId)).ifPresent(verifiableCredentialService::delete);
        return ResponseEntity.noContent().build();
    }

}

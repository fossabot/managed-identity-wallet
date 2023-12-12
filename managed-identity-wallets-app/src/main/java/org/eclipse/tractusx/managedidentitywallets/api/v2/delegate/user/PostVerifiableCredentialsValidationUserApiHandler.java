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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.user;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v2.delegate.AbstractApiHandler;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.ApiV2Mapper;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialValidationResult;
import org.eclipse.tractusx.managedidentitywallets.service.ValidationService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ValidateVerifiableCredentialRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ValidateVerifiableCredentialResponsePayloadV2;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class PostVerifiableCredentialsValidationUserApiHandler extends AbstractApiHandler {

    private final ValidationService validationService;
    private final ApiV2Mapper apiMapper;

    public ResponseEntity<ValidateVerifiableCredentialResponsePayloadV2> execute(@NonNull ValidateVerifiableCredentialRequestPayloadV2 validateVerifiableCredentialRequestPayloadV2) {
        final Optional<List<VerifiableCredential>> verifiableCredentials = readVerifiableCredentialArgs(validateVerifiableCredentialRequestPayloadV2.getVerifiableCredentials());
        if (verifiableCredentials.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        final VerifiableCredentialValidationResult result = validationService.validate(verifiableCredentials.get());

        return ResponseEntity.ok(apiMapper.mapValidateVerifiableCredentialResponsePayloadV2(result));
    }
}

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
import org.eclipse.tractusx.managedidentitywallets.models.VerifiablePresentationValidationResult;
import org.eclipse.tractusx.managedidentitywallets.service.ValidationService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ValidateVerifiablePresentationRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ValidateVerifiablePresentationResponsePayloadV2;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
class PostVerifiablePresentationsValidationUserApiHandler extends AbstractApiHandler {

    private final ValidationService validationService;
    private final ApiV2Mapper apiMapper;

    public ResponseEntity<ValidateVerifiablePresentationResponsePayloadV2> execute(@NonNull ValidateVerifiablePresentationRequestPayloadV2 validateVerifiableCredentialRequestPayloadV2) {
        logIfDebug("userVerifiablePresentationValidationPost(payload={})", validateVerifiableCredentialRequestPayloadV2);

        try {
            final VerifiablePresentation verifiablePresentation = new VerifiablePresentation(validateVerifiableCredentialRequestPayloadV2.getVerifiablePresentation());

            final VerifiablePresentationValidationResult result = validationService.validate(verifiablePresentation);
            return ResponseEntity.ok(apiMapper.mapValidateVerifiablePresentationResponsePayloadV2(result));
        } catch (Exception e) {
            log.info("userVerifiablePresentationValidationPost(payload={}) - Exception: {}", validateVerifiableCredentialRequestPayloadV2, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

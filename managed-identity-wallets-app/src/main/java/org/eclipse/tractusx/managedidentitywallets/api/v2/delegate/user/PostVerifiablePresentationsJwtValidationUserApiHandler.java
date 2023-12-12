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
import org.eclipse.tractusx.managedidentitywallets.models.JsonWebToken;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiablePresentationJwtValidationResult;
import org.eclipse.tractusx.managedidentitywallets.service.ValidationService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ValidateVerifiablePresentationJwtRequestPayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ValidateVerifiablePresentationJwtResponsePayloadV2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
class PostVerifiablePresentationsJwtValidationUserApiHandler extends AbstractApiHandler {

    private final ValidationService validationService;
    private final ApiV2Mapper apiMapper;

    public ResponseEntity<ValidateVerifiablePresentationJwtResponsePayloadV2> execute(@NonNull ValidateVerifiablePresentationJwtRequestPayloadV2 validateVerifiableCredentialRequestPayloadV2) {
        logIfDebug("userVerifiablePresentationJwtValidationPost(payload={})", validateVerifiableCredentialRequestPayloadV2);

        try {
            final JsonWebToken jwt = new JsonWebToken(validateVerifiableCredentialRequestPayloadV2.getJwt());
            final VerifiablePresentationJwtValidationResult result = validationService.validate(jwt);
            return ResponseEntity.ok(apiMapper.mapValidateVerifiablePresentationJwtResponsePayloadV2(result));
        } catch (Exception e) {
            log.info("userVerifiablePresentationJwtValidationPost(payload={}) - Exception: {}", validateVerifiableCredentialRequestPayloadV2, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

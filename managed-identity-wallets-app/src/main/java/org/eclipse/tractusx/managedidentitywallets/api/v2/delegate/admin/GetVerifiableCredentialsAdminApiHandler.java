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
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.ApiV2Mapper;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.VerifiableCredentialListResponsePayloadV2;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
class GetVerifiableCredentialsAdminApiHandler extends AbstractApiHandler {

    private final VerifiableCredentialService verifiableCredentialService;
    private final MIWSettings miwSettings;
    private final ApiV2Mapper apiMapper;

    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> execute
            (Integer page, Integer perPage, String id, String type, String issuer, String holder) {

        logIfDebug("getWallets(page={}, perPage={})", page, perPage);

        page = Optional.ofNullable(page).orElse(0);
        perPage = Optional.ofNullable(perPage).orElse(miwSettings.getApiDefaultPageSize());

        final VerifiableCredentialQuery.VerifiableCredentialQueryBuilder builder = VerifiableCredentialQuery.builder();
        Optional.ofNullable(issuer).map(VerifiableCredentialIssuer::new).ifPresent(builder::verifiableCredentialIssuer);
        Optional.ofNullable(id).map(VerifiableCredentialId::new).ifPresent(builder::verifiableCredentialId);
        Optional.ofNullable(type).map(VerifiableCredentialType::new).map(List::of).ifPresent(builder::verifiableCredentialTypes);
        Optional.ofNullable(holder).map(WalletId::new).ifPresent(builder::holderWalletId);

        final Page<VerifiableCredential> verifiableCredentials = verifiableCredentialService.findAll(builder.build(), page, perPage);
        final VerifiableCredentialListResponsePayloadV2 payload = apiMapper.mapVerifiableCredentialListResponsePayloadV2(verifiableCredentials);

        return ResponseEntity.ok(payload);
    }

}

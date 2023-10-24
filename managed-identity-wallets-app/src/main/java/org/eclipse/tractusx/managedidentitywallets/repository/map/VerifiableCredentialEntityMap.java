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

package org.eclipse.tractusx.managedidentitywallets.repository.map;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.exceptions.MappingException;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor
public class VerifiableCredentialEntityMap extends AbstractMap<VerifiableCredential, VerifiableCredentialEntity> {

    @Override
    public VerifiableCredential map(@NonNull VerifiableCredentialEntity entity) throws MappingException {
        final String json = entity.getJson();
        try {
            return new VerifiableCredential(MAPPER.readValue(json, Map.class));
        } catch (JsonProcessingException e) {
            throw new MappingException("Could not deserialize VerifiableCredential JSON", e);
        }
    }
}

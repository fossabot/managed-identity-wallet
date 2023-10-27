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

package org.eclipse.tractusx.managedidentitywallets.v2.map;

import lombok.NonNull;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidatorImpl;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VerifiableCredentialsMapper {

    public boolean isVerifiableCredential(@NonNull Map<String, Object> vcMap) {
        try {
            JsonLdValidatorImpl jsonLdValidator = new JsonLdValidatorImpl();

            new VerifiableCredential(vcMap);
            return true;
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
    }

    public VerifiableCredential map(@NonNull Map<String, Object> vcMap) {
        return new VerifiableCredential(vcMap);
    }
}

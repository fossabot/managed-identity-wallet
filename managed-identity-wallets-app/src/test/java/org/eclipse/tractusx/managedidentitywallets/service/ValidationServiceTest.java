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

package org.eclipse.tractusx.managedidentitywallets.service;

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.MembershipVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.models.JsonWebToken;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialValidationResult;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiablePresentationJwtValidationResult;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiablePresentationValidationResult;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ValidationServiceTest extends MiwTestCase {

    @Autowired
    private ValidationService validationService;

    @Test
    public void testVerifiableCredentialValidation() {
        final VerifiableCredential verifiableCredential = newWalletPlusVerifiableCredentialPersisted();

        final VerifiableCredentialValidationResult result = validationService.validate(List.of(verifiableCredential));

        Assertions.assertTrue(result.isValid(), "VerifiableCredential should succeed. " + result);
    }

    @SneakyThrows
    @Test
    public void testVerifiablePresentationJwtValidation() {
        final VerifiablePresentation verifiablePresentation = newWalletPlusVerifiablePresentationPersisted();

        final VerifiablePresentationValidationResult result = validationService.validate(verifiablePresentation);

        Assertions.assertTrue(result.isValid(), "VerifiablePresentation validation should succeed. " + result);
    }

    @SneakyThrows
    @Test
    public void testVerifiablePresentationValidation() {
        final JsonWebToken jwt = newWalletPlusVerifiablePresentationJwtPersisted();

        final VerifiablePresentationJwtValidationResult result = validationService.validate(jwt);

        Assertions.assertTrue(result.isValid(), "VerifiablePresentation validation should succeed. " + result);
    }
}

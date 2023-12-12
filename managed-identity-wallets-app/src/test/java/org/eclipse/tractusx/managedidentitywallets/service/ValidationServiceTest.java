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
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

public class ValidationServiceTest extends MiwTestCase {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Test
    public void testVerifiableCredentialValidation() {
        final VerifiableCredential verifiableCredential = persistenceUtil.newWalletPlusVerifiableCredentialPersisted();

        final VerifiableCredentialValidationResult result = validationService.validate(List.of(verifiableCredential));

        Assertions.assertTrue(result.isValid(), "VerifiableCredential should succeed. " + result);
    }

    @SneakyThrows
    @Test
    public void testVerifiablePresentationValidation() {
        final VerifiablePresentation verifiablePresentation = persistenceUtil.newWalletPlusVerifiablePresentationPersisted();

        final VerifiablePresentationValidationResult result = validationService.validate(verifiablePresentation);

        Assertions.assertTrue(result.isValid(), "VerifiablePresentation validation should succeed. " + result);
    }

    @SneakyThrows
    @Test
    public void testVerifiablePresentationJwtValidation() {
        final JsonWebToken jwt = persistenceUtil.newWalletPlusVerifiablePresentationJwtPersisted();

        final VerifiablePresentationJwtValidationResult result = validationService.validate(jwt);

        Assertions.assertTrue(result.isValid(), "VerifiablePresentation validation should succeed. " + result);
    }

    @SneakyThrows
    @Test
    public void testVerifiablePresentationJwtValidationWithExpiredVerifiableCredential() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();
        final VerifiableCredential verifiableCredential = persistenceUtil.newVerifiableCredential(wallet, Instant.now().minusSeconds(5));
        final VerifiablePresentation verifiablePresentation = persistenceUtil.newVerifiablePresentation(wallet, verifiableCredential);

        final VerifiablePresentationValidationResult result = validationService.validate(verifiablePresentation);

        Assertions.assertFalse(result.isValid(), "VerifiablePresentation validation should not succeed. " + result);
        Assertions.assertTrue(result.getVerifiableCredentialResult().getVerifiableCredentialViolations().stream().anyMatch(violation -> violation.getTypes().stream()
                .anyMatch(t -> t.equals(VerifiableCredentialValidationResultViolation.Type.EXPIRED))), "Must contain expired verifiable credential violation. " + result);
    }

}

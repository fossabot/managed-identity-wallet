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

package org.eclipse.tractusx.managedidentitywallets.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.service.ValidationService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidator;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidatorImpl;

import java.lang.annotation.*;
import java.util.List;

@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IsJsonLdValid.VerifiableCredentialValidator.class, IsJsonLdValid.VerifiableCredentialsValidator.class})
public @interface IsJsonLdValid {
    String message() default "Verifiable Credential not JSON-LD valid";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    @Slf4j
    @RequiredArgsConstructor
    final class VerifiableCredentialValidator
            implements ConstraintValidator<IsJsonLdValid, VerifiableCredential> {

        private final ValidationService validationService;

        @Override
        public boolean isValid(VerifiableCredential verifiableCredential, ConstraintValidatorContext context) {
            return verifiableCredential != null && validationService.isJsonLdValid(verifiableCredential);
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    final class VerifiableCredentialsValidator
            implements ConstraintValidator<IsJsonLdValid, List<VerifiableCredential>> {

        private final ValidationService validationService;

        @Override
        public boolean isValid(List<VerifiableCredential> verifiableCredentials, ConstraintValidatorContext context) {
            return verifiableCredentials != null && validationService.isJsonLdValid(verifiableCredentials);
        }
    }
}

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
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IsSignatureValid.VerifiableCredentialValidator.class})
public @interface IsSignatureValid {
    String message() default "Verifiable Credential signature not valid";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    @Slf4j
    @RequiredArgsConstructor
    final class VerifiableCredentialValidator
            implements ConstraintValidator<IsSignatureValid, VerifiableCredential> {

        private final LinkedDataProofValidation proofValidation;

        @Override
        public boolean isValid(VerifiableCredential verifiableCredential, ConstraintValidatorContext context) {
            if (verifiableCredential == null)
                return false;

            boolean isProofValid = false;

            try {
                isProofValid = proofValidation.verifiy(verifiableCredential);
            } catch (Exception e) {
                // if a verifiable credential is not json-ld valid, the verify method will throw an exception
                if (log.isTraceEnabled()) {
                    log.error("Verifiable Credential signature validation failed (verifiable credential id: {})", verifiableCredential.getId(), e);
                }
            }

            if (log.isTraceEnabled()) {
                log.trace("Verifiable Credential signature validation result: {} (verifiable credential id: {})", isProofValid, verifiableCredential.getId());
            }
            return isProofValid;
        }
    }
}

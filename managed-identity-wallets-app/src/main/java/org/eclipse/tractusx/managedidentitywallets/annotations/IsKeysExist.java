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
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;

import java.lang.annotation.*;


@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IsKeysExist.KeysExistValidator.class})
// TODO Extend functionality to "IsKeyValid"
public @interface IsKeysExist {
    String message() default "Wallet key(s) not found in vault";

    Class<?>[] groups() default {};

    Class<?>[] payload() default {};

    @Slf4j
    @RequiredArgsConstructor
    final class KeysExistValidator
            implements ConstraintValidator<IsSignatureValid, Wallet> {
        private final VaultService vaultService;

        @Override
        public boolean isValid(Wallet wallet, ConstraintValidatorContext context) {
            return wallet.getStoredEd25519Keys().stream().allMatch(key -> vaultService.resolveKey(key).isPresent());
        }
    }
}

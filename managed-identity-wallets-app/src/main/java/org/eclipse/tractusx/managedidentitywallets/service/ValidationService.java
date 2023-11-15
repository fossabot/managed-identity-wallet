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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.ssi.lib.model.verifiable.Verifiable;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final LinkedDataProofValidation proofValidation;
    private final JsonLdValidator jsonLdValidator;

    public VerifiablePresentationValidationResult validate(VerifiablePresentation verifiablePresentation) {
        final List<VerifiablePresentationValidationResult.Type> violations = new ArrayList<>();

        if (isExpired(verifiablePresentation)) {
            violations.add(VerifiablePresentationValidationResult.Type.EXPIRED);
        }
        if (!isJsonLdValid(verifiablePresentation)) {
            violations.add(VerifiablePresentationValidationResult.Type.INVALID_JSONLD_FORMAT);
        }
        if (!isSignatureValid(verifiablePresentation)) {
            violations.add(VerifiablePresentationValidationResult.Type.INVALID_SIGNATURE);
        }

        final VerifiableCredentialValidationResult validationResult = validate(verifiablePresentation.getVerifiableCredentials());

        return VerifiablePresentationValidationResult.builder()
                .verifiablePresentationViolations(violations)
                .verifiableCredentialViolations(validationResult.getVerifiableCredentialViolations())
                .isValid(violations.isEmpty() && validationResult.isValid())
                .build();
    }

    public VerifiableCredentialValidationResult validate(List<VerifiableCredential> verifiableCredentials) {
        final List<VerifiableCredentialValidationResultViolation> violations = new ArrayList<>();


        for (final VerifiableCredential verifiableCredential : verifiableCredentials) {
            final VerifiableCredentialId id = new VerifiableCredentialId(verifiableCredential.getId().toString());
            final List<VerifiableCredentialValidationResultViolation.Type> types = new ArrayList<>();

            if (isExpired(verifiableCredential)) {
                types.add(VerifiableCredentialValidationResultViolation.Type.EXPIRED);
            }
            if (!isJsonLdValid(verifiableCredential)) {
                types.add(VerifiableCredentialValidationResultViolation.Type.INVALID_JSONLD_FORMAT);
            }
            if (!hasSignature(verifiableCredential)) {
                types.add(VerifiableCredentialValidationResultViolation.Type.NO_EMBEDDED_SIGNATURE);
            } else if (!isSignatureValid(verifiableCredential)) {
                types.add(VerifiableCredentialValidationResultViolation.Type.INVALID_SIGNATURE);
            }

            if (!types.isEmpty()) {
                violations.add(new VerifiableCredentialValidationResultViolation(id, types));
            }
        }

        return VerifiableCredentialValidationResult.builder()
                .isValid(violations.isEmpty())
                .verifiableCredentialViolations(violations)
                .build();
    }

    public boolean isExpired(List<VerifiableCredential> verifiableCredentials) {
        return verifiableCredentials.stream().allMatch(this::isExpired);
    }

    public boolean isExpired(VerifiableCredential verifiableCredential) {
        if (verifiableCredential.getExpirationDate() == null) {
            return false;
        }

        boolean isExpired = verifiableCredential.getExpirationDate().isBefore(Instant.now());
        if (log.isTraceEnabled()) {
            log.trace(isExpired ? "VerifiableCredential is expired. (id={})" : "VerifiableCredential is not expired. (id={})", verifiableCredential.getId());
        }
        return isExpired;
    }


    public boolean isExpired(VerifiablePresentation presentation) {

        // TODO Create Library Contribution, which adds the expiration date to the VerifiablePresentation
        final Object expirationDate = presentation.get(VerifiableCredential.EXPIRATION_DATE);
        if (expirationDate == null) {
            return false;
        }

        boolean isExpired = Instant.parse((String) expirationDate).isBefore(Instant.now());

        if (log.isTraceEnabled()) {
            log.trace(isExpired ? "VerifiablePresentation is expired. (id={})" : "VerifiablePresentation is not expired. (id={})", presentation.getId());
        }
        return isExpired;
    }

    public boolean isJsonLdValid(List<VerifiableCredential> verifiableCredentials) {
        return verifiableCredentials.stream().allMatch(this::isJsonLdValid);
    }

    public boolean isJsonLdValid(VerifiableCredential verifiableCredential) {
        boolean result;
        try {
            jsonLdValidator.validate(verifiableCredential);
            result = true;
        } catch (Exception e) {
            result = false;
        }

        if (log.isTraceEnabled()) {
            log.trace(result ? "VerifiableCredential is JSON-LD valid. (id={})" : "VerifiableCredential is not JSON-LD valid. (id={})", verifiableCredential.getId());
        }

        return result;
    }


    public boolean isJsonLdValid(VerifiablePresentation presentation) {
        boolean result;
        try {
            jsonLdValidator.validate(presentation);
            result = true;
        } catch (Exception e) {
            result = false;
        }


        if (log.isTraceEnabled()) {
            log.trace(result ? "VerifiablePresentation is JSON-LD valid. (id={})" : "VerifiablePresentation is not JSON-LD valid. (id={})", presentation.getId());
        }

        return result;
    }

    public <T extends Verifiable> boolean isSignatureValid(List<T> verifiableCredentials) {
        return verifiableCredentials.stream().allMatch(this::isSignatureValid);
    }

    public boolean hasSignature(List<VerifiableCredential> verifiableCredentials) {
        return verifiableCredentials.stream().allMatch(this::hasSignature);
    }

    public boolean hasSignature(VerifiableCredential verifiableCredential) {
        return verifiableCredential.getProof() != null &&
                verifiableCredential.getProof().getType() != null &&
                !verifiableCredential.getProof().getType().isBlank();
    }

    public boolean isSignatureValid(Verifiable verifiableCredential) {
        boolean isSignatureValid = false;

        try {
            isSignatureValid = proofValidation.verifiy(verifiableCredential);
        } catch (Exception e) {
            // if a verifiable credential is not json-ld valid, the signature verification is not possible and will throw an exception
            // which results in 'isSignatureValid=false', which is somewhat true.
        }

        if (log.isTraceEnabled()) {
            log.trace(isSignatureValid ? "VerifiableCredential signature is valid. (id={})" : "VerifiableCredential signature is not valid. (id={})", verifiableCredential.getId());
        }

        return isSignatureValid;
    }
}

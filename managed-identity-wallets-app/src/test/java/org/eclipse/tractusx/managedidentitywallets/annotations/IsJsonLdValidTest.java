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

import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.service.ValidationService;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class IsJsonLdValidTest {

    @Mock
    private ValidationService validationService;

    private IsJsonLdValid.VerifiableCredentialValidator singleValidator;
    private IsJsonLdValid.VerifiableCredentialsValidator listValidator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        singleValidator = new IsJsonLdValid.VerifiableCredentialValidator(validationService);
        listValidator = new IsJsonLdValid.VerifiableCredentialsValidator(validationService);
    }

    @Test
    public void singleCredentialIsValidWhenJsonLdValid() {
        VerifiableCredential credential = createValidVerifiableCredential();
        when(validationService.isJsonLdValid(credential)).thenReturn(true);

        assertTrue(singleValidator.isValid(credential, null));
    }

    @Test
    public void singleCredentialIsInvalidWhenJsonLdInvalid() {
        VerifiableCredential credential = createInvalidVerifiableCredential();
        when(validationService.isJsonLdValid(credential)).thenReturn(false);

        assertFalse(singleValidator.isValid(credential, null));
    }

    @Test
    public void singleCredentialIsInvalidWhenNull() {
        assertFalse(singleValidator.isValid(null, null));
    }

    @Test
    public void credentialListIsValidWhenAllJsonLdValid() {
        VerifiableCredential credential1 = createValidVerifiableCredential();
        VerifiableCredential credential2 = createValidVerifiableCredential();
        List<VerifiableCredential> credentials = Arrays.asList(credential1, credential2);

        when(validationService.isJsonLdValid(List.of(credential1, credential2))).thenReturn(true);

        assertTrue(listValidator.isValid(credentials, null));
    }

    @Test
    public void credentialListIsInvalidWhenAnyJsonLdInvalid() {
        VerifiableCredential credential1 = createValidVerifiableCredential();
        VerifiableCredential credential2 = createInvalidVerifiableCredential();
        List<VerifiableCredential> credentials = Arrays.asList(credential1, credential2);
        when(validationService.isJsonLdValid(credential1)).thenReturn(true);
        when(validationService.isJsonLdValid(credential2)).thenReturn(false);

        assertFalse(listValidator.isValid(credentials, null));
    }

    @Test
    public void credentialListIsInvalidWhenNull() {
        assertFalse(listValidator.isValid(null, null));
    }

    private VerifiableCredential createInvalidVerifiableCredential() {
        return createVerifiableCredential(Collections.emptyList());
    }

    private VerifiableCredential createValidVerifiableCredential() {
        return createVerifiableCredential(List.of(VerifiableCredential.DEFAULT_CONTEXT));
    }

    private VerifiableCredential createVerifiableCredential(@NonNull List<URI> contexts) {
        final URI issuer = URI.create("did:test:" + UUID.randomUUID());
        return new VerifiableCredentialBuilder()
                .context(contexts)
                .type(List.of("VerifiableCredential"))
                .issuanceDate(Instant.now())
                .expirationDate(Instant.now().plusSeconds(60))
                .issuer(issuer)
                .id(URI.create(issuer + "#" + UUID.randomUUID()))
                .credentialSubject(new VerifiableCredentialSubject(Map.of("id", "foo")))
                .build();
    }
}
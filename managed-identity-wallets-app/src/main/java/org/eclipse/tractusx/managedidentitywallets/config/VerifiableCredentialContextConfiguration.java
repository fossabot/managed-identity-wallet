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

package org.eclipse.tractusx.managedidentitywallets.config;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

@Getter
public class VerifiableCredentialContextConfiguration {

    @Value("${miw.verifiable.credential.contexts.useEmbeddedContexts}")
    private boolean useEmbeddedContexts;

    @NonNull
    @Value("${miw.verifiable.credential.contexts.businessPartnerNumberCredential}")
    private URI businessPartnerNumberVerifiableCredentialContext;

    @NonNull
    @Value("${miw.verifiable.credential.contexts.dismantlerCredential}")
    private URI dismantlerVerifiableCredentialContext;

    @NonNull
    @Value("${miw.verifiable.credential.contexts.summaryCredential}")
    private URI summaryVerifiableCredentialContext;

    @NonNull
    @Value("${miw.verifiable.credential.contexts.frameworkCredential}")
    private URI frameworkVerifiableCredentialContext;

    @NonNull
    @Value("${miw.verifiable.credential.contexts.membershipCredential}")
    private URI membershipVerifiableCredentialContext;
}
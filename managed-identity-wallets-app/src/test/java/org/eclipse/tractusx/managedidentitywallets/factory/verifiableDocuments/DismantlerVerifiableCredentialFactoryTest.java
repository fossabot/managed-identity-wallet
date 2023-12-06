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

package org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments;

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.config.VerifiableCredentialContextConfiguration;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.test.MiwTestCase;
import org.eclipse.tractusx.managedidentitywallets.test.util.TestPersistenceUtil;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DismantlerVerifiableCredentialFactoryTest extends MiwTestCase {

    private static final String ACTIVITY_TYPE = "recycle";

    @Autowired
    private TestPersistenceUtil persistenceUtil;

    @Autowired
    public DismantlerVerifiableCredentialFactory factory;

    @Autowired
    public JsonLdValidator jsonLdValidator;

    @Autowired
    public VerifiableCredentialContextConfiguration verifiableCredentialContextConfiguration;

    @Test
    public void testContextSet() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();

        final VerifiableCredential verifiableCredential = factory.createDismantlerVerifiableCredential(wallet, ACTIVITY_TYPE);

        final boolean containsBpnContext =
                verifiableCredential.getContext()
                        .contains(verifiableCredentialContextConfiguration.getDismantlerVerifiableCredentialContext());

        Assertions.assertTrue(containsBpnContext);
    }

    @Test
    @SneakyThrows
    public void testJsonLdCompliant() {
        final Wallet wallet = persistenceUtil.newWalletPersisted();

        final VerifiableCredential verifiableCredential = factory.createDismantlerVerifiableCredential(wallet, ACTIVITY_TYPE);

        System.out.println(verifiableCredential.toPrettyJson());

        Assertions.assertDoesNotThrow(() ->
                jsonLdValidator.validate(verifiableCredential));
    }
}

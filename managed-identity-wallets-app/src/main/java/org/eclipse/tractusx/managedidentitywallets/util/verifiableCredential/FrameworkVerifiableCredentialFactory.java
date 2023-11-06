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

package org.eclipse.tractusx.managedidentitywallets.util.verifiableCredential;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.util.DidFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FrameworkVerifiableCredentialFactory extends AbstractVerifiableCredentialFactory {

    private final DidFactory didFactory;

    public VerifiableCredential createDismantlerVerifiableCredential(@NonNull Wallet wallet,
                                                                     @NonNull VerifiableCredentialType verifiableCredentialType,
                                                                     @NonNull String contractTemplate,
                                                                     @NonNull String contractVersion) {
        final WalletId walletId = wallet.getWalletId();
        final Did did = didFactory.generateDid(wallet);

        final VerifiableCredentialSubject verifiableCredentialSubject = new VerifiableCredentialSubject(Map.of(
                StringPool.TYPE, verifiableCredentialType.getText(),
                StringPool.ID, did.toString(),
                StringPool.HOLDER_IDENTIFIER, walletId.getText(),
                StringPool.CONTRACT_TEMPLATE, contractTemplate,
                StringPool.CONTRACT_VERSION, contractVersion));

        return createdIssuedCredential(verifiableCredentialSubject, verifiableCredentialType.getText());
    }
}

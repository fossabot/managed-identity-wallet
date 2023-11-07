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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DismantlerVerifiableCredentialFactory extends AbstractVerifiableDocumentFactory {

    private final DidFactory didFactory;

    public VerifiableCredential createDismantlerVerifiableCredential(@NonNull Wallet wallet, @NonNull String activityType) {
        return createDismantlerVerifiableCredential(wallet, activityType, Collections.emptyList());
    }

    public VerifiableCredential createDismantlerVerifiableCredential(@NonNull Wallet wallet, @NonNull String activityType, @NonNull List<String> allowedVehicleBrands) {

        final WalletId walletId = wallet.getWalletId();
        final Did did = didFactory.generateDid(wallet);

        VerifiableCredentialSubject verifiableCredentialSubject = new VerifiableCredentialSubject(Map.of(
                StringPool.TYPE, MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL,
                StringPool.ID, did.toString(),
                StringPool.HOLDER_IDENTIFIER, walletId.getText(),
                StringPool.ACTIVITY_TYPE, activityType,
                StringPool.ALLOWED_VEHICLE_BRANDS, allowedVehicleBrands));

        return createdIssuedCredential(verifiableCredentialSubject, MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL);
    }
}

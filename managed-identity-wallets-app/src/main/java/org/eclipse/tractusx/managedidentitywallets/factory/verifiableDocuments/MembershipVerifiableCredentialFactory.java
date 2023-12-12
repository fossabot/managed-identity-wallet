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
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.config.VerifiableCredentialContextConfiguration;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.factory.DidFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MembershipVerifiableCredentialFactory extends AbstractVerifiableDocumentFactory {

    private final DidFactory didFactory;
    private final MIWSettings miwSettings;
    private final WalletService walletService;
    private final VerifiableCredentialContextConfiguration verifiableCredentialContextConfiguration;

    public VerifiableCredential createMembershipVerifiableCredential(@NonNull Wallet wallet) {

        final WalletId newMemberWalletId = wallet.getWalletId();
        final WalletId issuerWalletId = new WalletId(miwSettings.getAuthorityWalletBpn());
        final Wallet issuerWallet = walletService.findById(issuerWalletId).orElseThrow(() -> new RuntimeException("Issuer wallet not found"));
        final Did did = didFactory.generateDid(wallet);

        final VerifiableCredentialSubject verifiableCredentialSubject = new VerifiableCredentialSubject(Map.of(
                StringPool.TYPE, VerifiableCredentialType.MEMBERSHIP_CREDENTIAL,
                StringPool.ID, did.toString(),
                StringPool.HOLDER_IDENTIFIER, newMemberWalletId.getText(),
                StringPool.MEMBER_OF, issuerWallet.getWalletName().getText(),
                StringPool.STATUS, "Active",
                StringPool.START_TIME, Instant.now().toString()));

        final URI context = verifiableCredentialContextConfiguration.getMembershipVerifiableCredentialContext();
        return createdIssuedCredential(verifiableCredentialSubject, MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL, List.of(context));
    }

    public VerifiableCredential createMembershipVerifiableCredential(@NonNull Wallet wallet, OffsetDateTime expirationDate) {

        final WalletId newMemberWalletId = wallet.getWalletId();
        final WalletId issuerWalletId = new WalletId(miwSettings.getAuthorityWalletBpn());
        final Wallet issuerWallet = walletService.findById(issuerWalletId).orElseThrow(() -> new RuntimeException("Issuer wallet not found"));
        final Did did = didFactory.generateDid(wallet);

        final VerifiableCredentialSubject verifiableCredentialSubject = new VerifiableCredentialSubject(Map.of(
                StringPool.TYPE, VerifiableCredentialType.MEMBERSHIP_CREDENTIAL,
                StringPool.ID, did.toString(),
                StringPool.HOLDER_IDENTIFIER, newMemberWalletId.getText(),
                StringPool.MEMBER_OF, issuerWallet.getWalletName().getText(),
                StringPool.STATUS, "Active",
                StringPool.START_TIME, Instant.now().toString()));

        final URI context = verifiableCredentialContextConfiguration.getMembershipVerifiableCredentialContext();
        return createdIssuedCredential(verifiableCredentialSubject, MIWVerifiableCredentialType.MEMBERSHIP_CREDENTIAL, List.of(context), expirationDate.toInstant());
    }
}

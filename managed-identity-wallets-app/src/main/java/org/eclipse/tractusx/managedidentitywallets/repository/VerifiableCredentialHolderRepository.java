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

package org.eclipse.tractusx.managedidentitywallets.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifiableCredentialHolderRepository {

    private final VerifiableCredentialRepository verifiableCredentialRepository;
    private final WalletRepository walletRepository;

    private final VerifiableCredentialWalletIntersectionJpaRepository verifiableCredentialWalletIntersectionJpaRepository;

    public void storeVerifiableCredentialWalletIntersection(@NonNull Wallet wallet, @NonNull VerifiableCredential verifiableCredential) {
        throwIfVerifiableCredentialNotFound(verifiableCredential);
        throwIfWalletNotFound(wallet);


    }

    private void throwIfVerifiableCredentialNotFound(VerifiableCredential verifiableCredential) {
        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId(verifiableCredential.getId().toString());
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(verifiableCredentialId)
                .build();
        final boolean verifiableCredentialExists = verifiableCredentialRepository.exists(verifiableCredentialQuery);
        if (!verifiableCredentialExists) {
            throw new VerifiableCredentialNotFoundException(verifiableCredentialId);
        }
    }

    private void throwIfWalletNotFound(Wallet wallet) {
        final WalletId walletId = new WalletId(wallet.getWalletId().toString());
        final WalletQuery walletQuery = WalletQuery.builder()
                .walletId(walletId)
                .build();
        final boolean walletExists = walletRepository.exists(walletQuery);
        if (!walletExists) {
            throw new WalletNotFoundException(wallet.getWalletId());
        }
    }
}

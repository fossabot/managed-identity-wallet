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

package org.eclipse.tractusx.managedidentitywallets.repository.repository;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotExistsException;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialIntersectionEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerifiableCredentialRepository {

    private final WalletJpaRepository walletJpaRepository;
    private final VerifiableCredentialJpaRepository verifiableCredentialJpaRepository;
    private final VerifiableCredentialIntersectionJpaRepository verifiableCredentialIntersectionJpaRepository;
    private final VerifiableCredentialIssuerIntersectionJpaRepository verifiableCredentialIssuerIntersectionJpaRepository;

    @Transactional
    public void save(VerifiableCredential vc, String walletName)
            throws WalletNotExistsException {
        final WalletEntity walletEntity = walletJpaRepository.findByName(walletName)
                .orElseThrow(() -> new WalletNotExistsException(walletName));

        // Verifiable Credential
        final VerifiableCredentialEntity verifiableCredentialEntity = new VerifiableCredentialEntity();
        verifiableCredentialEntity.setId(UUID.randomUUID().toString());
        verifiableCredentialEntity.setJson(vc.toJson());
        verifiableCredentialJpaRepository.save(verifiableCredentialEntity);

        // Verifiable Credential - Wallet Intersection
        final VerifiableCredentialIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = new VerifiableCredentialIntersectionEntity.VerifiableCredentialIntersectionEntityId();
        verifiableCredentialIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIntersectionEntityId.setWallet(walletEntity);
        final VerifiableCredentialIntersectionEntity verifiableCredentialIntersectionEntity = new VerifiableCredentialIntersectionEntity();
        verifiableCredentialIntersectionEntity.setId(verifiableCredentialIntersectionEntityId);
        verifiableCredentialIntersectionJpaRepository.save(verifiableCredentialIntersectionEntity);

        // Verifiable Credential - Issuer Intersection
    }

    public Page<VerifiableCredential> getCredentialsByIssuer(String issuer) {
        return null; // TODO
    }
}

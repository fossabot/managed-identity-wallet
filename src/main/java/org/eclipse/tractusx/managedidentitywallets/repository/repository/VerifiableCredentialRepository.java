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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.*;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class VerifiableCredentialRepository {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WalletJpaRepository walletJpaRepository;
    private final VerifiableCredentialJpaRepository verifiableCredentialJpaRepository;
    private final VerifiableCredentialIntersectionJpaRepository verifiableCredentialIntersectionJpaRepository;
    private final VerifiableCredentialIssuerIntersectionJpaRepository verifiableCredentialIssuerIntersectionJpaRepository;
    private final VerifiableCredentialTypeIntersectionJpaRepository verifiableCredentialTypeIntersectionJpaRepository;

    @Transactional
    public void save(VerifiableCredential vc, String walletName) {
        final WalletEntity walletEntity = walletJpaRepository.findByName(walletName)
                .orElseThrow(() -> new RuntimeException()); // TODO

        // Verifiable Credential
        final VerifiableCredentialEntity verifiableCredentialEntity = new VerifiableCredentialEntity();
        verifiableCredentialEntity.setId(UUID.randomUUID().toString());
        verifiableCredentialEntity.setJson(vc.toJson());
        if (!verifiableCredentialJpaRepository.existsById(verifiableCredentialEntity.getId()))
            verifiableCredentialJpaRepository.save(verifiableCredentialEntity);

        // Verifiable Credential - Wallet Intersection
        final VerifiableCredentialIntersectionEntity.VerifiableCredentialIntersectionEntityId verifiableCredentialIntersectionEntityId = new VerifiableCredentialIntersectionEntity.VerifiableCredentialIntersectionEntityId();
        verifiableCredentialIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIntersectionEntityId.setWallet(walletEntity);
        final VerifiableCredentialIntersectionEntity verifiableCredentialIntersectionEntity = new VerifiableCredentialIntersectionEntity();
        verifiableCredentialIntersectionEntity.setId(verifiableCredentialIntersectionEntityId);
        if (!verifiableCredentialIntersectionJpaRepository.existsById(verifiableCredentialIntersectionEntityId))
            verifiableCredentialIntersectionJpaRepository.save(verifiableCredentialIntersectionEntity);

        // Verifiable Credential - Issuer Intersection
        final VerifiableCredentialIssuerEntity verifiableCredentialIssuerEntity = new VerifiableCredentialIssuerEntity();
        verifiableCredentialIssuerEntity.setIssuer(vc.getIssuer().toString());
        final VerifiableCredentialIssuerIntersectionEntity.VerifiableCredentialIssuerIntersectionEntityId verifiableCredentialIssuerIntersectionEntityId = new VerifiableCredentialIssuerIntersectionEntity.VerifiableCredentialIssuerIntersectionEntityId();
        verifiableCredentialIssuerIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
        verifiableCredentialIssuerIntersectionEntityId.setVerifiableCredentialIssuer(verifiableCredentialIssuerEntity);
        final VerifiableCredentialIssuerIntersectionEntity verifiableCredentialIssuerIntersectionEntity = new VerifiableCredentialIssuerIntersectionEntity();
        verifiableCredentialIssuerIntersectionEntity.setId(verifiableCredentialIssuerIntersectionEntityId);
        if (!verifiableCredentialIssuerIntersectionJpaRepository.existsById(verifiableCredentialIssuerIntersectionEntityId))
            verifiableCredentialIssuerIntersectionJpaRepository.save(verifiableCredentialIssuerIntersectionEntity);

        // Verifiable Credential - Type Intersection
        for (final String type : vc.getTypes()) {
            final VerifiableCredentialTypeEntity verifiableCredentialType = new VerifiableCredentialTypeEntity();
            verifiableCredentialType.setType(type);
            final VerifiableCredentialTypeIntersectionEntity.VerifiableCredentialTypeIntersectionEntityId verifiableCredentialTypeIntersectionEntityId = new VerifiableCredentialTypeIntersectionEntity.VerifiableCredentialTypeIntersectionEntityId();
            verifiableCredentialTypeIntersectionEntityId.setVerifiableCredential(verifiableCredentialEntity);
            verifiableCredentialTypeIntersectionEntityId.setVerifiableCredentialType(verifiableCredentialType);
            final VerifiableCredentialTypeIntersectionEntity verifiableCredentialTypeIntersectionEntity = new VerifiableCredentialTypeIntersectionEntity();
            verifiableCredentialTypeIntersectionEntity.setId(verifiableCredentialTypeIntersectionEntityId);
            if (!verifiableCredentialTypeIntersectionJpaRepository.existsById(verifiableCredentialTypeIntersectionEntityId))
                verifiableCredentialTypeIntersectionJpaRepository.save(verifiableCredentialTypeIntersectionEntity);
        }
    }


    public Optional<VerifiableCredential> findByHolderAndId(String walletOwner, String id) {
        return verifiableCredentialJpaRepository
                .findByIdAndWalletIntersections_Wallet_Id(id, walletOwner)
                .map(VerifiableCredentialEntity::getJson)
                .map(json -> {
                    try {
                        return MAPPER.readValue(json, Map.class);
                    } catch (JsonProcessingException e) {
                        log.error("Could not deserialize VerifiableCredential JSON", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(VerifiableCredential::new);
    }

    public Page<VerifiableCredential> findByIssuer(String issuer, Pageable p) {
        return null; // TODO
    }
}

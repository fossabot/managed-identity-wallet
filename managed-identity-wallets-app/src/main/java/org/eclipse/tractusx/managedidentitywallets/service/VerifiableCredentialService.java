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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.annotations.IsJsonLdValid;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialCreatedEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialCreatingEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialDeletedEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialDeletingEvent;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class VerifiableCredentialService {

    private final VerifiableCredentialRepository verifiableCredentialRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public Optional<VerifiableCredential> findById(@NonNull VerifiableCredentialId id) {
        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(id)
                .build();
        return verifiableCredentialRepository.findOne(query);
    }

    public Page<VerifiableCredential> findAll(int page, int size) {
        final VerifiableCredentialQuery query = VerifiableCredentialQuery.builder().build();
        return findAll(query, page, size);
    }

    public Page<VerifiableCredential> findAll(@NonNull VerifiableCredentialQuery query, int page, int size) {
        final Pageable pageable = Pageable.ofSize(size).withPage(page);
        return verifiableCredentialRepository.findAll(query, pageable);
    }

    public void create(@NonNull @IsJsonLdValid VerifiableCredential verifiableCredential) {
        applicationEventPublisher.publishEvent(new VerifiableCredentialCreatingEvent(verifiableCredential));
        verifiableCredentialRepository.create(verifiableCredential);
        afterCommit(() -> applicationEventPublisher.publishEvent(new VerifiableCredentialCreatedEvent(verifiableCredential)));
    }

    public void delete(@NonNull VerifiableCredential verifiableCredential) {
        applicationEventPublisher.publishEvent(new VerifiableCredentialDeletingEvent(verifiableCredential));
        verifiableCredentialRepository.delete(verifiableCredential);
        afterCommit(() -> applicationEventPublisher.publishEvent(new VerifiableCredentialDeletedEvent(verifiableCredential)));
    }

    private static void afterCommit(Runnable runnable) {
        TransactionSynchronizationUtils.invokeAfterCommit(List.of(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        runnable.run();
                    }
                }));
    }
}

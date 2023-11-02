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
import org.eclipse.tractusx.managedidentitywallets.event.*;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void storeVerifiableCredential(@NonNull Wallet wallet, @NonNull VerifiableCredential verifiableCredential) {
        walletRepository.storeVerifiableCredentialInWallet(wallet, verifiableCredential);
    }

    public Optional<Wallet> findById(@NonNull WalletId id) {
        final WalletQuery query = WalletQuery.builder()
                .walletId(id)
                .build();
        return walletRepository.findOne(query);
    }

    public Page<Wallet> findAll(int page, int size) {
        final WalletQuery query = WalletQuery.builder().build();
        return findAll(query, page, size);
    }

    public Page<Wallet> findAll(@NonNull WalletQuery query, int page, int size) {
        final Pageable pageable = Pageable.ofSize(size).withPage(page);
        return walletRepository.findAll(query, pageable);
    }

    public void create(@NonNull Wallet wallet) {
        applicationEventPublisher.publishEvent(new WalletCreatingEvent(wallet));
        walletRepository.create(wallet);
        afterCommit(() -> applicationEventPublisher.publishEvent(new WalletCreatedEvent(wallet)));
    }

    public void update(@NonNull Wallet wallet) {
        applicationEventPublisher.publishEvent(new WalletUpdatingEvent(wallet));
        walletRepository.update(wallet);
        afterCommit(() -> applicationEventPublisher.publishEvent(new WalletUpdatedEvent(wallet)));
    }

    public void delete(@NonNull Wallet wallet) {
        applicationEventPublisher.publishEvent(new WalletDeletingEvent(wallet));
        walletRepository.delete(wallet.getWalletId());
        afterCommit(() -> applicationEventPublisher.publishEvent(new WalletDeletedEvent(wallet)));
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

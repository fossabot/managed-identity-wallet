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

package org.eclipse.tractusx.managedidentitywallets.test;


import lombok.Getter;
import lombok.NonNull;
import org.eclipse.tractusx.managedidentitywallets.event.*;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WalletEventTracker {
    final List<WalletCreatingEvent> walletCreatingEvents = new ArrayList<>();
    final List<WalletCreatedEvent> walletCreatedEvents = new ArrayList<>();
    final List<WalletDeletedEvent> walletDeletedEvents = new ArrayList<>();
    final List<WalletDeletingEvent> walletDeletingEvents = new ArrayList<>();
    final List<WalletUpdatedEvent> walletUpdatedEvents = new ArrayList<>();
    final List<WalletUpdatingEvent> walletUpdatingEvents = new ArrayList<>();

    @EventListener
    public void onWalletCreatingEvent(@NonNull WalletCreatingEvent event) {
        walletCreatingEvents.add(event);
    }

    @EventListener
    public void onWalletCreatedEvent(@NonNull WalletCreatedEvent event) {
        walletCreatedEvents.add(event);
    }

    @EventListener
    public void onWalletDeletedEvent(@NonNull WalletDeletedEvent event) {
        walletDeletedEvents.add(event);
    }

    @EventListener
    public void onWalletDeletingEvent(@NonNull WalletDeletingEvent event) {
        walletDeletingEvents.add(event);
    }

    @EventListener
    public void onWalletUpdatedEvent(@NonNull WalletUpdatedEvent event) {
        walletUpdatedEvents.add(event);
    }

    @EventListener
    public void onWalletUpdatingEvent(@NonNull WalletUpdatingEvent event) {
        walletUpdatingEvents.add(event);
    }

    public void clear() {
        walletCreatedEvents.clear();
        walletCreatingEvents.clear();
        walletDeletedEvents.clear();
        walletDeletingEvents.clear();
        walletUpdatedEvents.clear();
        walletUpdatingEvents.clear();
    }
}
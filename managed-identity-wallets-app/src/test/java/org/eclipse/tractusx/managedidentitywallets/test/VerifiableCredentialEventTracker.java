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
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialCreatedEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialCreatingEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialDeletedEvent;
import org.eclipse.tractusx.managedidentitywallets.event.VerifiableCredentialDeletingEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

@Getter
public class VerifiableCredentialEventTracker {
    final List<VerifiableCredentialCreatingEvent> verifiableCredentialCreatingEvents = new ArrayList<>();
    final List<VerifiableCredentialCreatedEvent> verifiableCredentialCreatedEvents = new ArrayList<>();
    final List<VerifiableCredentialDeletedEvent> verifiableCredentialDeletedEvents = new ArrayList<>();
    final List<VerifiableCredentialDeletingEvent> verifiableCredentialDeletingEvents = new ArrayList<>();

    @EventListener
    public void onVerifiableCredentialCreatingEvent(@NonNull VerifiableCredentialCreatingEvent event) {
        verifiableCredentialCreatingEvents.add(event);
    }

    @EventListener
    public void onVerifiableCredentialCreatedEvent(@NonNull VerifiableCredentialCreatedEvent event) {
        verifiableCredentialCreatedEvents.add(event);
    }

    @EventListener
    public void onVerifiableCredentialDeletedEvent(@NonNull VerifiableCredentialDeletedEvent event) {
        verifiableCredentialDeletedEvents.add(event);
    }

    @EventListener
    public void onVerifiableCredentialDeletingEvent(@NonNull VerifiableCredentialDeletingEvent event) {
        verifiableCredentialDeletingEvents.add(event);
    }

    public void clear() {
        verifiableCredentialCreatedEvents.clear();
        verifiableCredentialCreatingEvents.clear();
        verifiableCredentialDeletedEvents.clear();
        verifiableCredentialDeletingEvents.clear();
    }
}
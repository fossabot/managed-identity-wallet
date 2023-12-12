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

package org.eclipse.tractusx.managedidentitywallets.test.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.test.VerifiableCredentialEventTracker;
import org.eclipse.tractusx.managedidentitywallets.test.WalletEventTracker;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class TestEventUtil {

    private final WalletEventTracker walletEventTracker;

    private final VerifiableCredentialEventTracker verifiableCredentialEventTracker;

    @BeforeEach
    public void cleanUp() {
        verifiableCredentialEventTracker.clear();
        walletEventTracker.clear();
    }

    @Configuration
    static class EventUtilConfiguration {
        @Bean
        public VerifiableCredentialEventTracker getVerifiableCredentialEventTracker() {
            return new VerifiableCredentialEventTracker();
        }

        @Bean
        public WalletEventTracker getWalletEventTracker() {
            return new WalletEventTracker();
        }
    }
}

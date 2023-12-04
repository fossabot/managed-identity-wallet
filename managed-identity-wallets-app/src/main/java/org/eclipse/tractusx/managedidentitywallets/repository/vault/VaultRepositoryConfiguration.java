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

package org.eclipse.tractusx.managedidentitywallets.repository.vault;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultRepositoryConfiguration {

    @Configuration
    @RequiredArgsConstructor
    public static class InMemoryConfiguration {

        @Bean
        @ConditionalOnProperty(value = "spring.cloud.vault.enabled", havingValue = "false", matchIfMissing = true)
        public VaultRepository inMemoryVaultRepository() {
            return new InMemoryVaultRepository();
        }

        @Bean
        @Autowired
        @ConditionalOnProperty(value = "spring.cloud.vault.enabled", havingValue = "true")
        public VaultRepository vaultRepository(VaultTemplate vaultTemplate) {
            return new VaultRepositoryImpl(vaultTemplate);
        }
    }
}

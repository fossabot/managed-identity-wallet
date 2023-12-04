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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultTemplate;

@Configuration
public class VaultRepositoryConfiguration {

    @Configuration
    @ConditionalOnMissingBean(VaultTemplate.class)
    public static class InMemoryConfiguration {

        @Bean
        public VaultRepository vaultRepository() {
            return new InMemoryVaultRepository();
        }
    }

    @Configuration
    @ConditionalOnBean(VaultTemplate.class)
    @RequiredArgsConstructor
    public static class VaultConfiguration {

        private final VaultTemplate vaultTemplate;
        @Value("${miw.vault.mountName}")
        private String managedIdentityWalletsMount;

        @Bean
        public VaultRepository vaultRepository() {
            return new VaultRepositoryImpl(managedIdentityWalletsMount, vaultTemplate);
        }
    }
}

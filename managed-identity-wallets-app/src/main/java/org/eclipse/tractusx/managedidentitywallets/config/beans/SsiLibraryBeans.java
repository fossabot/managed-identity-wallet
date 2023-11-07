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

package org.eclipse.tractusx.managedidentitywallets.config.beans;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.config.HttpConfigurationProperties;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidResolver;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebResolver;
import org.eclipse.tractusx.ssi.lib.did.web.util.DidWebParser;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

@Configuration
@Slf4j
public class SsiLibraryBeans {

    @Bean
    @SneakyThrows
    public HttpClient httpClient(@NonNull HttpConfigurationProperties httpConfigurationProperties) {

        final Duration connectionTimeout = Optional.ofNullable(httpConfigurationProperties.getConnectionTimeout())
                .orElse(HttpConfigurationProperties.DEFAULT_CONNECT_TIMEOUT);

        return HttpClient.newBuilder()
                .connectTimeout(connectionTimeout)
                .build();
    }

    @Bean
    public DidResolver didResolver(@NonNull HttpClient httpClient, @NonNull MIWSettings miwSettings) {
        final DidWebParser didWebParser = new DidWebParser();
        return new DidWebResolver(httpClient, didWebParser, miwSettings.enforceHttps());
    }

    @Bean
    public LinkedDataProofValidation linkedDataProofValidation(@NonNull DidResolver didResolver) {
        return LinkedDataProofValidation.newInstance(didResolver);
    }
}

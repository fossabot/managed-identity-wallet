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

package org.eclipse.tractusx.managedidentitywallets.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.config.HttpConfigurationProperties;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidResolver;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebResolver;
import org.eclipse.tractusx.ssi.lib.did.web.util.DidWebParser;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidator;
import org.eclipse.tractusx.ssi.lib.validation.JsonLdValidatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SsiLibraryConfiguration {

    @NonNull
    private final HttpConfigurationProperties httpConfigurationProperties;

    @Bean
    public HttpClient httpClient() {
        final HttpClient.Builder builder = HttpClient.newBuilder();

        builder.connectTimeout(Optional.ofNullable(httpConfigurationProperties.getConnectionTimeout())
                .orElse(HttpConfigurationProperties.DEFAULT_CONNECT_TIMEOUT));

        builder.followRedirects(HttpClient.Redirect.NORMAL);

        final HttpConfigurationProperties.FollowRedirect followRedirect = Optional.ofNullable(httpConfigurationProperties.getFollowRedirects())
                .orElse(HttpConfigurationProperties.DEFAULT_FOLLOW_REDIRECTS);
        switch (followRedirect) {
            case always -> builder.followRedirects(HttpClient.Redirect.ALWAYS);
            case never -> builder.followRedirects(HttpClient.Redirect.NEVER);
            case normal -> builder.followRedirects(HttpClient.Redirect.NORMAL);
            default -> throw new IllegalStateException("Unexpected value: " + followRedirect);
        }

        return builder.build();
    }

    @Bean
    public DidResolver didResolver(HttpClient httpClient, MIWSettings miwSettings) {
        final DidWebParser didWebParser = new DidWebParser();
        return new DidWebResolver(httpClient, didWebParser, miwSettings.isEnforceHttps());
    }

    @Bean
    public LinkedDataProofValidation linkedDataProofValidation(@NonNull DidResolver didResolver) {
        return LinkedDataProofValidation.newInstance(didResolver);
    }

    @Bean
    public JsonLdValidator jsonLdValidator() {
        return new JsonLdValidatorImpl();
    }

}

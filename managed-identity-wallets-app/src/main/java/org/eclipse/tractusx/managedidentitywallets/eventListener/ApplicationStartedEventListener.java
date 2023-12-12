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

package org.eclipse.tractusx.managedidentitywallets.eventListener;

import com.apicatalog.jsonld.document.JsonDocument;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.config.VerifiableCredentialContextConfiguration;
import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.models.WalletName;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.util.ApplicationResourceLoader;
import org.eclipse.tractusx.ssi.lib.model.RemoteDocumentLoader;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationStartedEventListener {

    private static final int ORDER_REGISTER_EMBEDDED_RESOURCES = 10;
    private static final int ORDER_CREATE_AUTHORITY_WALLET = 20;

    private final MIWSettings miwSettings;
    private final WalletService walletService;
    private final ApplicationResourceLoader applicationResourceLoader;
    private final VerifiableCredentialContextConfiguration verifiableCredentialContextConfiguration;

    @EventListener
    @Order(ORDER_CREATE_AUTHORITY_WALLET)
    public void createAuthorityWallet(ApplicationStartedEvent event) {
        final WalletId walletId = new WalletId(miwSettings.getAuthorityWalletBpn());
        final WalletName walletName = new WalletName(miwSettings.getAuthorityWalletName());

        if (walletService.existsById(walletId)) {
            log.trace("Authority wallet already exists, skipping creation");
            return;
        }

        final Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .walletName(walletName)
                .createdAt(OffsetDateTime.now())
                .build();

        log.info("Creating authority wallet with id {}", walletId.getText());
        walletService.create(wallet);
    }

    @EventListener
    @SneakyThrows
    @Order(ORDER_REGISTER_EMBEDDED_RESOURCES)
    public void registerEmbeddedResources(ApplicationStartedEvent event) {
        if (!verifiableCredentialContextConfiguration.isUseEmbeddedContexts()) {
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Registering embedded resources");
        }

        final RemoteDocumentLoader documentLoader = RemoteDocumentLoader.getInstance();
        documentLoader.setEnableLocalCache(true);

        final Map<URI, JsonDocument> localCache = new HashMap<>();
        for (ApplicationResourceLoader.JsonLdResource jsonLdResource : ApplicationResourceLoader.JsonLdResource.values()) {
            localCache.put(jsonLdResource.getUri(),
                    JsonDocument.of(applicationResourceLoader.loadJsonLdResource(jsonLdResource)));
        }

        documentLoader.setLocalCache(localCache);
    }
}

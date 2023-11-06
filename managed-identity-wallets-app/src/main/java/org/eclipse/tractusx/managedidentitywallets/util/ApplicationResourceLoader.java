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

package org.eclipse.tractusx.managedidentitywallets.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;

@NoArgsConstructor
@Component
public class ApplicationResourceLoader {

    public InputStream loadJsonLdResource(JsonLdResource jsonLdResource) {
        return this.getClass().getResourceAsStream(jsonLdResource.resourceName);
    }

    public enum JsonLdResource {
        JWS_2020_V1("https://w3id.org/security/suites/jws-2020/v1", "/contexts/jws2020-v1.json"),
        DID_V1("https://www.w3.org/ns/did/v1", "/contexts/did-v1.json"),
        SUMMARY_VC_V1("https://catenax-ng.github.io/product-core-schemas/SummaryVC.json", "/contexts/summary-vc-v1.json"),
        BUSINESS_PARTNER_DATA_VC_V1("https://catenax-ng.github.io/product-core-schemas/businessPartnerData.json", "/contexts/business-partner-data-v1.json"),
        CREDENTIALS_V1("https://www.w3.org/2018/credentials/v1", "/contexts/credentials-v1.json"),
        ;

        @Getter
        private final URI uri;
        private final String resourceName;

        JsonLdResource(String uri, String resourceName) {
            this.uri = URI.create(uri);
            this.resourceName = resourceName;
        }

    }
}

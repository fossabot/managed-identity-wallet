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

package org.eclipse.tractusx.managedidentitywallets.api.v1.controller;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.security.SecurityService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * The type Base controller.
 */
@Controller
public class BaseController {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private WalletService walletService;

    /**
     * Gets bpn from token.
     *
     * @return the bpn from token
     */
    public String getBpn() {
        final String bpn = securityService.getBpn()
                .orElseThrow(() -> new ForbiddenException("Invalid token, BPN not found"));

        if (walletService.existsById(new WalletId(bpn))) {
            return bpn;
        } else {
            throw new ForbiddenException();
        }
    }
}

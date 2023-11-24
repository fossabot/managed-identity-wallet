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

package org.eclipse.tractusx.managedidentitywallets.api.v2.delegate;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class AbstractApiHandler {

    /* Used as long as there is no authentication */
    protected static final WalletId TMP_WALLET_ID = new WalletId("BPNL000000000000");

    protected void logIfDebug(String string, Object arg) {
        if (log.isDebugEnabled()) {
            log.debug(string, arg);
        }
    }

    protected void logIfDebug(String string, Object arg1, Object arg2) {
        if (log.isDebugEnabled()) {
            log.debug(string, arg1, arg2);
        }
    }


    protected void logIfDebug(String string, Object arg1, Object arg2, Object arg3) {
        if (log.isDebugEnabled()) {
            log.debug(string, arg1, arg2, arg3);
        }
    }

    protected void logIfDebug(String string, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (log.isDebugEnabled()) {
            log.debug(string, arg1, arg2, arg3, arg4);
        }
    }

    protected void logIfDebug(String string, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if (log.isDebugEnabled()) {
            log.debug(string, arg1, arg2, arg3, arg4, arg5);
        }
    }

    protected Optional<VerifiableCredentialSubject> readVerifiableCredentialSubjectArgs(Map<String, Object> subject) {
        if (subject == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new VerifiableCredentialSubject(subject));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected Optional<List<VerifiableCredential>> readVerifiableCredentialArgs(List<Map<String, Object>> payload) {
        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    payload.stream()
                            .map(this::readVerifiableCredentialArg)
                            .map(Optional::orElseThrow)
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected Optional<VerifiableCredential> readVerifiableCredentialArg(Map<String, Object> payload) {
        if (payload == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new VerifiableCredential(payload));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

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

package org.eclipse.tractusx.managedidentitywallets.factory;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.models.DidFragment;
import org.eclipse.tractusx.managedidentitywallets.models.Ed25519KeyId;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.ssi.lib.crypt.IKeyGenerator;
import org.eclipse.tractusx.ssi.lib.crypt.KeyPair;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559Generator;
import org.eclipse.tractusx.ssi.lib.exception.KeyGenerationException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class Ed25519KeyFactory {

    public ResolvedEd25519Key generateNewEd25519Key() {
        return generateNewEd25519Key(new DidFragment(UUID.randomUUID().toString()));
    }

    @SneakyThrows(KeyGenerationException.class)
    public ResolvedEd25519Key generateNewEd25519Key(@NonNull DidFragment didFragment) {

        final IKeyGenerator keyGenerator = new x21559Generator();
        final KeyPair keyPair = keyGenerator.generateKey();

        final Ed25519KeyId keyId = new Ed25519KeyId(UUID.randomUUID().toString());

        return ResolvedEd25519Key.builder()
                .id(keyId)
                .privateKey(keyPair.getPrivateKey().asByte())
                .publicKey(keyPair.getPublicKey().asByte())
                .didFragment(didFragment)
                .createdAt(OffsetDateTime.now())
                .build();
    }


}

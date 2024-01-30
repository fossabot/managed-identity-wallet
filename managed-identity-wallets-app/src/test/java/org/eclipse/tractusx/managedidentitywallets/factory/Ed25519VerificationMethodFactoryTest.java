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

import lombok.SneakyThrows;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519VerificationMethod;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PublicKey;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebResolver;
import org.eclipse.tractusx.ssi.lib.did.web.util.DidWebParser;
import org.eclipse.tractusx.ssi.lib.proof.hash.HashedLinkedData;
import org.eclipse.tractusx.ssi.lib.proof.types.ed25519.Ed25519ProofSigner;
import org.eclipse.tractusx.ssi.lib.proof.types.ed25519.Ed25519ProofVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

public class Ed25519VerificationMethodFactoryTest {


    private final Ed25519KeyFactory ed25519KeyFactory = new Ed25519KeyFactory();
    private final Ed25519ProofVerifier ed25519ProofVerifier = new Ed25519ProofVerifier(new DidWebResolver(HttpClient.newHttpClient(), new DidWebParser(), false));
    private final Ed25519ProofSigner ed25519ProofSigner = new Ed25519ProofSigner();

    @SneakyThrows
    @Test
    public void testGeneratedKeyWithProof() {
        final ResolvedEd25519VerificationMethod key = ed25519KeyFactory.generateNewEd25519Key();
        final x21559PrivateKey privateKey = new x21559PrivateKey(key.getPrivateKey().getBytes());
        final x21559PublicKey publicKey = new x21559PublicKey(key.getPublicKey().getBytes());

        final HashedLinkedData data = new HashedLinkedData("foo".getBytes());
        final byte[] signature = ed25519ProofSigner.sign(data, privateKey);

        final boolean result = ed25519ProofVerifier.verify(data, signature, publicKey);

        Assertions.assertTrue(result);
    }
}

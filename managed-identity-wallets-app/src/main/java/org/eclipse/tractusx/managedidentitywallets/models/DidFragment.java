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

package org.eclipse.tractusx.managedidentitywallets.models;

import lombok.*;

/**
 * In the context of Decentralized Identifiers (DIDs), a "DID Fragment" refers to a portion of the DID that comes
 * after a '#' character. The DID Fragment is often used to reference a specific resource or document
 * associated with the DID.
 * <p>
 * For example, a DID might look like this: did:example:12345678
 * <p>
 * In this case, the DID is did:example:12345678, and the DID Fragment would be any part of the DID that appears after
 * a '#' character, if present. For example:
 * <p>
 * did:example:12345678#section-1
 * <p>
 * Such a did fragment is represented using this class by calling 'new DidFragment("section-1")'.
 */
@Value
@EqualsAndHashCode
@ToString
public class DidFragment {
    @NonNull
    String text;
}

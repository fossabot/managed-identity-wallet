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

package org.eclipse.tractusx.managedidentitywallets.repository.repository;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.WalletEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WalletRepository {

    private final WalletJpaRepository walletJpaRepository;

    public boolean existsById(String id) {
        return false;
    }

    public Optional<WalletEntity> getById(String id) {
        return null;
    }

    public Optional<WalletEntity> getByName(String walletName) {

        final Optional<WalletEntity> entityOptional = walletJpaRepository.findByName(walletName);

        return entityOptional;
    }
}

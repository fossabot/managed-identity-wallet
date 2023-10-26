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

package org.eclipse.tractusx.managedidentitywallets.v2.map;

import org.eclipse.tractusx.managedidentitywallets.models.Wallet;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.ListWalletsResponsePayloadV2;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.WalletV2;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface WalletsApiMapper {

    @Mapping(target = "walletId.text", source = "id")
    @Mapping(target = "walletName.text", source = "name")
    @Mapping(target = "walletDescription.text", source = "description")
    Wallet map(WalletV2 walletV2);

    @Mapping(target = "id", source = "walletId.text")
    @Mapping(target = "description", source = "walletDescription.text")
    @Mapping(target = "name", source = "walletName.text")
    WalletV2 map(Wallet wallet);

    @Mapping(target = "size", source = "numberOfElements")
    @Mapping(target = "page", source = "number")
    @Mapping(target = "totalElements", source = "totalElements")
    @Mapping(target = "items", source = "content")
    ListWalletsResponsePayloadV2 map(Page<Wallet> wallets);
}

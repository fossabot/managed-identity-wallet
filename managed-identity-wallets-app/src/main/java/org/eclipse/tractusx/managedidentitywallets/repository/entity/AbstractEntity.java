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

package org.eclipse.tractusx.managedidentitywallets.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Data
@MappedSuperclass
public abstract class AbstractEntity {

    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_MODIFIED_AT = "modified_at";
    public static final String COLUMN_VERSION = "version";

    // TODO define column names

    @CreationTimestamp
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = COLUMN_CREATED_AT, nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = COLUMN_MODIFIED_AT, nullable = false)
    private OffsetDateTime modifiedAt;

    @Column(name = COLUMN_VERSION, nullable = false, length = 8)
    private String version;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.modifiedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedAt = OffsetDateTime.now();
    }
}


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

/* Wallet */
CREATE TABLE IF NOT EXISTS wallet
(
    id          varchar(255)             NOT NULL,
    version     char(8)                  NOT NULL DEFAULT 'v1',
    name        varchar(255)             NOT NULL,
    description varchar(255),
    created_at  timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX wallet_name ON wallet (name);

/* Key */
CREATE TABLE IF NOT EXISTS key_ed25519
(
    id             varchar(255)             NOT NULL,
    version        char(8)                  NOT NULL DEFAULT 'v1',
    did_identifier varchar(255)             NOT NULL,
    description    varchar(255),
    vault_secret   varchar(255)             NOT NULL,
    wallet_id      varchar(255)             NOT NULL,
    created_at     timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (wallet_id) REFERENCES wallet (id) ON DELETE CASCADE
);

/* Verifiable Credential */
CREATE TABLE IF NOT EXISTS verifiable_credential
(
    id          varchar(255) NOT NULL,
    version     char(8)      NOT NULL DEFAULT 'v1',
    raw         text         NOT NULL,
    created_at  timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp(6) NULL,
    PRIMARY KEY (id)
);

/* Verifiable Credential Type */
CREATE TABLE IF NOT EXISTS verifiable_credential_type
(
    type        varchar(255) NOT NULL,
    version     char(8)      NOT NULL DEFAULT 'v1',
    created_at  timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp(6) NULL,
    PRIMARY KEY (type)
);

/* Verifiable Credential Issuer */
CREATE TABLE IF NOT EXISTS verifiable_credential_issuer
(
    issuer      varchar(255) NOT NULL,
    version     char(8)      NOT NULL DEFAULT 'v1',
    created_at  timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp(6) NULL,
    PRIMARY KEY (issuer)
);

/* Verifiable Credential Intersection Table */
CREATE TABLE IF NOT EXISTS verifiable_credential_intersection
(
    wallet_id                varchar(255) NOT NULL,
    verifiable_credential_id varchar(255) NOT NULL,
    version                  char(8)      NOT NULL DEFAULT 'v1',
    created_at               timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at              timestamp(6) NULL,
    PRIMARY KEY (wallet_id, verifiable_credential_id),
    FOREIGN KEY (wallet_id) REFERENCES wallet (id) ON DELETE CASCADE,
    FOREIGN KEY (verifiable_credential_id) REFERENCES verifiable_credential (id) ON DELETE CASCADE
);

/* Verifiable Credential Type Intersection Table */
CREATE TABLE IF NOT EXISTS verifiable_credential_type_intersection
(
    verifiable_credential_id      varchar(255) NOT NULL,
    verifiable_credential_type_id varchar(255) NOT NULL,
    version                       char(8)      NOT NULL DEFAULT 'v1',
    created_at                    timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at                   timestamp(6) NULL,
    PRIMARY KEY (verifiable_credential_id, verifiable_credential_type_id),
    FOREIGN KEY (verifiable_credential_id) REFERENCES verifiable_credential (id) ON DELETE CASCADE,
    FOREIGN KEY (verifiable_credential_type_id) REFERENCES verifiable_credential_type (type) ON DELETE CASCADE
);

/* Verifiable Credential Issuer Intersection Table */
CREATE TABLE IF NOT EXISTS verifiable_credential_type_intersection
(
    verifiable_credential_id        varchar(255) NOT NULL,
    verifiable_credential_issuer_id varchar(255) NOT NULL,
    version                         char(8)      NOT NULL DEFAULT 'v1',
    created_at                      timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at                     timestamp(6) NULL,
    PRIMARY KEY (verifiable_credential_id, verifiable_credential_issuer_id),
    FOREIGN KEY (verifiable_credential_id) REFERENCES verifiable_credential (id) ON DELETE CASCADE,
    FOREIGN KEY (verifiable_credential_issuer_id) REFERENCES verifiable_credential_issuer (issuer) ON DELETE CASCADE
);
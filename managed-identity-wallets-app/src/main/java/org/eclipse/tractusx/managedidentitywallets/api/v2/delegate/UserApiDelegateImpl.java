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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.annotations.HasSubject;
import org.eclipse.tractusx.managedidentitywallets.api.v2.map.WalletsApiMapper;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialAlreadyExistsException;
import org.eclipse.tractusx.managedidentitywallets.exception.VerifiableCredentialNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.UserApiDelegate;
import org.eclipse.tractusx.managedidentitywallets.spring.models.v2.*;
import org.eclipse.tractusx.managedidentitywallets.util.DidFactory;
import org.eclipse.tractusx.managedidentitywallets.util.verifiableCredential.GenericAbstractVerifiableCredentialFactory;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserApiDelegateImpl implements UserApiDelegate {

    private static final WalletId TMP_WALLET_ID = new WalletId("BPNL000000000000");

    private final VerifiableCredentialService verifiableCredentialService;
    private final WalletService walletService;
    private final WalletsApiMapper apiMapper;
    private final MIWSettings miwSettings;
    private final GenericAbstractVerifiableCredentialFactory genericVerifiableCredentialFactory;
    private final DidFactory didFactory;

    @Override
    @Transactional
    public ResponseEntity<Map<String, Object>> userCreateVerifiableCredential(Map<String, Object> payload) {
        if (log.isDebugEnabled()) {
            log.debug("userCreateVerifiableCredential(payload={})", payload);
        }

        final VerifiableCredential verifiableCredential = new VerifiableCredential(payload);

        final Wallet wallet = walletService.findById(TMP_WALLET_ID)
                .orElseThrow(() -> new WalletNotFoundException(TMP_WALLET_ID));

        try {
            verifiableCredentialService.create(verifiableCredential);
        } catch (VerifiableCredentialAlreadyExistsException e) {
            // ignore
        }
        walletService.storeVerifiableCredential(wallet, verifiableCredential);

        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId(verifiableCredential.getId().toString());
        final VerifiableCredential storedVerifiableCredential =
                verifiableCredentialService.findById(verifiableCredentialId)
                        .orElseThrow();
        final URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(storedVerifiableCredential.getId().toString())
                .toUri();

        return ResponseEntity.created(location).body(storedVerifiableCredential);
    }

    @Override
    public ResponseEntity<Void> userDeleteVerifiableCredentialById(String verifiableCredentialId) {
        if (log.isDebugEnabled()) {
            log.debug("userDeleteVerifiableCredentialById(walletId={}, verifiableCredentialId={})", TMP_WALLET_ID, verifiableCredentialId);
        }
        final VerifiableCredentialId id = new VerifiableCredentialId(verifiableCredentialId);
        final VerifiableCredential verifiableCredential = verifiableCredentialService.findById(id)
                .orElseThrow(() -> new VerifiableCredentialNotFoundException(id));
        verifiableCredentialService.delete(verifiableCredential);
        return ResponseEntity.status(204).build();
    }

    @Override
    public ResponseEntity<Map<String, Object>> userGetVerifiableCredentialById(String verifiableCredentialId) {
        if (log.isDebugEnabled()) {
            log.debug("userGetVerifiableCredentialById(walletId={}, verifiableCredentialId={})", TMP_WALLET_ID, verifiableCredentialId);
        }

        final VerifiableCredentialId id = new VerifiableCredentialId(verifiableCredentialId);
        final Optional<VerifiableCredential> verifiableCredential = verifiableCredentialService.findById(id);

        return verifiableCredential.<ResponseEntity<Map<String, Object>>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> userGetIssuedVerifiableCredentials(Integer page, Integer perPage, String type) {
        if (log.isDebugEnabled()) {
            log.debug("userGetIssuedVerifiableCredentials(walletId={}, page={}, perPage={}, type={})", TMP_WALLET_ID, page, perPage, type);
        }

        page = Optional.ofNullable(page).orElse(0);
        perPage = Optional.ofNullable(perPage).orElse(miwSettings.apiDefaultPageSize());

        final List<VerifiableCredentialType> verifiableCredentialType = Optional.ofNullable(type)
                .map(VerifiableCredentialType::new)
                .stream().toList();

        final Did issuerDid = didFactory.generateDid(TMP_WALLET_ID);
        final VerifiableCredentialIssuer verifiableCredentialIssuer = new VerifiableCredentialIssuer(issuerDid.toString());
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialTypes(verifiableCredentialType)
                .verifiableCredentialIssuer(verifiableCredentialIssuer)
                .build();

        final Page<VerifiableCredential> verifiableCredentialPage = verifiableCredentialService.findAll(verifiableCredentialQuery, page, perPage);

        final VerifiableCredentialListResponsePayloadV2 response = apiMapper.mapVerifiableCredentialListResponsePayloadV2(verifiableCredentialPage);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VerifiableCredentialListResponsePayloadV2> userGetVerifiableCredentials(Integer page, Integer perPage, String type, String issuer) {
        if (log.isDebugEnabled()) {
            log.debug("userGetVerifiableCredentials(walletId={}, page={}, perPage={}, type={}, issuer={})", TMP_WALLET_ID, page, perPage, type, issuer);
        }

        page = Optional.ofNullable(page).orElse(0);
        perPage = Optional.ofNullable(perPage).orElse(miwSettings.apiDefaultPageSize());

        final List<VerifiableCredentialType> verifiableCredentialType = Optional.ofNullable(type)
                .map(VerifiableCredentialType::new)
                .stream().toList();
        final VerifiableCredentialIssuer verifiableCredentialIssuer = Optional.ofNullable(issuer)
                .map(VerifiableCredentialIssuer::new)
                .orElse(null);
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(TMP_WALLET_ID)
                .verifiableCredentialTypes(verifiableCredentialType)
                .verifiableCredentialIssuer(verifiableCredentialIssuer)
                .build();

        final Page<VerifiableCredential> verifiableCredentialPage = verifiableCredentialService.findAll(verifiableCredentialQuery, page, perPage);

        final VerifiableCredentialListResponsePayloadV2 response = apiMapper.mapVerifiableCredentialListResponsePayloadV2(verifiableCredentialPage);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WalletResponsePayloadV2> userGetWallet() {
        if (log.isDebugEnabled()) {
            log.debug("userGetWallet(walletId={})", TMP_WALLET_ID);
        }

        final Wallet wallet = walletService.findById(TMP_WALLET_ID)
                .orElseThrow();

        final WalletResponsePayloadV2 payloadV2 = apiMapper.mapWalletResponsePayloadV2(wallet);
        return ResponseEntity.ok(payloadV2);
    }

    @Override
    @Transactional
    @Valid
    public ResponseEntity<Map<String, Object>> userIssuedVerifiableCredential(@HasSubject IssueVerifiableCredentialRequestPayloadV2 issueVerifiableCredentialRequestPayloadV2) {
        if (log.isDebugEnabled()) {
            log.debug("userIssuedVerifiableCredential(issueVerifiableCredentialRequestPayloadV2={})", issueVerifiableCredentialRequestPayloadV2);
        }

        final GenericAbstractVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.GenericVerifiableCredentialFactoryArgsBuilder factoryArgs =
                GenericAbstractVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder();

        final Wallet wallet = walletService.findById(TMP_WALLET_ID).orElseThrow();

        /* Subject */
        final VerifiableCredentialSubject subject = new VerifiableCredentialSubject(issueVerifiableCredentialRequestPayloadV2.getVerifiableCredentialSubject());
        factoryArgs.subject(subject);

        /* Wallet */
        factoryArgs.issuerWallet(wallet);

        /* Expiration Date */
        Optional.ofNullable(issueVerifiableCredentialRequestPayloadV2.getExpirationDate())
                .map(OffsetDateTime::toInstant)
                .ifPresent(factoryArgs::expirationDate);

        /* Verifiable Credential Types */
        Optional.ofNullable(issueVerifiableCredentialRequestPayloadV2.getAdditionalVerifiableCredentialTypes())
                .ifPresent(types -> types.stream()
                        .map(VerifiableCredentialType::new)
                        .forEach(factoryArgs::additionalVerifiableCredentialType));

        /* Verifiable Credential Contexts */
        Optional.ofNullable(issueVerifiableCredentialRequestPayloadV2.getAdditionalVerifiableCredentialContexts())
                .ifPresent(contexts -> contexts
                        .stream()
                        .map(URI::create)
                        .map(VerifiableCredentialContext::new)
                        .forEach(factoryArgs::additionalContext));

        final VerifiableCredential verifiableCredential = genericVerifiableCredentialFactory.createVerifiableCredential(factoryArgs.build());
        /* The MIW should remember all issued Verifiable Credentials. It is written to the database but not (yet) linked to any wallet */
        verifiableCredentialService.create(verifiableCredential);

        return ResponseEntity.ok(verifiableCredential);
    }

    @Override
    public ResponseEntity<Map<String, Object>> userIssuedVerifiablePresentation(IssueVerifiablePresentationRequestPayloadV2 issueVerifiablePresentationRequestPayloadV2) {
        return UserApiDelegate.super.userIssuedVerifiablePresentation(issueVerifiablePresentationRequestPayloadV2);
    }

    @Override
    public ResponseEntity<IssueVerifiablePresentationJwtResponsePayloadV2> userIssuedVerifiablePresentationJwt(IssueVerifiablePresentationJwtRequestPayloadV2 issueVerifiablePresentationJwtRequestPayloadV2) {
        return UserApiDelegate.super.userIssuedVerifiablePresentationJwt(issueVerifiablePresentationJwtRequestPayloadV2);
    }

    @Override
    public ResponseEntity<ValidateVerifiablePresentationJwtResponsePayloadV2> validateVerifiablePresentationsJwtPost(ValidateVerifiablePresentationJwtRequestPayloadV2 validateVerifiablePresentationJwtRequestPayloadV2) {
        return UserApiDelegate.super.validateVerifiablePresentationsJwtPost(validateVerifiablePresentationJwtRequestPayloadV2);
    }

    @Override
    public ResponseEntity<ValidateVerifiableCredentialResponsePayloadV2> validateVerifiableCredentialsPost(ValidateVerifiableCredentialRequestPayloadV2 validateVerifiableCredentialRequestPayloadV2) {
        return UserApiDelegate.super.validateVerifiableCredentialsPost(validateVerifiableCredentialRequestPayloadV2);
    }

    @Override
    public ResponseEntity<ValidateVerifiablePresentationResponsePayloadV2> validateVerifiablePresentationsPost(ValidateVerifiablePresentationRequestPayloadV2 validateVerifiablePresentationRequestPayloadV2) {
        return UserApiDelegate.super.validateVerifiablePresentationsPost(validateVerifiablePresentationRequestPayloadV2);
    }
}

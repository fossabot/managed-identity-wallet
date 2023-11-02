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

package org.eclipse.tractusx.managedidentitywallets.api.v1.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.HoldersCredential;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.DuplicateWalletProblem;
import org.eclipse.tractusx.managedidentitywallets.api.v1.map.WalletMapV1;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.CommonUtils;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.WalletNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.*;
import org.eclipse.tractusx.managedidentitywallets.repository.VerifiableCredentialRepository;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.CreateWalletRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.Validate;
import org.eclipse.tractusx.managedidentitywallets.repository.query.WalletQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.crypt.jwk.JsonWebKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PublicKey;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.model.did.*;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.net.URI;
import java.util.*;

/**
 * The type Wallet service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WalletServiceV1 {

    /**
     * The constant BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN.
     */
    public static final String BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN = "Base wallet BPN is not matching with request BPN(from token)";
    private final WalletService walletService;
    private final VerifiableCredentialRepository verifiableCredentialRepository;

    private final VaultService vaultService;

    private final MIWSettings miwSettings;

    private final IssuersCredentialService issuersCredentialService;

    private final CommonService commonService;

    private final WalletMapV1 walletMapV1;


    /**
     * Store credential map.
     *
     * @param data       the data
     * @param identifier the identifier
     * @param callerBpn  the caller bpn
     * @return the map
     */
    public Map<String, String> storeCredential(Map<String, Object> data, String identifier, String callerBpn) {
        return null;
//        VerifiableCredential verifiableCredential = new VerifiableCredential(data);
//        Wallet wallet = getWalletByIdentifier(identifier);
//
//        //validate BPN access
//        Validate.isFalse(callerBpn.equalsIgnoreCase(wallet.getBpn())).launch(new ForbiddenException("Wallet BPN is not matching with request BPN(from the token)"));
//
//        //check type
//        Validate.isTrue(verifiableCredential.getTypes().isEmpty()).launch(new BadDataException("Invalid types provided in credentials"));
//
//        List<String> cloneTypes = new ArrayList<>(verifiableCredential.getTypes());
//        cloneTypes.remove(VerifiableCredentialType.VERIFIABLE_CREDENTIAL);
//
//        holdersCredentialRepository.save(HoldersCredential.builder()
//                .holderDid(wallet.getDid())
//                .issuerDid(verifiableCredential.getIssuer().toString())
//                .type(String.join(",", cloneTypes))
//                .data(verifiableCredential)
//                .selfIssued(false)
//                .stored(true)  //credential is stored(not issued by MIW)
//                .credentialId(verifiableCredential.getId().toString())
//                .build());
//        log.debug("VC type of {} stored for bpn ->{} with id-{}", cloneTypes, callerBpn, verifiableCredential.getId());
//        return Map.of("message", String.format("Credential with id %s has been successfully stored", verifiableCredential.getId()));
    }


    private Wallet getWalletByIdentifier(String identifier) {
        return commonService.getWalletByIdentifier(identifier);
    }

    /**
     * Gets wallet by identifier.
     *
     * @param identifier      the identifier
     * @param withCredentials the with credentials
     * @param callerBpn       the caller bpn
     * @return the wallet by identifier
     */
    public Wallet getWalletByIdentifier(String identifier, boolean withCredentials, String callerBpn) {
        return null;
//        Wallet wallet = getWalletByIdentifier(identifier);
//
//        // authority wallet can see all wallets
//        if (!miwSettings.authorityWalletBpn().equals(callerBpn)) {
//            //validate BPN access
//            Validate.isFalse(callerBpn.equalsIgnoreCase(wallet.getBpn())).launch(new ForbiddenException("Wallet BPN is not matching with request BPN(from the token)"));
//        }
//
//        if (withCredentials) {
//            wallet.setVerifiableCredentials(holdersCredentialRepository.getCredentialsByHolder(wallet.getDid()));
//        }
//        return wallet;
    }


    /**
     * Gets wallets.
     *
     * @param pageNumber the page number
     * @param size       the size
     * @param sortColumn the sort column
     * @param sortType   the sort type
     * @return the wallets
     */
    public Page<Wallet> getWallets(int pageNumber, int size, String sortColumn, String sortType) {

        Sort sort = Sort.unsorted();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortType != null) {
            try {
                direction = Sort.Direction.valueOf(sortType.toUpperCase());
            } catch (Exception e) {
                log.debug("Invalid sort type provided ->{}", sortType);
            }
        }

        if (sortColumn != null) {
            switch (sortColumn.toLowerCase()) {
                case "did":
                case "bpn":
                    sort = Sort.by(direction, "walletId.text");
                    break;
                case "name":
                    sort = Sort.by(direction, "walletName.text");
                    break;
                default:
                    log.debug("Invalid sort column provided ->{}", sortColumn);
            }
        }

        final WalletQuery walletQuery = WalletQuery.builder().build();
        return walletService.findAll(walletQuery, pageNumber, size, sort)
                .map(walletMapV1::map);
    }

    /**
     * Create wallet wallet.
     *
     * @param request the request
     * @return the wallet
     */
    @SneakyThrows
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public Wallet createWallet(CreateWalletRequest request, String callerBpn) {
        return createWallet(request, false, callerBpn);
    }

    /**
     * Create wallet.
     *
     * @param request the request
     * @return the wallet
     */
    @SneakyThrows
    @Transactional
    public Wallet createWallet(CreateWalletRequest request, boolean authority, String callerBpn) {
        validateCreateWallet(request, callerBpn);

        final WalletId walletId = new WalletId(request.getBpn());
        final WalletName walletName = new WalletName(request.getName());
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet =
                org.eclipse.tractusx.managedidentitywallets.models.Wallet.builder()
                        .walletId(walletId)
                        .walletName(walletName)
                        .build();
        walletService.create(wallet);

        return walletMapV1.map(wallet);
    }

    @SneakyThrows
    private Wallet map(org.eclipse.tractusx.managedidentitywallets.models.Wallet w) {

        final var key = vaultService.resolveKey(
                w.getStoredEd25519Keys().stream().findFirst().orElseThrow());
        var keyId = key.getId().getText();

        //create did json
        Did did = DidWebFactory.fromHostnameAndPath(miwSettings.host(), w.getWalletId().getText());

        JsonWebKey jwk = new JsonWebKey(keyId, new x21559PublicKey(key.getPublicKey()), new x21559PrivateKey(key.getPrivateKey()));
        JWKVerificationMethod jwkVerificationMethod =
                new JWKVerificationMethodBuilder().did(did).jwk(jwk).build();

        DidDocumentBuilder didDocumentBuilder = new DidDocumentBuilder();
        didDocumentBuilder.id(did.toUri());
        didDocumentBuilder.verificationMethods(List.of(jwkVerificationMethod));
        DidDocument didDocument = didDocumentBuilder.build();
        //modify context URLs
        List<URI> context = didDocument.getContext();
        List<URI> mutableContext = new ArrayList<>(context);
        miwSettings.didDocumentContextUrls().forEach(uri -> {
            if (!mutableContext.contains(uri)) {
                mutableContext.add(uri);
            }
        });
        didDocument.put("@context", mutableContext);
        didDocument = DidDocument.fromJson(didDocument.toJson());


        return Wallet.builder()
                .didDocument(didDocument)
                .bpn(w.getWalletId().getText())
                .name(w.getWalletName().getText())
                .did(did.toUri().toString())
                .algorithm(StringPool.ED_25519)
                .build();
    }

    private void validateCreateWallet(CreateWalletRequest request, String callerBpn) {
        // check base wallet
        Validate.isFalse(callerBpn.equalsIgnoreCase(miwSettings.authorityWalletBpn())).launch(new ForbiddenException(BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN));

        // check wallet already exists
        final WalletId walletId = new WalletId(request.getBpn());
        boolean exist = walletService.existsById(walletId);
        if (exist) {
            throw new DuplicateWalletProblem("Wallet is already exists for bpn " + request.getBpn());
        }
    }

    @SneakyThrows
    private String getPrivateKeyString(byte[] privateKeyBytes) {
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(new PemObject("PRIVATE KEY", privateKeyBytes));
        pemWriter.flush();
        pemWriter.close();
        return stringWriter.toString();
    }

    @SneakyThrows
    private String getPublicKeyString(byte[] publicKeyBytes) {
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        pemWriter.writeObject(new PemObject("PUBLIC KEY", publicKeyBytes));
        pemWriter.flush();
        pemWriter.close();
        return stringWriter.toString();
    }

    /**
     * Issue bpn credential
     *
     * @param holderbpn the holder wallet bpn
     * @return the verifiable credential
     */
    @SneakyThrows
    public VerifiableCredential issueBpnCredential(String holderbpn) {

        final WalletId issuerWalletId = new WalletId(miwSettings.authorityWalletBpn());
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet issuerWallet = walletService.findById(issuerWalletId)
                .orElseThrow(() -> new WalletNotFoundException(issuerWalletId));

        final WalletId holderWalletId = new WalletId(holderbpn);
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet holderWallet = walletService.findById(holderWalletId)
                .orElseThrow(() -> new WalletNotFoundException(holderWalletId));
        final Did holderDid = DidWebFactory.fromHostnameAndPath(miwSettings.host(), holderbpn);

        final Did issuerDid = DidWebFactory.fromHostnameAndPath(miwSettings.host(), issuerWallet.getWalletId().getText());
        final DidDocument issuerDidDocument = commonService.getDidDocument(issuerWallet);

        final StoredEd25519Key storedEd25519Key = issuerWallet.getStoredEd25519Keys().get(0);
        byte[] privateKeyBytes = vaultService.resolveKey(storedEd25519Key).getPrivateKey();
        List<String> types = List.of(VerifiableCredentialType.VERIFIABLE_CREDENTIAL, MIWVerifiableCredentialType.BPN_CREDENTIAL);
        VerifiableCredentialSubject verifiableCredentialSubject = new VerifiableCredentialSubject(Map.of(StringPool.TYPE, MIWVerifiableCredentialType.BPN_CREDENTIAL,
                StringPool.ID, holderDid,
                StringPool.BPN, holderbpn));
        HoldersCredential holdersCredential = CommonUtils.getHoldersCredential(verifiableCredentialSubject,
                types, issuerDidDocument, privateKeyBytes, holderDid.toString(), miwSettings.vcContexts(), miwSettings.vcExpiryDate());

        //Store Credential in holder wallet
        verifiableCredentialRepository.create(holdersCredential.getData());
        walletService.storeVerifiableCredential(holderWallet, holdersCredential.getData());

        //update summery VC
        issuersCredentialService.
                updateSummeryCredentials(issuerDidDocument, privateKeyBytes, issuerDid.toString(),
                        holderbpn, holderDid.toString(),
                        MIWVerifiableCredentialType.BPN_CREDENTIAL);

        log.debug("BPN credential issued for bpn -{}", StringEscapeUtils.escapeJava(holderbpn));

        return holdersCredential.getData();
    }

}

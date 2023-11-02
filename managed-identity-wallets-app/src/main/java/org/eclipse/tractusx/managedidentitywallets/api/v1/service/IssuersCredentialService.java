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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueDismantlerCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueMembershipCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.HoldersCredential;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.IssuersCredential;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.BadDataException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.DuplicateCredentialProblem;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.WalletNotFoundProblem;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.CommonUtils;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.Validate;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.WalletRepository;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidDocumentResolverRegistry;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidDocumentResolverRegistryImpl;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebDocumentResolver;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebFactory;
import org.eclipse.tractusx.ssi.lib.did.web.util.DidWebParser;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.did.DidDocument;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialSubject;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
import org.eclipse.tractusx.ssi.lib.proof.SignatureType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpClient;
import java.util.*;

/**
 * The type Issuers credential service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IssuersCredentialService {

    /**
     * The constant BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN.
     */
    public static final String BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN = "Base wallet BPN is not matching with request BPN(from token)";

    private final MIWSettings miwSettings;


    private final WalletKeyService walletKeyService;
    private final WalletRepository walletRepository;


    private final CommonService commonService;

    private final VerifiableCredentialService verifiableCredentialService;

    /**
     * Gets credentials.
     *
     * @param credentialId     the credential id
     * @param holderIdentifier the issuer identifier
     * @param type             the type
     * @param sortColumn       the sort column
     * @param sortType         the sort type
     * @param pageNumber       the page number
     * @param size             the size
     * @param callerBPN        the caller bpn
     * @return the credentials
     */
    public Page<VerifiableCredential> getCredentials(String credentialId, String holderIdentifier, List<String> type, String sortColumn, String sortType, int pageNumber, int size, String callerBPN) {

        if (holderIdentifier != null) {
            // addition when refactoring the API to API v2: It should not be possible to query credentials from
            // another wallet than the caller's wallet. This would leak sensitive information
            log.debug("Querying credentials from another wallet than the caller's wallet is not allowed.");
            return Page.empty();
        }

        Sort sort = Sort.unsorted();
        if (sortColumn != null) {
            final Sort.Direction direction = Sort.Direction.fromOptionalString(sortType.toUpperCase())
                    .orElse(Sort.DEFAULT_DIRECTION);

            switch (sortColumn) {
                case "createdAt":
                    sort = Sort.by(direction, VerifiableCredentialEntity.COLUMN_CREATED_AT);
                    break;
                case "credentialId":
                    sort = Sort.by(direction, VerifiableCredentialEntity.COLUMN_ID);
                    break;
                case "issuerDid":
                    log.warn("Sorting by issuer is not supported.");
                    break;
                case "type":
                    log.warn("Sorting by type is not supported. A Verifiable Credential my have multiple types.");
                    break;
                default:
                    log.warn("Sorting by {} is not supported.", sortColumn);
                    break;
            }
        }

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .verifiableCredentialId(new VerifiableCredentialId(credentialId))
                .verifiableCredentialTypes(type.stream().map(org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType::new).toList())
                .verifiableCredentialIssuer(new VerifiableCredentialIssuer(callerBPN))
                .build();

        return verifiableCredentialService
                .findAll(verifiableCredentialQuery, pageNumber, size, sort);
    }


    /**
     * Issue framework credential verifiable credential.
     *
     * @param request   the request
     * @param callerBPN the caller bpn
     * @return the verifiable credential
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public VerifiableCredential issueFrameworkCredential(IssueFrameworkCredentialRequest request, String callerBPN) {
        //validate type
        Validate.isFalse(miwSettings.supportedFrameworkVCTypes().contains(request.getType())).launch(new BadDataException("Framework credential of type " + request.getType() + " is not supported, supported values are " + miwSettings.supportedFrameworkVCTypes()));

        //Fetch Holder Wallet
        Wallet holderWallet = commonService.getWalletByIdentifier(request.getHolderIdentifier());

        Wallet baseWallet = commonService.getWalletByIdentifier(miwSettings.authorityWalletBpn());

        validateAccess(callerBPN, baseWallet);
        // get Key
        byte[] privateKeyBytes = walletKeyService.getPrivateKeyByWalletIdentifierAsBytes(baseWallet.getId());

        //if base wallet issue credentials to itself
        boolean isSelfIssued = isSelfIssued(holderWallet.getBpn());

        VerifiableCredentialSubject subject = new VerifiableCredentialSubject(Map.of(
                StringPool.TYPE, request.getType(),
                StringPool.ID, holderWallet.getDid(),
                StringPool.HOLDER_IDENTIFIER, holderWallet.getBpn(),
                StringPool.CONTRACT_TEMPLATE, request.getContractTemplate(),
                StringPool.CONTRACT_VERSION, request.getContractVersion()));
        List<String> types = List.of(VerifiableCredentialType.VERIFIABLE_CREDENTIAL, MIWVerifiableCredentialType.USE_CASE_FRAMEWORK_CONDITION);
        HoldersCredential holdersCredential = CommonUtils.getHoldersCredential(subject, types, baseWallet.getDidDocument(), privateKeyBytes, holderWallet.getDid(), miwSettings.vcContexts(), miwSettings.vcExpiryDate());

        //save in holder wallet
        verifiableCredentialService.create(holdersCredential.getData());
        var storedWallet = walletRepository.findById(new WalletId(request.getHolderIdentifier()))
                .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        walletRepository.storeVerifiableCredentialInWallet(storedWallet, holdersCredential.getData());

        //update summery cred
        updateSummeryCredentials(baseWallet.getDidDocument(), privateKeyBytes, baseWallet.getDid(), holderWallet.getBpn(), holderWallet.getDid(), request.getType());

        log.debug("Framework VC of type ->{} issued to bpn ->{}", StringEscapeUtils.escapeJava(request.getType()), StringEscapeUtils.escapeJava(holderWallet.getBpn()));

        // Return VC
        return holdersCredential.getData();
    }

    /**
     * Issue dismantler credential verifiable credential.
     *
     * @param request   the request
     * @param callerBPN the caller bpn
     * @return the verifiable credential
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public VerifiableCredential issueDismantlerCredential(IssueDismantlerCredentialRequest request, String callerBPN) {

//        //Fetch Holder Wallet
//        Wallet holderWallet = commonService.getWalletByIdentifier(request.getBpn());
//
//        // Fetch Issuer Wallet
//        Wallet issuerWallet = commonService.getWalletByIdentifier(miwSettings.authorityWalletBpn());
//
//        validateAccess(callerBPN, issuerWallet);
//
//        //check duplicate
//        isCredentialExit(holderWallet.getDid(), MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL);
//
//        byte[] privateKeyBytes = walletKeyService.getPrivateKeyByWalletIdentifierAsBytes(issuerWallet.getId());
//
//        //if base wallet issue credentials to itself
//        boolean isSelfIssued = isSelfIssued(request.getBpn());
//
//        VerifiableCredentialSubject subject = new VerifiableCredentialSubject(Map.of(StringPool.TYPE, MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL,
//                StringPool.ID, holderWallet.getDid(),
//                StringPool.HOLDER_IDENTIFIER, holderWallet.getBpn(),
//                StringPool.ACTIVITY_TYPE, request.getActivityType(),
//                StringPool.ALLOWED_VEHICLE_BRANDS, request.getAllowedVehicleBrands() == null ? Collections.emptySet() : request.getAllowedVehicleBrands()));
//        List<String> types = List.of(VerifiableCredentialType.VERIFIABLE_CREDENTIAL, MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL);
//        HoldersCredential holdersCredential = CommonUtils.getHoldersCredential(subject, types, issuerWallet.getDidDocument(), privateKeyBytes, holderWallet.getDid(), miwSettings.vcContexts(), miwSettings.vcExpiryDate(), isSelfIssued);
//
//
//        //save in holder wallet
//        holdersCredential = holdersCredentialRepository.save(holdersCredential);
//
//        //Store Credential in issuers table
//        IssuersCredential issuersCredential = IssuersCredential.of(holdersCredential);
//        issuersCredential = create(issuersCredential);
//
//        //update summery VC
//        updateSummeryCredentials(issuerWallet.getDidDocument(), privateKeyBytes, issuerWallet.getDid(), holderWallet.getBpn(), holderWallet.getDid(), MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL);
//
//        log.debug("Dismantler VC issued to bpn -> {}", StringEscapeUtils.escapeJava(request.getBpn()));
//
//        // Return VC
//        return issuersCredential.getData();
        return null;
    }

    /**
     * Issue membership credential verifiable credential.
     *
     * @param issueMembershipCredentialRequest the issue membership credential request
     * @param callerBPN                        the caller bpn
     * @return the verifiable credential
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public VerifiableCredential issueMembershipCredential(IssueMembershipCredentialRequest issueMembershipCredentialRequest, String callerBPN) {

        //Fetch Holder Wallet
//        Wallet holderWallet = commonService.getWalletByIdentifier(issueMembershipCredentialRequest.getBpn());
//
//        //check duplicate
//        isCredentialExit(holderWallet.getDid(), VerifiableCredentialType.MEMBERSHIP_CREDENTIAL);
//
//        // Fetch Issuer Wallet
//        Wallet issuerWallet = commonService.getWalletByIdentifier(miwSettings.authorityWalletBpn());
//
//        validateAccess(callerBPN, issuerWallet);
//
//        byte[] privateKeyBytes = walletKeyService.getPrivateKeyByWalletIdentifierAsBytes(issuerWallet.getId());
//        List<String> types = List.of(VerifiableCredentialType.VERIFIABLE_CREDENTIAL, VerifiableCredentialType.MEMBERSHIP_CREDENTIAL);
//
//        //if base wallet issue credentials to itself
//        boolean isSelfIssued = isSelfIssued(issueMembershipCredentialRequest.getBpn());
//
//        //VC Subject
//        VerifiableCredentialSubject verifiableCredentialSubject = new VerifiableCredentialSubject(Map.of(StringPool.TYPE, VerifiableCredentialType.MEMBERSHIP_CREDENTIAL,
//                StringPool.ID, holderWallet.getDid(),
//                StringPool.HOLDER_IDENTIFIER, holderWallet.getBpn(),
//                StringPool.MEMBER_OF, issuerWallet.getName(),
//                StringPool.STATUS, "Active",
//                StringPool.START_TIME, Instant.now().toString()));
//        HoldersCredential holdersCredential = CommonUtils.getHoldersCredential(verifiableCredentialSubject, types, issuerWallet.getDidDocument(), privateKeyBytes, holderWallet.getDid(), miwSettings.vcContexts(), miwSettings.vcExpiryDate(), isSelfIssued);
//
//
//        //save in holder wallet
//        holdersCredential = holdersCredentialRepository.save(holdersCredential);
//
//        IssuersCredential issuersCredential = IssuersCredential.of(holdersCredential);
//
//        //Store Credential in issuer table
//        issuersCredential = create(issuersCredential);
//
//        //update summery VC
//        updateSummeryCredentials(issuerWallet.getDidDocument(), privateKeyBytes, issuerWallet.getDid(), holderWallet.getBpn(), holderWallet.getDid(), VerifiableCredentialType.MEMBERSHIP_CREDENTIAL);
//
//        log.debug("Membership VC issued to bpn ->{}", StringEscapeUtils.escapeJava(issueMembershipCredentialRequest.getBpn()));
//
//        // Return VC
//        return issuersCredential.getData();
        return null;
    }


    /**
     * Issue credential using base wallet
     *
     * @param holderDid the holder did
     * @param data      the data
     * @param callerBpn the caller bpn
     * @return the verifiable credential
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public VerifiableCredential issueCredentialUsingBaseWallet(String holderDid, Map<String, Object> data, String callerBpn) {
        // Fetch Holder Wallet
        Wallet holderWallet = commonService.getWalletByIdentifier(holderDid);

        VerifiableCredential verifiableCredential = new VerifiableCredential(data);

        //Summary VC can not be issued using API, as summary VC is issuing at runtime
        verifiableCredential.getTypes().forEach(type -> Validate.isTrue(type.equals(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)).launch(new BadDataException("Can not issue " + MIWVerifiableCredentialType.SUMMARY_CREDENTIAL + " type VC using API")));

        Wallet issuerWallet = commonService.getWalletByIdentifier(verifiableCredential.getIssuer().toString());

        validateAccess(callerBpn, issuerWallet);

        // get issuer Key
        byte[] privateKeyBytes = walletKeyService.getPrivateKeyByWalletIdentifierAsBytes(issuerWallet.getId());

        boolean isSelfIssued = isSelfIssued(holderWallet.getBpn());

        // Create Credential
        HoldersCredential holdersCredential = CommonUtils.getHoldersCredential(verifiableCredential.getCredentialSubject().get(0),
                verifiableCredential.getTypes(), issuerWallet.getDidDocument(),
                privateKeyBytes,
                holderWallet.getDid(),
                verifiableCredential.getContext(), Date.from(verifiableCredential.getExpirationDate()));


        //save in holder wallet
        verifiableCredentialService.create(holdersCredential.getData());
        var wallet = walletRepository.findById(new WalletId(holderDid))
                .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        walletRepository.storeVerifiableCredentialInWallet(wallet, holdersCredential.getData());


        log.debug("VC type of {} issued to bpn ->{}", StringEscapeUtils.escapeJava(verifiableCredential.getTypes().toString()), StringEscapeUtils.escapeJava(holderWallet.getBpn()));

        // Return VC
        return holdersCredential.getData();

    }

    /**
     * Credentials validation map.
     *
     * @param data                     the data
     * @param withCredentialExpiryDate the with credential expiry date
     * @return the map
     */
    public Map<String, Object> credentialsValidation(Map<String, Object> data, boolean withCredentialExpiryDate) {
        VerifiableCredential verifiableCredential = new VerifiableCredential(data);

        // DID Resolver Constracture params
        DidDocumentResolverRegistry didDocumentResolverRegistry = new DidDocumentResolverRegistryImpl();
        didDocumentResolverRegistry.register(
                new DidWebDocumentResolver(HttpClient.newHttpClient(), new DidWebParser(), miwSettings.enforceHttps()));

        String proofTye = verifiableCredential.getProof().get(StringPool.TYPE).toString();
        LinkedDataProofValidation proofValidation;
        if (SignatureType.ED21559.toString().equals(proofTye)) {
            proofValidation = LinkedDataProofValidation.newInstance(SignatureType.ED21559, didDocumentResolverRegistry);
        } else if (SignatureType.JWS.toString().equals(proofTye)) {
            proofValidation = LinkedDataProofValidation.newInstance(SignatureType.JWS, didDocumentResolverRegistry);
        } else {
            throw new BadDataException(String.format("Invalid proof type: %s", proofTye));
        }

        boolean valid = proofValidation.verifiyProof(verifiableCredential);

        Map<String, Object> response = new TreeMap<>();

        //check expiry
        boolean dateValidation = CommonService.validateExpiry(withCredentialExpiryDate, verifiableCredential, response);

        response.put(StringPool.VALID, valid && dateValidation);
        response.put("vc", verifiableCredential);

        return response;
    }


    private void validateAccess(String callerBpn, Wallet issuerWallet) {
        //validate BPN access, VC must be issued by base wallet
        Validate.isFalse(callerBpn.equals(issuerWallet.getBpn())).launch(new ForbiddenException(BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN));

        //issuer must be base wallet
        Validate.isFalse(issuerWallet.getBpn().equals(miwSettings.authorityWalletBpn())).launch(new ForbiddenException(BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN));
    }


    private void isCredentialExit(String holderDid, String credentialType) {
        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(new WalletId(holderDid))
                .verifiableCredentialTypes(List.of(new org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType(credentialType)))
                .build();
        if (verifiableCredentialService.exists(verifiableCredentialQuery)) {
            throw new DuplicateCredentialProblem("Credential of type " + credentialType + " is already exists ");
        }
    }

    private boolean isSelfIssued(String holderBpn) {
        return holderBpn.equals(miwSettings.authorityWalletBpn());
    }


    /**
     * Update summery credentials.
     *
     * @param issuerDidDocument the issuer did document
     * @param issuerPrivateKey  the issuer private key
     * @param holderBpn         the holder bpn
     * @param holderDid         the holder did
     * @param type              the type
     */
    public void updateSummeryCredentials(DidDocument issuerDidDocument, byte[] issuerPrivateKey, String issuerDid, String holderBpn, String holderDid, String type) {

        //get last issued summary vc to holder to update items
        Optional<IssuersCredential> filter = getLastIssuedSummaryCredential(holderBpn);
        List<String> items;
        if (filter.isPresent()) {
            IssuersCredential issuersCredential = filter.get();

            //check if summery VC has subject
            Validate.isTrue(issuersCredential.getData().getCredentialSubject().isEmpty()).launch(new BadDataException("VC subject not found in existing su,,ery VC"));

            //Check if we have only one subject in summery VC
            Validate.isTrue(issuersCredential.getData().getCredentialSubject().size() > 1).launch(new BadDataException("VC subjects can more then 1 in case of summery VC"));

            VerifiableCredentialSubject subject = issuersCredential.getData().getCredentialSubject().get(0);
            if (subject.containsKey(StringPool.ITEMS)) {
                items = (List<String>) subject.get(StringPool.ITEMS);
                if (!items.contains(type)) {
                    items.add(type);
                }
            } else {
                items = List.of(type);

            }
        } else {
            items = List.of(type);
        }
        log.debug("Issuing summary VC with items ->{}", StringEscapeUtils.escapeJava(items.toString()));

        //get summery VC of holder
        VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(new WalletId(holderBpn))
                .verifiableCredentialTypes(List.of(new org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)))
                .build();
        List<VerifiableCredential> vcs = verifiableCredentialService.findAll(verifiableCredentialQuery).getContent();
        if (CollectionUtils.isEmpty(vcs)) {
            log.debug("No summery VC found for did ->{}, checking in issuer", StringEscapeUtils.escapeJava(holderDid));
        } else {
            //delete old summery VC from holder table, delete only not stored VC
            log.debug("Deleting older summary VC fir bpn -{}", holderBpn);
            vcs.forEach(verifiableCredentialService::delete);
        }

        //issue new summery VC
        final VerifiableCredentialSubject subject = new VerifiableCredentialSubject(Map.of(StringPool.ID, holderDid,
                StringPool.HOLDER_IDENTIFIER, holderBpn,
                StringPool.ITEMS, items,
                StringPool.TYPE, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL,
                StringPool.CONTRACT_TEMPLATE, miwSettings.contractTemplatesUrl()));

        final List<String> types = List.of(VerifiableCredentialType.VERIFIABLE_CREDENTIAL, MIWVerifiableCredentialType.SUMMARY_CREDENTIAL);
        final HoldersCredential holdersCredential = CommonUtils.getHoldersCredential(subject, types,
                issuerDidDocument,
                issuerPrivateKey,
                holderDid, miwSettings.summaryVcContexts(), miwSettings.vcExpiryDate());


        //save in holder wallet
        var wallet = walletRepository.findById(new WalletId(holderDid))
                .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        verifiableCredentialService.create(holdersCredential.getData());
        walletRepository.storeVerifiableCredentialInWallet(wallet, holdersCredential.getData());


        log.info("Summery VC updated for holder did -> {}", StringEscapeUtils.escapeJava(holderDid));
    }

    private Optional<IssuersCredential> getLastIssuedSummaryCredential(String holderBpn) {

        final VerifiableCredentialQuery verifiableCredentialQuery = VerifiableCredentialQuery.builder()
                .holderWalletId(new WalletId(holderBpn))
                .verifiableCredentialTypes(List.of(new org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL)))
                .build();

        return verifiableCredentialService
                .findAll(verifiableCredentialQuery, 0, 1000)
                .stream().max(Comparator.comparing(VerifiableCredential::getIssuanceDate))
                .stream().findFirst().map(summaryCredentials -> {
                    final Did holderDid = DidWebFactory.fromHostnameAndPath(miwSettings.host(), holderBpn);

                    final IssuersCredential issuersCredential = new IssuersCredential();
                    issuersCredential.setCredentialId(summaryCredentials.getId().toString());
                    issuersCredential.setData(summaryCredentials);
                    issuersCredential.setHolderDid(holderDid.toString());
                    issuersCredential.setIssuerDid(summaryCredentials.getIssuer().toString());
                    issuersCredential.setType(MIWVerifiableCredentialType.SUMMARY_CREDENTIAL);

                    return issuersCredential;
                });
    }
}
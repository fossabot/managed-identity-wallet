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
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.MIWVerifiableCredentialType;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueDismantlerCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueFrameworkCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.dto.IssueMembershipCredentialRequest;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.BadDataException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.DuplicateCredentialProblem;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.ForbiddenException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.WalletNotFoundProblem;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.Validate;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialIssuer;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.repository.entity.VerifiableCredentialEntity;
import org.eclipse.tractusx.managedidentitywallets.repository.query.VerifiableCredentialQuery;
import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.DismantlerVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.FrameworkVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.GenericVerifiableCredentialFactory;
import org.eclipse.tractusx.managedidentitywallets.factory.verifiableDocuments.MembershipVerifiableCredentialFactory;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidResolver;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebResolver;
import org.eclipse.tractusx.ssi.lib.did.web.util.DidWebParser;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredentialType;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
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

    private final WalletService walletService;


    private final CommonService commonService;

    private final VerifiableCredentialService verifiableCredentialService;
    private final MembershipVerifiableCredentialFactory membershipVerifiableCredentialFactory;
    private final DismantlerVerifiableCredentialFactory dismantlerVerifiableCredentialFactory;
    private final FrameworkVerifiableCredentialFactory frameworkVerifiableCredentialFactory;
    private final GenericVerifiableCredentialFactory genericVerifiableCredentialFactory;

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
        Validate.isFalse(miwSettings.getSupportedFrameworkVCTypes().contains(request.getType())).launch(new BadDataException("Framework credential of type " + request.getType() + " is not supported, supported values are " + miwSettings.getSupportedFrameworkVCTypes()));

        //Fetch Holder Wallet
        Wallet holderWallet = commonService.getWalletByIdentifier(request.getHolderIdentifier());

        Wallet baseWallet = commonService.getWalletByIdentifier(miwSettings.getAuthorityWalletBpn());

        validateAccess(callerBPN, baseWallet);

        final org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType verifiableCredentialType = new org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialType(request.getType());
        final String contractTemplate = request.getContractTemplate();
        final String contractVersion = request.getContractVersion();
        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet = walletService.findById(new WalletId(request.getHolderIdentifier()))
                .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        final VerifiableCredential verifiableCredential = frameworkVerifiableCredentialFactory.createFrameworkVerifiableCredential(
                wallet, verifiableCredentialType, contractTemplate, contractVersion);

        //save in holder wallet
        verifiableCredentialService.create(verifiableCredential);
        walletService.storeVerifiableCredential(wallet, verifiableCredential);

        log.debug("Framework VC of type ->{} issued to bpn ->{}", StringEscapeUtils.escapeJava(request.getType()), StringEscapeUtils.escapeJava(holderWallet.getBpn()));

        // Return VC
        return verifiableCredential;
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

        //Fetch Holder Wallet
        Wallet holderWallet = commonService.getWalletByIdentifier(request.getBpn());

        // Fetch Issuer Wallet
        Wallet issuerWallet = commonService.getWalletByIdentifier(miwSettings.getAuthorityWalletBpn());

        validateAccess(callerBPN, issuerWallet);

        //check duplicate
        isCredentialExit(holderWallet.getDid(), MIWVerifiableCredentialType.DISMANTLER_CREDENTIAL);


        final String activityType = request.getActivityType();
        final List<String> allowedVehicleBrands =
                Optional.ofNullable(request.getAllowedVehicleBrands()).orElse(Set.of())
                        .stream().toList();

        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet =
                walletService.findById(new WalletId(request.getBpn()))
                        .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        final VerifiableCredential verifiableCredential = dismantlerVerifiableCredentialFactory.createDismantlerVerifiableCredential(wallet, activityType, allowedVehicleBrands);

        verifiableCredentialService.create(verifiableCredential);
        walletService.storeVerifiableCredential(wallet, verifiableCredential);

        log.debug("Dismantler VC issued to bpn -> {}", StringEscapeUtils.escapeJava(request.getBpn()));

        // Return VC
        return verifiableCredential;
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

        // Fetch Holder Wallet
        Wallet holderWallet = commonService.getWalletByIdentifier(issueMembershipCredentialRequest.getBpn());

        //check duplicate
        isCredentialExit(holderWallet.getDid(), VerifiableCredentialType.MEMBERSHIP_CREDENTIAL);

        // Fetch Issuer Wallet
        Wallet issuerWallet = commonService.getWalletByIdentifier(miwSettings.getAuthorityWalletBpn());

        validateAccess(callerBPN, issuerWallet);

        final org.eclipse.tractusx.managedidentitywallets.models.Wallet wallet =
                walletService.findById(new WalletId(issueMembershipCredentialRequest.getBpn()))
                        .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        final VerifiableCredential verifiableCredential = membershipVerifiableCredentialFactory.createMembershipVerifiableCredential(wallet);

        verifiableCredentialService.create(verifiableCredential);
        walletService.storeVerifiableCredential(wallet, verifiableCredential);

        log.debug("Membership VC issued to bpn ->{}", StringEscapeUtils.escapeJava(issueMembershipCredentialRequest.getBpn()));

        return verifiableCredential;
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


        final org.eclipse.tractusx.managedidentitywallets.models.Wallet issuerWalletRealDomain = walletService.findById(new WalletId(callerBpn)).orElseThrow();

        final GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs factoryArgs =
                GenericVerifiableCredentialFactory.GenericVerifiableCredentialFactoryArgs.builder()
                        .subjects(verifiableCredential.getCredentialSubject())
                        .issuerWallet(issuerWalletRealDomain)
                        .build();

        // Create Credential
        final VerifiableCredential holdersCredential = genericVerifiableCredentialFactory.createVerifiableCredential(factoryArgs);

        //save in holder wallet
        verifiableCredentialService.create(holdersCredential);
        var wallet = walletService.findById(new WalletId(holderDid))
                .orElseThrow(() -> new WalletNotFoundProblem("Wallet not found"));
        walletService.storeVerifiableCredential(wallet, holdersCredential);

        log.debug("VC type of {} issued to bpn ->{}", StringEscapeUtils.escapeJava(verifiableCredential.getTypes().toString()), StringEscapeUtils.escapeJava(holderWallet.getBpn()));

        // Return VC
        return holdersCredential;

    }

    /**
     * Credentials validation map.
     *
     * @param data                     the data
     * @param withCredentialExpiryDate the with credential expiry date
     * @return the map
     */
    public Map<String, Object> credentialsValidation(Map<String, Object> data, boolean withCredentialExpiryDate) {
        final VerifiableCredential verifiableCredential = new VerifiableCredential(data);
        final DidResolver didResolver = new DidWebResolver(HttpClient.newHttpClient(), new DidWebParser(), miwSettings.isEnforceHttps());
        final LinkedDataProofValidation proofValidation = LinkedDataProofValidation.newInstance(didResolver);

        boolean valid = proofValidation.verifiy(verifiableCredential);

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
        Validate.isFalse(issuerWallet.getBpn().equals(miwSettings.getAuthorityWalletBpn())).launch(new ForbiddenException(BASE_WALLET_BPN_IS_NOT_MATCHING_WITH_REQUEST_BPN_FROM_TOKEN));
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
}
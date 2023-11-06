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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.api.v1.constant.StringPool;
import org.eclipse.tractusx.managedidentitywallets.api.v1.entity.Wallet;
import org.eclipse.tractusx.managedidentitywallets.api.v1.exception.BadDataException;
import org.eclipse.tractusx.managedidentitywallets.api.v1.utils.Validate;
import org.eclipse.tractusx.managedidentitywallets.config.MIWSettings;
import org.eclipse.tractusx.managedidentitywallets.exception.Ed25519KeyNotFoundException;
import org.eclipse.tractusx.managedidentitywallets.models.ResolvedEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.StoredEd25519Key;
import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
import org.eclipse.tractusx.managedidentitywallets.service.VaultService;
import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
import org.eclipse.tractusx.ssi.lib.crypt.octet.OctetKeyPairFactory;
import org.eclipse.tractusx.ssi.lib.crypt.x21559.x21559PrivateKey;
import org.eclipse.tractusx.ssi.lib.did.resolver.DidResolver;
import org.eclipse.tractusx.ssi.lib.did.web.DidWebResolver;
import org.eclipse.tractusx.ssi.lib.did.web.util.DidWebParser;
import org.eclipse.tractusx.ssi.lib.exception.InvalidJsonLdException;
import org.eclipse.tractusx.ssi.lib.exception.InvalidePrivateKeyFormat;
import org.eclipse.tractusx.ssi.lib.exception.UnsupportedSignatureTypeException;
import org.eclipse.tractusx.ssi.lib.jwt.SignedJwtFactory;
import org.eclipse.tractusx.ssi.lib.jwt.SignedJwtValidator;
import org.eclipse.tractusx.ssi.lib.jwt.SignedJwtVerifier;
import org.eclipse.tractusx.ssi.lib.model.did.Did;
import org.eclipse.tractusx.ssi.lib.model.did.DidParser;
import org.eclipse.tractusx.ssi.lib.model.verifiable.credential.VerifiableCredential;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentation;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentationBuilder;
import org.eclipse.tractusx.ssi.lib.model.verifiable.presentation.VerifiablePresentationType;
import org.eclipse.tractusx.ssi.lib.proof.LinkedDataProofValidation;
import org.eclipse.tractusx.ssi.lib.serialization.jsonLd.JsonLdSerializerImpl;
import org.eclipse.tractusx.ssi.lib.serialization.jwt.SerializedJwtPresentationFactory;
import org.eclipse.tractusx.ssi.lib.serialization.jwt.SerializedJwtPresentationFactoryImpl;
import org.eclipse.tractusx.ssi.lib.serialization.jwt.SerializedVerifiablePresentation;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.*;

/**
 * The type Presentation service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PresentationService {

    private final CommonService commonService;

    private final VaultService vaultService;
    private final WalletService walletService;

    private final MIWSettings miwSettings;

    /**
     * Create presentation map.
     *
     * @param data      the data
     * @param asJwt     the as jwt
     * @param audience  the audience
     * @param callerBpn the caller bpn
     * @return the map
     */
    @SneakyThrows({InvalidePrivateKeyFormat.class, Ed25519KeyNotFoundException.class})
    public Map<String, Object> createPresentation(Map<String, Object> data, boolean asJwt, String audience, String callerBpn) {
        List<Map<String, Object>> verifiableCredentialList = (List<Map<String, Object>>) data.get(StringPool.VERIFIABLE_CREDENTIALS);

        //check if holder wallet is in the system
        Wallet callerWallet = commonService.getWalletByIdentifier(callerBpn);

        List<VerifiableCredential> verifiableCredentials = new ArrayList<>(verifiableCredentialList.size());
        verifiableCredentialList.forEach(map -> {
            VerifiableCredential verifiableCredential = new VerifiableCredential(map);
            verifiableCredentials.add(verifiableCredential);
        });

        Map<String, Object> response = new HashMap<>();
        if (asJwt) {
            log.debug("Creating VP as JWT for bpn ->{}", callerBpn);
            Validate.isFalse(StringUtils.hasText(audience)).launch(new BadDataException("Audience needed to create VP as JWT"));

            //Issuer of VP is holder of VC
            Did vpIssuerDid = DidParser.parse(callerWallet.getDid());

            //JWT Factory
            SerializedJwtPresentationFactory presentationFactory = new SerializedJwtPresentationFactoryImpl(
                    new SignedJwtFactory(new OctetKeyPairFactory()), new JsonLdSerializerImpl(), vpIssuerDid);

            //Build JWT

            final org.eclipse.tractusx.managedidentitywallets.models.Wallet domainWallet = walletService.findById(new WalletId(callerBpn)).orElseThrow();
            final StoredEd25519Key latestKey = domainWallet.getStoredEd25519Keys().stream().max(Comparator.comparing(org.eclipse.tractusx.managedidentitywallets.models.Ed25519Key::getCreatedAt)).orElseThrow();
            final ResolvedEd25519Key resolvedEd25519Key = vaultService.resolveKey(latestKey);

            x21559PrivateKey privateKey = new x21559PrivateKey(resolvedEd25519Key.getPrivateKey());
            SignedJWT presentation = presentationFactory.createPresentation(vpIssuerDid
                    , verifiableCredentials, audience, privateKey);

            response.put(StringPool.VP, presentation.serialize());
        } else {
            log.debug("Creating VP as JSON-LD for bpn ->{}", callerBpn);
            VerifiablePresentationBuilder verifiablePresentationBuilder =
                    new VerifiablePresentationBuilder();

            // Build VP
            VerifiablePresentation verifiablePresentation =
                    verifiablePresentationBuilder
                            .id(URI.create(miwSettings.authorityWalletDid() + "#" + UUID.randomUUID().toString()))
                            .type(List.of(VerifiablePresentationType.VERIFIABLE_PRESENTATION))
                            .verifiableCredentials(verifiableCredentials)
                            .build();
            response.put(StringPool.VP, verifiablePresentation);
        }
        return response;
    }


    /**
     * Validate presentation map.
     *
     * @param vp                       the vp
     * @param asJwt                    the as jwt
     * @param withCredentialExpiryDate the with credential expiry date
     * @param audience                 the audience
     * @return the map
     */
    @SneakyThrows
    public Map<String, Object> validatePresentation(Map<String, Object> vp, boolean asJwt, boolean withCredentialExpiryDate, String audience) {

        Map<String, Object> response = new HashMap<>();
        if (asJwt) {
            log.debug("Validating VP as JWT");
            //verify as jwt
            Validate.isNull(vp.get(StringPool.VP)).launch(new BadDataException("Can not find JWT"));
            String jwt = vp.get(StringPool.VP).toString();
            response.put(StringPool.VP, jwt);

            SignedJWT signedJWT = SignedJWT.parse(jwt);

            boolean validateSignature = validateSignature(signedJWT);

            //validate audience
            boolean validateAudience = validateAudience(audience, signedJWT);

            //validate jwt date
            boolean validateJWTExpiryDate = validateJWTExpiryDate(signedJWT);
            response.put(StringPool.VALIDATE_JWT_EXPIRY_DATE, validateJWTExpiryDate);

            boolean validCredential = true;
            boolean validateExpiryDate = true;
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> claims = mapper.readValue(signedJWT.getPayload().toBytes(), Map.class);
                String vpClaim = mapper.writeValueAsString(claims.get("vp"));

                JsonLdSerializerImpl jsonLdSerializer = new JsonLdSerializerImpl();
                VerifiablePresentation presentation = jsonLdSerializer.deserializePresentation(new SerializedVerifiablePresentation(vpClaim));

                for (VerifiableCredential credential : presentation.getVerifiableCredentials()) {
                    validateExpiryDate = CommonService.validateExpiry(withCredentialExpiryDate, credential, response);
                    if (!validateCredential(credential)) {
                        validCredential = false;
                    }
                }
            } catch (InvalidJsonLdException e) {
                throw new BadDataException(String.format("Validation of VP in form of JSON-LD is not supported. Invalid Json-LD: %s", e.getMessage()));
            }

            response.put(StringPool.VALID, (validateSignature && validateAudience && validateExpiryDate && validCredential && validateJWTExpiryDate));

            if (StringUtils.hasText(audience)) {
                response.put(StringPool.VALIDATE_AUDIENCE, validateAudience);

            }

        } else {
            log.debug("Validating VP as json-ld");
            throw new BadDataException("Validation of VP in form of JSON-LD is not supported");
        }

        return response;
    }

    private boolean validateSignature(SignedJWT signedJWT) {
        //validate jwt signature
        try {
            final DidResolver didResolver = new DidWebResolver(HttpClient.newHttpClient(), new DidWebParser(), miwSettings.enforceHttps());

            SignedJwtVerifier jwtVerifier = new SignedJwtVerifier(didResolver);
            return jwtVerifier.verify(signedJWT);
        } catch (Exception e) {
            log.error("Can not verify signature of jwt", e);
            return false;
        }
    }

    private boolean validateJWTExpiryDate(SignedJWT signedJWT) {
        try {
            SignedJwtValidator jwtValidator = new SignedJwtValidator();
            jwtValidator.validateDate(signedJWT);
            return true;
        } catch (Exception e) {
            log.error("Can not expiry date ", e);
            return false;
        }
    }

    private boolean validateAudience(String audience, SignedJWT signedJWT) {
        if (StringUtils.hasText(audience)) {
            try {
                SignedJwtValidator jwtValidator = new SignedJwtValidator();
                jwtValidator.validateAudiences(signedJWT, audience);
                return true;
            } catch (Exception e) {
                log.error("Can not validate audience ", e);
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean validateCredential(VerifiableCredential credential) throws UnsupportedSignatureTypeException {

        final DidResolver didResolver = new DidWebResolver(HttpClient.newHttpClient(), new DidWebParser(), miwSettings.enforceHttps());
        final LinkedDataProofValidation linkedDataProofValidation = LinkedDataProofValidation.newInstance(didResolver);

        boolean isValid = linkedDataProofValidation.verifiy(credential);
        if (isValid) {
            log.debug("Credential validation result: (valid: {}, credential-id: {})", isValid, credential.getId());
        } else {
            log.info("Credential validation result: (valid: {}, credential-id: {})", isValid, credential.getId());
        }
        return isValid;
    }
}

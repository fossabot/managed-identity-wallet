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

package org.eclipse.tractusx.managedidentitywallets.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.managedidentitywallets.exception.*;
import org.eclipse.tractusx.managedidentitywallets.spring.controllers.v2.AdministratorApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice(basePackageClasses = {AdministratorApi.class})
public class DefaultErrorHandlerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {WalletNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<Object> handleWalletDoesNotExistException(WalletNotFoundException ex) {
        if (log.isDebugEnabled()) {
            log.debug("WalletNotFoundException: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.status(404).body(createMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {VerifiableCredentialNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<Object> handleVerifiableCredentialDoesNotExistException(VerifiableCredentialNotFoundException ex) {
        if (log.isDebugEnabled()) {
            log.debug("VerifiableCredentialNotFoundException: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.status(404).body(createMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {VerifiableCredentialAlreadyExistsException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<Object> handleVerifiableCredentialAlreadyExistsException(VerifiableCredentialAlreadyExistsException ex) {
        if (log.isDebugEnabled()) {
            log.debug("VerifiableCredentialAlreadyExistsException: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.status(409).body(createMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {WalletAlreadyExistsException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<Object> handleWalletAlreadyExistsException(WalletAlreadyExistsException ex) {
        if (log.isDebugEnabled()) {
            log.debug("WalletAlreadyExistsException: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.status(409).body(createMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {VerifiableCredentialAlreadyStoredInWalletException.class})
    @ResponseStatus(value = HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<Object> handleVerifiableCredentialAlreadyStoredInWalletException(VerifiableCredentialAlreadyStoredInWalletException ex) {
        if (log.isDebugEnabled()) {
            log.debug("VerifiableCredentialAlreadyStoredInWallet: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.status(409).body(createMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        if (log.isDebugEnabled()) {
            log.debug("ConstraintViolationException: {}", ex.getMessage(), ex);
        }
        return ResponseEntity.badRequest().body(createMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {UnexpectedRollbackException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<Object> handleUnexpectedRollbackException(UnexpectedRollbackException ex) {
        log.error("UnexpectedRollbackException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(409).body(createMessage(ex.getMessage()));
    }

    private static Object createMessage(String message) {
        return Map.of("message", message);
    }
}

package org.eclipse.tractusx.managedidentitywallets.exception;

import lombok.RequiredArgsConstructor;

/**
 * This exception is thrown when a BPN attribute is required but not present in the bearer token.
 */
@RequiredArgsConstructor
public class BpnAttributeMissingException extends RuntimeException {
}

package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

@RequiredArgsConstructor
public class SecurityFilterRegistration {
  @Getter private final AbstractAuthenticationProcessingFilter filter;
}

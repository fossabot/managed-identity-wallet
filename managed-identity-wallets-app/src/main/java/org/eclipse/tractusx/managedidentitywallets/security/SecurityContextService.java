package org.eclipse.tractusx.managedidentitywallets.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityContextService {

  public Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
        .map(SecurityContext::getAuthentication);
  }

  public Optional<AuthenticationToken> getAuthenticationToken() {
    return getAuthentication()
        .filter(a -> a instanceof AuthenticationToken)
        .map(AuthenticationToken.class::cast);
  }

  public Optional<AuthenticationPrincipal> getDeguraPrincipal() {
    return getAuthenticationToken()
        .map(AuthenticationToken::getPrincipal)
        .filter(a -> a instanceof AuthenticationPrincipal)
        .map(AuthenticationPrincipal.class::cast);
  }

  public Optional<AuthenticationModel> getAuthenticationModel() {
    return getDeguraPrincipal().map(AuthenticationPrincipal::getAuthenticationModel);
  }
}

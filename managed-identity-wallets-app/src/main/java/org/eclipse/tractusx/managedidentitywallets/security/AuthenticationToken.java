package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticationToken extends AbstractAuthenticationToken {
  private final AuthenticationPrincipal authenticationPrincipal;

  public AuthenticationToken(
      @NonNull final AuthenticationPrincipal authenticationPrincipal,
      @NonNull final Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.authenticationPrincipal = authenticationPrincipal;
    super.setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return authenticationPrincipal;
  }

  public AuthenticationPrincipal getAuthenticationPrincipal() {
    return authenticationPrincipal;
  }
}

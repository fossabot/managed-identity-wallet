package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor
public class AuthenticationPrincipal implements Principal {
  @NonNull
  @Getter private final AuthenticationModel authenticationModel;

  @Override
  public String getName() {
    return authenticationModel.getSubject();
  }
}

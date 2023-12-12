package org.eclipse.tractusx.managedidentitywallets.security;

import java.io.Serializable;

public interface PermissionRule {
  String getDomain();

  String getAction();

  boolean hasPermission(AuthenticationModel authentication);

  boolean hasPermission(AuthenticationModel authentication, Serializable id);
}

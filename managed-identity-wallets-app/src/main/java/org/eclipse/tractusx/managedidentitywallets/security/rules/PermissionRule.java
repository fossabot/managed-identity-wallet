package org.eclipse.tractusx.managedidentitywallets.security.rules;

import org.eclipse.tractusx.managedidentitywallets.security.ApplicationPrincipal;

import java.io.Serializable;

public interface PermissionRule {
  String getDomain();

  String getAction();

  boolean hasPermission(ApplicationPrincipal principal);

  boolean hasPermission(ApplicationPrincipal principal, Serializable id);
}

package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.Data;

@Data
public class AuthenticationModel {
  private String subject;

  private String bpn;
}

package org.eclipse.tractusx.managedidentitywallets.api.v2;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ApiRolesV2 {
    public static final String ADMIN = "miw:admin";
    public static final String ADMIN_ROLE = "ROLE_" + ADMIN;
    public static final String WALLET_OWNER = "miw:user";
    public static final String WALLET_OWNER_ROLE = "ROLE_" + WALLET_OWNER;
}

package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApplicationAuthenticationToken extends AbstractAuthenticationToken {

    private final ApplicationPrincipal principal;

    public ApplicationAuthenticationToken(
            @NonNull ApplicationPrincipal principal,
            @NonNull Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        super.setAuthenticated(true);
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }


    public ApplicationPrincipal getAuthenticationPrincipal() {
        return principal;
    }
}

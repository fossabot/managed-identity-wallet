package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ApplicationPrincipal implements Principal {
    @NonNull
    private final String name;
    @NonNull
    private final String bpn;
}

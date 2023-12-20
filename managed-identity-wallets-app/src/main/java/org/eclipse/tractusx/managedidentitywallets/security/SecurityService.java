package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.security.ApplicationAuthenticationToken;
import org.eclipse.tractusx.managedidentitywallets.security.ApplicationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityService {

    public Optional<String> getBpn() {
        return getPrincipal()
                .map(ApplicationPrincipal::getBpn);
    }

    public Optional<ApplicationPrincipal> getPrincipal() {
        return getAuthenticationToken()
                .map(ApplicationAuthenticationToken::getPrincipal)
                .filter(ApplicationPrincipal.class::isInstance)
                .map(ApplicationPrincipal.class::cast);
    }

    public Optional<ApplicationAuthenticationToken> getAuthenticationToken() {
        return getAuthentication().filter(authentication -> authentication instanceof ApplicationAuthenticationToken)
                .map(authentication -> (ApplicationAuthenticationToken) authentication);
    }

    public Optional<Authentication> getAuthentication() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication);
    }
}

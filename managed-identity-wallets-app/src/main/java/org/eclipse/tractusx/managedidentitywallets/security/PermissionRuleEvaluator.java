package org.eclipse.tractusx.managedidentitywallets.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
public class PermissionRuleEvaluator implements PermissionEvaluator {

  @NonNull private final List<PermissionRule> permissionRuleSet;

  @Override
  public boolean hasPermission(
      final Authentication authentication, final Object domain, final Object action) {
    if ((authentication == null) || !(domain instanceof String) || !(action instanceof String)) {
      return false;
    }

    if (!(authentication instanceof AuthenticationToken)) {
      return false;
    }

    final List<PermissionRule> permissionRules =
        permissionRuleSet.stream()
            .filter(
                e ->
                    e.getDomain() != null
                        && e.getDomain().equals(domain)
                        && e.getAction() != null
                        && e.getAction().equals(action))
            .toList();

    if (permissionRules.isEmpty()) {
      return false;
    }

    final AuthenticationModel authenticationModel =
        ((AuthenticationToken) authentication)
            .getAuthenticationPrincipal()
            .getAuthenticationModel();

    return permissionRules.stream().allMatch(p -> p.hasPermission(authenticationModel));
  }

  @Override
  public boolean hasPermission(
      final Authentication authentication,
      final Serializable targetId,
      final String domain,
      final Object action) {

    if ((authentication == null)
        || domain == null
        || !(action instanceof String)
        || targetId == null) {
      return false;
    }

    if (!(authentication instanceof AuthenticationToken)) {
      return false;
    }

    final List<PermissionRule> permissionRules =
        permissionRuleSet.stream()
            .filter(
                e ->
                    e.getDomain() != null
                        && e.getDomain().equals(domain)
                        && e.getAction() != null
                        && e.getAction().equals(action))
            .toList();

    if (permissionRules.isEmpty()) {
      return false;
    }

    final AuthenticationModel authenticationModel =
            ((AuthenticationToken) authentication)
                    .getAuthenticationPrincipal()
                    .getAuthenticationModel();

    return permissionRules.stream().allMatch(p -> p.hasPermission(authenticationModel, targetId));
  }
}

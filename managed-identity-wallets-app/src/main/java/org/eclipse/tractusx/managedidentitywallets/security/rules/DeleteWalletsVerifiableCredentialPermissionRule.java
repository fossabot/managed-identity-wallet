//package org.eclipse.tractusx.managedidentitywallets.security.rules;
//
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//import org.eclipse.tractusx.managedidentitywallets.models.VerifiableCredentialId;
//import org.eclipse.tractusx.managedidentitywallets.models.WalletId;
//import org.eclipse.tractusx.managedidentitywallets.security.ApplicationPrincipal;
//import org.eclipse.tractusx.managedidentitywallets.service.VerifiableCredentialService;
//import org.eclipse.tractusx.managedidentitywallets.service.WalletService;
//import org.springframework.stereotype.Component;
//
//import java.io.Serializable;
//
//@Component
//@RequiredArgsConstructor
//public class DeleteWalletsVerifiableCredentialPermissionRule implements PermissionRule {
//
//    private final WalletService walletService;
//    private final VerifiableCredentialService verifiableCredentialService;
//
//    @Override
//    public String getDomain() {
//        return "verifiableCredential";
//    }
//
//    @Override
//    public String getAction() {
//        return "delete";
//    }
//
//    @Override
//    public boolean hasPermission(@NonNull ApplicationPrincipal principal) {
//        // without telling which verifiable credential to modify, we can't check the permission
//        return false;
//    }
//
//    @Override
//    public boolean hasPermission(@NonNull ApplicationPrincipal principal, @NonNull Serializable id) {
//        if (principal.getBpn() == null || !(id instanceof String)) {
//            return false;
//        }
//
//        final WalletId walletId = new WalletId(principal.getBpn());
//        final VerifiableCredentialId verifiableCredentialId = new VerifiableCredentialId((String) id);
//
//        return !verifiableCredentialService.existsById(verifiableCredentialId) ||
//                walletService.containsVerifiableCredential(walletId, verifiableCredentialId);
//    }
//}

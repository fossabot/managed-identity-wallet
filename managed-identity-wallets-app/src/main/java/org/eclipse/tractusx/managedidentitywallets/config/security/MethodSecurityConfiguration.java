package org.eclipse.tractusx.managedidentitywallets.config.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.managedidentitywallets.security.PermissionRule;
import org.eclipse.tractusx.managedidentitywallets.security.PermissionRuleEvaluator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.lang.Nullable;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(proxyTargetClass = true)
public class MethodSecurityConfiguration {

  private final List<PermissionRule> permissionRules;

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public AbstractSecurityExpressionHandlerBeanPostprocessor abstractSecurityExpressionHandlerBeanPostprocessor() {
    return new AbstractSecurityExpressionHandlerBeanPostprocessor(permissionRules);
  }

  @RequiredArgsConstructor
  static class AbstractSecurityExpressionHandlerBeanPostprocessor implements BeanPostProcessor {
    @NonNull
    private final List<PermissionRule> permissionRules;

    @Getter(AccessLevel.PRIVATE)
    private final PermissionRuleEvaluator permissionRuleEvaluator = createPermissionRuleEvaluator();
    private PermissionRuleEvaluator createPermissionRuleEvaluator() {
      return new PermissionRuleEvaluator(Objects.requireNonNull(permissionRules));
    }

    @Nullable
    public Object postProcessAfterInitialization(@NonNull final Object bean, @NonNull final String beanName) throws BeansException {
      if(bean instanceof AbstractSecurityExpressionHandler) {
        ((AbstractSecurityExpressionHandler<?>) bean)
            .setPermissionEvaluator(getPermissionRuleEvaluator());
      }
      return bean;
    }
  }
}

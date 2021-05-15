package io.vaibhavshukla.multitenantservice.multitenant.config;


import io.vaibhavshukla.multitenantservice.multitenant.util.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("currentTenantIdentifierResolver")
public class CurrrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();

        if (tenantId != null && tenantId.length() != 0  ) {
            return tenantId;
        }else {
            return "BOOTSTRAP";
        }
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}

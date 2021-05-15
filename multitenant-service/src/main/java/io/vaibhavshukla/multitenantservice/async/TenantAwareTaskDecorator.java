package io.vaibhavshukla.multitenantservice.async;

import io.vaibhavshukla.multitenantservice.multitenant.util.TenantContext;
import org.springframework.core.task.TaskDecorator;

public class TenantAwareTaskDecorator implements TaskDecorator {


    @Override
    public Runnable decorate(Runnable runnable) {
        String tenantId = TenantContext.getTenantId();

        return () -> {
            try {
                TenantContext.setTenantId(tenantId);
                runnable.run();
            } finally {
                TenantContext.setTenantId(null);
            }
        };

    }
}

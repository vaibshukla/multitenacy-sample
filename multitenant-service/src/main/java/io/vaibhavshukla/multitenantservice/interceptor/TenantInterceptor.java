package io.vaibhavshukla.multitenantservice.interceptor;

import io.vaibhavshukla.multitenantservice.multitenant.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.util.Optional;

@Slf4j
@Component
public class TenantInterceptor implements WebRequestInterceptor {

    @Override
    public void preHandle(WebRequest request) throws Exception {
       Optional<String> tenant =  Optional.ofNullable(request.getHeader("X-Tenant-Id"));
       log.info("ServerName :{} " , ((ServletWebRequest)request).getRequest().getServerName().split("\\.")[0] );
       String tenantId = tenant.orElse(((ServletWebRequest)request).getRequest().getServerName().split("\\.")[0]);
       TenantContext.setTenantId(tenantId);
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {

    }
}

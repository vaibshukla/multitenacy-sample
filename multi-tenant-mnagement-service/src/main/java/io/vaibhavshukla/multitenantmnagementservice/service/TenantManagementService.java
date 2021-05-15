package io.vaibhavshukla.multitenantmnagementservice.service;

import io.vaibhavshukla.multitenantmnagementservice.entity.Tenant;
import io.vaibhavshukla.multitenantmnagementservice.vo.TenantCreationVO;
import io.vaibhavshukla.multitenantmnagementservice.vo.TenantsVo;

import java.util.List;

public interface TenantManagementService {
    void createTeanant(TenantCreationVO tenantCreationVO);

    List<TenantsVo> getAllTenants();
}

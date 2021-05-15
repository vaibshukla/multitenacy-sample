package io.vaibhavshukla.multitenantmnagementservice.vo;

import lombok.Data;

@Data
public class TenantCreationVO {

    private String tenantId;

    private String db;

    private String password;
}

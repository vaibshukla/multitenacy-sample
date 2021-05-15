package io.vaibhavshukla.multitenantmnagementservice.vo;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TenantsVo {

    private String tenantId;

    private String identifier;
}

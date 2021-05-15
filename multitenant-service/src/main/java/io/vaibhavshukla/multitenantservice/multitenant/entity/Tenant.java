package io.vaibhavshukla.multitenantservice.multitenant.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Data
@Entity
public class Tenant {

    @Id
    @Column(name = "tenant_id" , length = 256)
    @Size(max = 256)
    private String tenantId;

    @Size(max = 256)
    @Column(name = "db" , length = 256)
    private String db;

    @Size(max = 256 )
    @Column(name = "password" , length = 256)
    private String password;

    @Size(max = 256)
    @Column(name = "url" , length = 256)
    private String url;
}

package io.vaibhavshukla.multitenantmnagementservice.repository;

import io.vaibhavshukla.multitenantmnagementservice.entity.Tenant;
import org.springframework.data.repository.CrudRepository;

public interface TenantRepository extends CrudRepository<Tenant , String> {

}

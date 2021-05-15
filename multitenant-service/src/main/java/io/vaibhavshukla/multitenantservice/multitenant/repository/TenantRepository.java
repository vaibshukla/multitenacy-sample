package io.vaibhavshukla.multitenantservice.multitenant.repository;


import io.vaibhavshukla.multitenantservice.multitenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant , String> {

    Optional<Tenant> findByTenantId(String tenantId);
}

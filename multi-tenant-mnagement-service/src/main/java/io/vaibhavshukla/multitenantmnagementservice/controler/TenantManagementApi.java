package io.vaibhavshukla.multitenantmnagementservice.controler;

import io.vaibhavshukla.multitenantmnagementservice.service.TenantManagementService;
import io.vaibhavshukla.multitenantmnagementservice.vo.TenantCreationVO;
import io.vaibhavshukla.multitenantmnagementservice.vo.TenantsVo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/management/tenants")
@AllArgsConstructor
public class TenantManagementApi {

    private final TenantManagementService tenantManagementService;

    @PostMapping
    public ResponseEntity<String> createTenant(@RequestBody TenantCreationVO tenant) {
        tenantManagementService.createTeanant(tenant);
        return ResponseEntity.status(HttpStatus.OK).body("Teanant is created");
    }

    @GetMapping
    public ResponseEntity<List<TenantsVo>> getTenants() {
       var tenant =  tenantManagementService.getAllTenants();
       return ResponseEntity.status(HttpStatus.OK).body(tenant);
    }
}

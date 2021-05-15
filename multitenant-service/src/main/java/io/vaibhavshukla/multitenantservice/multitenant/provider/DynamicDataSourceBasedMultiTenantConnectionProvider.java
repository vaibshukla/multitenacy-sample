package io.vaibhavshukla.multitenantservice.multitenant.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.zaxxer.hikari.HikariDataSource;
import io.vaibhavshukla.multitenantservice.multitenant.entity.Tenant;
import io.vaibhavshukla.multitenantservice.multitenant.repository.TenantRepository;
import io.vaibhavshukla.multitenantservice.multitenant.service.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DynamicDataSourceBasedMultiTenantConnectionProvider
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final String TENANT_POOL_NAME_SUFFIX = "DataSource";

    private final DataSource masterDataSource;

    private final DataSourceProperties dataSourceProperties;

    private final TenantRepository masterTenantRepository;

    private final EncryptionService encryptionService;

    public DynamicDataSourceBasedMultiTenantConnectionProvider(@Qualifier("masterDataSource") DataSource masterDataSource,
                                                               @Qualifier("masterDataSourceProperties") DataSourceProperties dataSourceProperties,
                                                               TenantRepository masterTenantRepository,
                                                               EncryptionService encryptionService) {
        this.masterDataSource = masterDataSource;
        this.dataSourceProperties = dataSourceProperties;
        this.masterTenantRepository = masterTenantRepository;
        this.encryptionService = encryptionService;
    }

    @Value("${multitenancy.datasource-cache.maximumSize:100}")
    private Long maximumSize;

    @Value("${multitenancy.datasource-cache.expireAfterAccess:10}")
    private Integer expireAfterAccess;

    @Value("${encryption.secret}")
    private String secret;

    @Value("${encryption.salt}")
    private String salt;

    private LoadingCache<String ,DataSource> tenantDataSources;

    @Override
    protected DataSource selectAnyDataSource() {
        return  masterDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantId) {
        try {
            return tenantDataSources.get(tenantId);
        } catch (ExecutionException e) {
            log.error(" Exception occured while retrieving  the datasource for tenant id : {} " , tenantId);
            throw new RuntimeException("Failed to load the datasource for tenant id : " + tenantId);
        }
    }

    /****
     * Once the Bean Created , Now starting the cache
     */
    @PostConstruct
    private void createCache() {
        tenantDataSources = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess , TimeUnit.MINUTES)
                .removalListener((RemovalListener<String , DataSource>) removal -> {
                    HikariDataSource hikariDataSource = (HikariDataSource) removal.getValue();
                    hikariDataSource.close();
                    log.info(" Closed Datasource :{} " , hikariDataSource.getPoolName());
                })
                .build(new CacheLoader<String, DataSource>() {
                    @Override
                    public DataSource load(String tenantId) throws Exception {
                       Tenant tenant =   masterTenantRepository.findByTenantId(tenantId)
                                .orElseThrow(() -> new RuntimeException("No such Tenant " + tenantId));
                       return createAndConfigureDataSource(tenant);
                    }
                });
    }

    private DataSource createAndConfigureDataSource(Tenant tenant) {
        String decyptedPassword  = encryptionService.decrypt(tenant.getPassword() ,  secret, salt);
        HikariDataSource dataSource =  dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        dataSource.setUsername(tenant.getDb());
        dataSource.setPassword(decyptedPassword);
        dataSource.setJdbcUrl(tenant.getUrl());

        dataSource.setPoolName(tenant.getTenantId() + TENANT_POOL_NAME_SUFFIX);
        log.info(" Configured Datasource : {} " , dataSource.getPoolName());
        return dataSource;
    }

}

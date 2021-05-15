package io.vaibhavshukla.multitenantmnagementservice.service;

import io.vaibhavshukla.multitenantmnagementservice.entity.Tenant;
import io.vaibhavshukla.multitenantmnagementservice.exception.TenantCreationException;
import io.vaibhavshukla.multitenantmnagementservice.repository.TenantRepository;
import io.vaibhavshukla.multitenantmnagementservice.vo.TenantCreationVO;
import io.vaibhavshukla.multitenantmnagementservice.vo.TenantsVo;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@EnableConfigurationProperties({LiquibaseProperties.class})
public class TenantManagementServiceImpl implements TenantManagementService {

    private final EncryptionService encryptionService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final LiquibaseProperties liquibaseProperties;
    private final ResourceLoader resourceLoader;
    private final TenantRepository tenantRepository;

    private final String urlPrefix;
    private final String secret;
    private final String salt;

    public TenantManagementServiceImpl(EncryptionService encryptionService,
                                       DataSource dataSource,
                                       JdbcTemplate jdbcTemplate,
                                       @Qualifier("tenantLiquibaseProperties")
                                       LiquibaseProperties liquibaseProperties,
                                       ResourceLoader resourceLoader,
                                       TenantRepository tenantRepository,
                                       @Value("${multitenancy.tenant.datasource.url-prefix}")
                                       String urlPrefix,
                                       @Value("${encryption.secret}")
                                       String secret,
                                       @Value("${encryption.salt}")
                                       String salt) {

        this.encryptionService = encryptionService;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.liquibaseProperties = liquibaseProperties;
        this.resourceLoader = resourceLoader;
        this.tenantRepository = tenantRepository;
        this.urlPrefix = urlPrefix;
        this.secret = secret;
        this.salt = salt;
    }

    private static final String VALID_DATABASE_NAME_REGREXP = "[A-Za-z0-9_]*";

    @Override
    public void createTeanant(TenantCreationVO tenantCreationVO) {

        if (!tenantCreationVO.getDb().matches(VALID_DATABASE_NAME_REGREXP)) {
            throw new TenantCreationException("Invalid Database Name " + tenantCreationVO.getDb());
        }

        String url = urlPrefix + tenantCreationVO.getDb();
        String encyptedPassword = encryptionService.encrypt(tenantCreationVO.getPassword() , secret , salt);

        try {
            createDatabase(tenantCreationVO.getDb() , tenantCreationVO.getPassword());
        } catch (DataAccessException e) {
            throw new TenantCreationException("Error while creating database " + tenantCreationVO.getDb() ,e );
        }

        try(Connection connection = DriverManager.getConnection(url , tenantCreationVO.getDb() , tenantCreationVO.getPassword())) {
            DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
            runLiquibase(tenantDataSource);
        }catch (SQLException | LiquibaseException e ) {

        }

        var tenant = Tenant.builder()
                .tenantId(UUID.randomUUID().toString())
                .db(tenantCreationVO.getDb())
                .password(encyptedPassword)
                .url(url)
                .build();
        tenantRepository.save(tenant);


    }

    @Override
    public List<TenantsVo> getAllTenants() {
        var tenant =  tenantRepository.findAll();
        List<TenantsVo> tenants = new ArrayList<>();

        tenant.forEach(t-> {
            tenants.add(TenantsVo.builder().tenantId(t.getTenantId()).identifier(t.getDb()).build());
        });
        return tenants;
    }

    private void createDatabase(String database , String password ) {
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE DATABASE " + database));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE USER " + database + " WITH ENCRYPTED PASSWORD '" + password + "'"));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + database + " TO " + database));
    }

    private void runLiquibase(DataSource dataSource) throws LiquibaseException {
        SpringLiquibase liquibase = getSpringLiquibase(dataSource);
        liquibase.afterPropertiesSet();
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
        return liquibase;
    }
}

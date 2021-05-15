package io.vaibhavshukla.multitenantservice.multitenant.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.SpringBeanContainer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = {"io.vaibhavshukla.multitenantservice.multitenant.repository"},
        entityManagerFactoryRef = "masterEntityManagerFactory" ,
        transactionManagerRef = "masterTransactionManager"
)
@EnableConfigurationProperties({DataSourceProperties.class , JpaProperties.class   })
public class MasterPersistenceConfig {

    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    private final JpaProperties jpaProperties;

    private final String  entityPackages;


    public MasterPersistenceConfig(ConfigurableListableBeanFactory configurableListableBeanFactory,
                                   JpaProperties jpaProperties,
                                   @Value("${multitenancy.master.entityManager.packages}") String entityPackages) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
        this.jpaProperties = jpaProperties;
        this.entityPackages = entityPackages;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(
            @Qualifier("masterDataSource")DataSource dataSource
            ) {
         var entityManager =    new LocalContainerEntityManagerFactoryBean();
         entityManager.setPersistenceUnitName("master-persistent-unit");
         entityManager.setPackagesToScan(entityPackages);
         entityManager.setDataSource(dataSource);

         JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

         entityManager.setJpaVendorAdapter(vendorAdapter);

        Map<String ,Object> properties = new HashMap<>(this.jpaProperties.getProperties());
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY , SpringPhysicalNamingStrategy.class);
        properties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY , SpringImplicitNamingStrategy.class);
        properties.put(AvailableSettings.BEAN_CONTAINER , new SpringBeanContainer(this.configurableListableBeanFactory));

        entityManager.setJpaPropertyMap(properties);

        return entityManager;
    }

    @Bean
    public JpaTransactionManager masterTransactionManager(
            @Qualifier("masterEntityManagerFactory") EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}

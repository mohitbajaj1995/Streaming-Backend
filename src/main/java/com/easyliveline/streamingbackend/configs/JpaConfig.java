package com.easyliveline.streamingbackend.configs;

import com.easyliveline.streamingbackend.util.MultiTenantConnectionProviderImpl;
import com.easyliveline.streamingbackend.util.TenantIdentifierResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration // Enable JPA auditing and reference AuditorAware bean
public class JpaConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MultiTenantConnectionProviderImpl multiTenantConnectionProvider;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.easyliveline.streamingbackend.models");
//        emf.setPersistenceUnitName("default");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.multiTenancy", "SCHEMA");
        properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        properties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.physical_naming_strategy", "com.easyliveline.streamingbackend.configs.SnakeCasePhysicalNamingStrategy");
        properties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.use_sql_comments", "true");
        properties.put("hibernate.type", "trace"); // Logs parameter binding

        emf.setJpaPropertyMap(properties);
        return emf;
    }
}
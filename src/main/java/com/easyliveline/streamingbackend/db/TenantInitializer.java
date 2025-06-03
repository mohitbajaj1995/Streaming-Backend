//package com.easyliveline.streamingbackend.db;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class TenantInitializer implements ApplicationRunner {
//
//    @Autowired
//    private TenantSchemaService schemaService;
//
//    @Autowired
//    private HibernateBootstrapService hibernateBootstrap;
//
//    @Override
//    public void run(ApplicationArguments args) {
//        List<String> tenantSchemas = List.of("tenant_1", "tenant_2", "tenant_3");
//
//        // Step 1: Create schemas
//        for (String tenant : tenantSchemas) {
//            schemaService.createSchemaIfNotExists(tenant);
//        }
//
//        // Step 2: Trigger Hibernate to create tables in those schemas
//        hibernateBootstrap.bootstrapTenants(tenantSchemas);
//    }
//}

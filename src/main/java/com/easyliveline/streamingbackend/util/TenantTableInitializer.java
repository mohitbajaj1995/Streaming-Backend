//package com.easyliveline.streamingbackend.util;
//
//import com.easyliveline.streamingbackend.models.ApplicationConfiguration;
//import com.easyliveline.streamingbackend.services.TenantTableService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TenantTableInitializer implements ApplicationRunner {
//
//    private final TenantTableService tenantTableService;
//    private final ApplicationConfiguration applicationConfiguration;
//
//    @Autowired
//    public TenantTableInitializer(TenantTableService tenantTableService, ApplicationConfiguration applicationConfiguration) {
//        this.tenantTableService = tenantTableService;
//        this.applicationConfiguration = applicationConfiguration;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) {
//        boolean needToUpdateTables = applicationConfiguration.getAppSettings().isNeedToUpdateTables();
//        System.out.println("Table Update Settings: " + needToUpdateTables);
//        if (needToUpdateTables) {
//            applicationConfiguration.getWebsite().keySet().stream()
//                    .filter(key -> !"royal".equals(key))
//                    .forEach(tenantTableService::createTenantTables);
//        }
//    }
//}
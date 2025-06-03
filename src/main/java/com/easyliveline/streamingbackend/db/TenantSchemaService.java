//package com.easyliveline.streamingbackend.db;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TenantSchemaService {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    public void createSchemaIfNotExists(String schemaName) {
//        String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
//        jdbcTemplate.execute(sql);
//    }
//}
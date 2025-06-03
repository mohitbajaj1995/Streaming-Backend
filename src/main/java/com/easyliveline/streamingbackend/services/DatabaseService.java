package com.easyliveline.streamingbackend.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Executes raw SQL using EntityManager.
     */
    @Transactional
    public void executeSql(String sql) {
        entityManager.createNativeQuery(sql).executeUpdate();
    }

    public boolean indexExists(String indexName) {
        System.out.println("Checking if index exists: " + indexName);
        String sql = "SELECT 1 FROM pg_indexes WHERE indexname = ?";
        List<?> result = entityManager.createNativeQuery(sql)
                .setParameter(1, indexName)
                .getResultList();
        return !result.isEmpty();
    }

    public boolean constraintExists(String tableName, String constraintName) {
        System.out.println("Checking if constraint exists: " + constraintName + " on table: " + tableName);
        String sql = "SELECT 1 FROM information_schema.table_constraints WHERE table_name = ? AND constraint_name = ?";
        List<?> result = entityManager.createNativeQuery(sql)
                .setParameter(1, tableName)
                .setParameter(2, constraintName)
                .getResultList();
        return !result.isEmpty();
    }

}

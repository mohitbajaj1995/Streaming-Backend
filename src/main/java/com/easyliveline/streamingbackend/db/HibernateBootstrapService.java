//package com.easyliveline.streamingbackend.db;
//
//import com.easyliveline.streamingbackend.Util.TenantContext;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class HibernateBootstrapService {
//
//    @Autowired
//    private EntityManagerFactory entityManagerFactory;
//
//    public void bootstrapTenants(List<String> tenantSchemas) {
//        for (String tenantId : tenantSchemas) {
//            try {
//                // Set the current tenant ID
//                TenantContext.setCurrentTenant(tenantId);
//
//                // Trigger Hibernate to initialize tables
//                EntityManager em = entityManagerFactory.createEntityManager();
//                em.getTransaction().begin();
//                em.createNativeQuery("SELECT 1").getSingleResult(); // Dummy query
//                em.getTransaction().commit();
//                em.close();
//
//                System.out.println("✅ Bootstrapped tenant schema: " + tenantId);
//            } catch (Exception e) {
//                System.err.println("❌ Failed to bootstrap tenant schema: " + tenantId);
//            } finally {
//                TenantContext.clear(); // Clean up the thread-local
//            }
//        }
//    }
//}
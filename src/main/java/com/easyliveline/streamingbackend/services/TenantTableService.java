package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.SnakeCaseNamingStrategy;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.*;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TenantTableService {

    private final DatabaseService databaseService;


    public TenantTableService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    public void createTenantTables(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", dataSourceUrl)
                .applySetting("hibernate.connection.username", dbUsername)
                .applySetting("hibernate.connection.password", dbPassword)
                .applySetting("hibernate.physical_naming_strategy", SnakeCaseNamingStrategy.class.getName())
                .build();

        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Transaction.class)
                    .addAnnotatedClass(Refund.class)
                    .addAnnotatedClass(Meeting.class)
                    .addAnnotatedClass(Zoom.class)
                    .addAnnotatedClass(SuperMaster.class)
                    .addAnnotatedClass(Master.class)
                    .addAnnotatedClass(Subscriber.class)
                    .addAnnotatedClass(Plan.class)
                    .addAnnotatedClass(Owner.class)
                    .addAnnotatedClass(Manager.class)
                    .addAnnotatedClass(Admin.class)
                    .buildMetadata();

            for (PersistentClass persistentClass : metadata.getEntityBindings()) {
                Table table = persistentClass.getTable();
                if (table != null) {
                    String newTableName = table.getName().replace("royal", tenantId);
                    table.setName(newTableName);
                }
            }

            Map<String, Object> configurationValues = new HashMap<>();
            configurationValues.put("hibernate.hbm2ddl.auto", "update");
            configurationValues.put("hibernate.show_sql", "true");

            SchemaManagementToolCoordinator.process(
                    metadata,
                    registry,
                    configurationValues,
                    null
            );

            System.out.println("Created tables for tenant: " + tenantId);
            applyConstraints(metadata, tenantId);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public void applyConstraints(Metadata metadata, String tenantId) {
        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
            System.out.println("For Table: "+ persistentClass.getTable().getName() + " with tenant: " + tenantId);

            Table table = persistentClass.getTable();
            if (table == null) continue;

            String tableName = table.getName();

            for (UniqueKey uniqueKey : table.getUniqueKeys().values()) {
                if (uniqueKey.getColumns().isEmpty()) continue;

                String constraintName = "uk_" + tenantId + "_" + uniqueKey.getName();
                String columns = uniqueKey.getColumns().stream()
                        .map(Column::getName)
                        .collect(Collectors.joining(", "));

                if (!databaseService.constraintExists(tableName, constraintName)) {
                    String sql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName +
                            " UNIQUE (" + columns + ");";
                    databaseService.executeSql(sql);
                }
            }

            for (ForeignKey foreignKey : table.getForeignKeys().values()) {
                if (foreignKey.getColumns().isEmpty() || foreignKey.getReferencedColumns().isEmpty()) continue;

                String fkName = "fk_" + tenantId + "_" + foreignKey.getName();
                String columnNames = foreignKey.getColumns().stream()
                        .map(Column::getName)
                        .collect(Collectors.joining(", "));

                String referencedTable = foreignKey.getReferencedTable().getName().replace("royal", tenantId);
                String referencedColumns = foreignKey.getReferencedColumns().stream()
                        .map(Column::getName)
                        .collect(Collectors.joining(", "));

                if (!databaseService.constraintExists(tableName, fkName)) {
                    String fkSql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + fkName +
                            " FOREIGN KEY (" + columnNames + ") REFERENCES " + referencedTable +
                            " (" + referencedColumns + ");";
                    databaseService.executeSql(fkSql);
                }
            }

            for (org.hibernate.mapping.Index index : table.getIndexes().values()) {
                if (index.getColumns().isEmpty()) continue;

                String originalIndexName = index.getName();
                String replacedIndexName = originalIndexName.replace("royal", tenantId);

                String columnNames = index.getColumns().stream()
                        .map(Column::getName)
                        .collect(Collectors.joining(", "));
                System.out.println("Creating index: " + replacedIndexName+" on table: " + tableName + " with columns: " + columnNames);


                if (!databaseService.indexExists(replacedIndexName)) {
                    String indexSql = "CREATE INDEX " + replacedIndexName + " ON " + tableName +
                            " (" + columnNames + ");";
                    databaseService.executeSql(indexSql);
                }
            }
        }
    }
}

//@Service
//public class TenantTableService {
//
//    private final DatabaseService databaseService;
//
//
//    public TenantTableService(DatabaseService databaseService) {
//        this.databaseService = databaseService;
//    }
//
//    @Value("${spring.datasource.url}")
//    private String dataSourceUrl;
//
//public void createTenantTables(String tenantId) {
//    if (tenantId == null || tenantId.trim().isEmpty()) {
//        throw new IllegalArgumentException("Tenant ID cannot be null or empty");
//    }
//    // Configure Hibernate StandardServiceRegistry
//    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//            .applySetting("hibernate.connection.url", dataSourceUrl)
//            .applySetting("hibernate.connection.username", "postgres")
//            .applySetting("hibernate.connection.password", "22141714")
//            .applySetting("hibernate.physical_naming_strategy", SnakeCaseNamingStrategy.class.getName())
//            .build();
//
//    try {
//        // Build Metadata
//        Metadata metadata = new MetadataSources(registry)
//                .addAnnotatedClass(User.class)
//                .addAnnotatedClass(Transaction.class)
//                .addAnnotatedClass(Refund.class)
//                .addAnnotatedClass(Meeting.class)
//                .addAnnotatedClass(Zoom.class)
//                .addAnnotatedClass(SuperMaster.class)
//                .addAnnotatedClass(Master.class)
//                .addAnnotatedClass(Subscriber.class)
//                .addAnnotatedClass(Plan.class)
//                .addAnnotatedClass(Owner.class)
//                .addAnnotatedClass(Manager.class)
//                .addAnnotatedClass(Admin.class)
//                .buildMetadata();
//
//        // Rename tables, constraints, indexes in metadata BEFORE schema update
//        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//            Table table = persistentClass.getTable();
//            if (table == null) continue;
//
//            // Rename table
//            String newTableName = table.getName().replace("royal", tenantId);
//            table.setName(newTableName);
//
//            // Rename unique keys
//            for (UniqueKey uniqueKey : table.getUniqueKeys().values()) {
//                String ukName = uniqueKey.getName();
//                if (ukName != null && ukName.contains("royal")) {
//                    uniqueKey.setName(ukName.replace("royal", tenantId));
//                }
//            }
//
//            // Rename foreign keys and referenced tables
//            for (ForeignKey fk : table.getForeignKeys().values()) {
//                String fkName = fk.getName();
//                if (fkName != null && fkName.contains("royal")) {
//                    fk.setName(fkName.replace("royal", tenantId));
//                }
//                Table refTable = fk.getReferencedTable();
//                if (refTable != null) {
//                    String refTableName = refTable.getName();
//                    if (refTableName.contains("royal")) {
//                        refTable.setName(refTableName.replace("royal", tenantId));
//                    }
//                }
//            }
//
//            // Rename indexes
//            for (org.hibernate.mapping.Index index : table.getIndexes().values()) {
//                String idxName = index.getName();
//                if (idxName != null && idxName.contains("royal")) {
//                    index.setName(idxName.replace("royal", tenantId));
//                }
//            }
//        }
//
//        // Configuration values
//        Map<String, Object> configurationValues = new HashMap<>();
//        configurationValues.put("hibernate.hbm2ddl.auto", "update");  // DDL auto update
//        configurationValues.put("hibernate.show_sql", "true");
//
//        // Run schema update with renamed metadata
//        SchemaManagementToolCoordinator.process(
//                metadata,
//                registry,
//                configurationValues,
//                null
//        );
//
//        System.out.println("Created tables for tenant: " + tenantId);
//
//        // Apply constraints manually if needed (optional, since Hibernate handles most)
//        System.out.println("Applying constraints for tenant: " + tenantId);
//        applyConstraints(metadata, tenantId);
//
//    } finally {
//        StandardServiceRegistryBuilder.destroy(registry);
//    }
//}
//
//public void applyConstraints(Metadata metadata, String tenantId) {
//    for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//        Table table = persistentClass.getTable();
//        if (table == null) continue;
//
//        String tableName = table.getName();
//
//        // Unique Constraints
//        for (UniqueKey uniqueKey : table.getUniqueKeys().values()) {
//            if (uniqueKey.getColumns().isEmpty()) continue;
//
//            String constraintName = uniqueKey.getName();
//            if (constraintName == null) continue;
//
//            String columns = uniqueKey.getColumns().stream()
//                    .map(Column::getName)
//                    .collect(Collectors.joining(", "));
//
//            if (!databaseService.constraintExists(tableName, constraintName)) {
//                String sql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " UNIQUE (" + columns + ");";
//                databaseService.executeSql(sql);
//            }
//        }
//
//        for (ForeignKey foreignKey : table.getForeignKeys().values()) {
//            // Skip if no columns or no referenced columns
//            if (foreignKey.getColumns().isEmpty() || foreignKey.getReferencedColumns().isEmpty()) continue;
//
//            // Construct the foreign key name with tenantId prefix
//            String fkName = "fk_" + tenantId + "_" + foreignKey.getName();
//
//            // Join source columns without quotes
//            String columnNames = foreignKey.getColumns().stream()
//                    .map(Column::getName)
//                    .collect(Collectors.joining(", "));
//
//            // Replace 'royal' schema in referenced table with tenantId
//            String referencedTable = foreignKey.getReferencedTable().getName().replace("royal", tenantId);
//
//            // Join referenced columns without quotes
//            String referencedColumns = foreignKey.getReferencedColumns().stream()
//                    .map(Column::getName)
//                    .collect(Collectors.joining(", "));
//
//            // Check if constraint already exists before adding
//            if (!databaseService.constraintExists(tableName, fkName)) {
//                String fkSql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + fkName +
//                        " FOREIGN KEY (" + columnNames + ") REFERENCES " + referencedTable +
//                        " (" + referencedColumns + ");";
//                databaseService.executeSql(fkSql);
//            }
//        }
//
//
//        // Indexes
//        for (org.hibernate.mapping.Index index : table.getIndexes().values()) {
//            if (index.getColumns().isEmpty()) continue;
//
//            String indexName = index.getName();
//            if (indexName == null) continue;
//
//            String columnNames = index.getColumns().stream()
//                    .map(Column::getName)
//                    .collect(Collectors.joining(", "));
//
//            if (!databaseService.indexExists(indexName)) {
//                String indexSql = "CREATE INDEX " + indexName + " ON " + tableName + " (" + columnNames + ");";
//                databaseService.executeSql(indexSql);
//            }
//        }
//    }
//}
//}

//
//package com.easyliveline.streamingbackend.Services;
//
//import com.easyliveline.streamingbackend.Models.*;
//import com.easyliveline.streamingbackend.Util.SnakeCaseNamingStrategy;
//import org.hibernate.boot.Metadata;
//import org.hibernate.boot.MetadataSources;
//import org.hibernate.boot.registry.StandardServiceRegistry;
//import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
//import org.hibernate.mapping.Table;
//import org.hibernate.mapping.Index;
//import org.hibernate.mapping.UniqueKey;
//import org.hibernate.mapping.ForeignKey;
//import org.hibernate.mapping.PersistentClass;
//import org.hibernate.mapping.Column; // Keep if used by your entities/constraints
//import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.hibernate.boot.model.naming.Identifier;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class TenantTableService {
//
//    private final DatabaseService databaseService;
//
//    public TenantTableService(DatabaseService databaseService) {
//        this.databaseService = databaseService;
//    }
//
//    @Value("${spring.datasource.url}")
//    private String dataSourceUrl;
//
//    // Consider fetching these from configuration as well
//    @Value("${spring.datasource.username}")
//    private String dbUsername;
//
//    @Value("${spring.datasource.password}")
//    private String dbPassword;
//
////    public void createTenantTables(String tenantId) {
////        if (tenantId == null || tenantId.trim().isEmpty()) {
////            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
////        }
////
////        // Placeholder for the default schema part to be replaced (e.g., "royal")
////        // Make this configurable if it varies
////        String schemaPlaceholder = "royal";
////
////        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
////                .applySetting("hibernate.connection.url", dataSourceUrl)
////                .applySetting("hibernate.connection.username", dbUsername) // Use configured username
////                .applySetting("hibernate.connection.password", dbPassword) // Use configured password
////                .applySetting("hibernate.physical_naming_strategy", SnakeCaseNamingStrategy.class.getName())
////                // Important: defer DDL for explicit control if applyConstraints is still used,
////                // or ensure all names are unique before this step.
////                // .applySetting("hibernate.hbm2ddl.auto", "none") // or "validate" if you manage DDL fully elsewhere
////                .build();
////
////        try {
////            MetadataSources metadataSources = new MetadataSources(registry)
////                    .addAnnotatedClass(User.class)
////                    .addAnnotatedClass(Transaction.class)
////                    .addAnnotatedClass(Refund.class)
////                    .addAnnotatedClass(Meeting.class)
////                    .addAnnotatedClass(Zoom.class)
////                    .addAnnotatedClass(SuperMaster.class)
////                    .addAnnotatedClass(Master.class)
////                    .addAnnotatedClass(Subscriber.class)
////                    .addAnnotatedClass(Plan.class)
////                    .addAnnotatedClass(Owner.class)
////                    .addAnnotatedClass(Manager.class)
////                    .addAnnotatedClass(Admin.class);
////            // Add other annotated classes as needed
////
////            Metadata metadata = metadataSources.buildMetadata();
////
////            // Modify metadata for tenant-specific names BEFORE schema generation
////            for (PersistentClass persistentClass : metadata.getEntityBindings()) {
////                Table table = persistentClass.getTable();
////                String originalTableName = table.getName();
////
////                if (originalTableName.contains(schemaPlaceholder)) {
////                    String newTableName = originalTableName.replace(schemaPlaceholder, tenantId);
////                    table.setName(newTableName);
////                    System.out.println("Prepared rename for table: " + originalTableName + " -> " + newTableName);
////                }
////
////                // Rename Unique Key Constraints
////                Map<String, UniqueKey> newUniqueKeys = new HashMap<>();
////                for (UniqueKey uk : new ArrayList<>(table.getUniqueKeys().values())) { // Iterate copy
////                    table.removeUniqueKey(uk); // Remove by object reference
////                    String originalUkName = uk.getName();
////                    if (originalUkName.contains(schemaPlaceholder)) {
////                        String newUkName = originalUkName.replace(schemaPlaceholder, tenantId);
////                        uk.setName(newUkName);
////                        System.out.println("Table " + table.getName() + ": Prepared rename for UK: " + originalUkName + " -> " + newUkName);
////                    }
////                    newUniqueKeys.put(uk.getName(), uk); // Add to new map with potentially new name
////                }
////                // Clear and re-add to ensure map keys are updated
////                table.getUniqueKeys().clear();
////                for(UniqueKey uk : newUniqueKeys.values()){
////                    table.addUniqueKey(uk);
////                }
////
////
////                // Rename Indexes
////                Map<String, Index> newIndexes = new HashMap<>();
////                for (Index index : new ArrayList<>(table.getIndexes().values())) { // Iterate copy
////                    table.removeIndex(index); // Remove by object reference
////                    String originalIndexName = index.getName();
////                    if (originalIndexName.contains(schemaPlaceholder)) {
////                        String newIndexName = originalIndexName.replace(schemaPlaceholder, tenantId);
////                        index.setName(newIndexName);
////                        System.out.println("Table " + table.getName() + ": Prepared rename for Index: " + originalIndexName + " -> " + newIndexName);
////                    }
////                    newIndexes.put(index.getName(), index);
////                }
////                table.getIndexes().clear();
////                for(Index index : newIndexes.values()){
////                    table.addIndex(index);
////                }
////
////
////                // Rename Foreign Key Constraints
////                // Note: Referenced table names are handled because all tables are iterated and renamed.
////                Map<String, ForeignKey> newForeignKeys = new HashMap<>();
////                for (ForeignKey fk : new ArrayList<>(table.getForeignKeys().values())) { // Iterate copy
////                    table.removeForeignKey(fk); // Remove by object reference
////                    String originalFkName = fk.getName(); // This is the constraint name
////                    if (originalFkName.contains(schemaPlaceholder)) {
////                        String newFkName = originalFkName.replace(schemaPlaceholder, tenantId);
////                        fk.setName(newFkName);
////                        System.out.println("Table " + table.getName() + ": Prepared rename for FK: " + originalFkName + " -> " + newFkName);
////                    }
////                    newForeignKeys.put(fk.getName(), fk);
////                }
////                table.getForeignKeys().clear();
////                for(ForeignKey fk : newForeignKeys.values()){
////                    table.addForeignKey(fk);
////                }
////            }
////
////            Map<String, Object> configurationValues = new HashMap<>();
////            configurationValues.put("hibernate.hbm2ddl.auto", "update"); // Now this should use tenant-specific names
////            configurationValues.put("hibernate.show_sql", "true");
////
////            SchemaManagementToolCoordinator.process(
////                    metadata,
////                    registry,
////                    configurationValues,
////                    null
////            );
////
////            System.out.println("Schema update process completed for tenant: " + tenantId);
////
////            // If SchemaManagementToolCoordinator with "update" handles everything,
////            // applyConstraints might become optional or a verification step.
////            // If you keep applyConstraints, ensure its naming logic matches the one used above.
////            // For instance, it should expect names like "idx_tenantId_..." not "idx_tenantId_idx_tenantId_..."
////            // applyConstraints(metadata, tenantId, schemaPlaceholder); // Pass schemaPlaceholder if needed
////
////        } finally {
////            StandardServiceRegistryBuilder.destroy(registry);
////        }
////    }
//
//    public void createTenantTables(String tenantId) {
//        if (tenantId == null || tenantId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
//        }
//
//        String schemaPlaceholder = "royal"; // Or your actual placeholder
//
//        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//                .applySetting("hibernate.connection.url", dataSourceUrl)
//                .applySetting("hibernate.connection.username", dbUsername)
//                .applySetting("hibernate.connection.password", dbPassword)
//                .applySetting("hibernate.physical_naming_strategy", SnakeCaseNamingStrategy.class.getName())
//                .build();
//
//        try {
//            MetadataSources metadataSources = new MetadataSources(registry)
//                    .addAnnotatedClass(User.class)
//                    .addAnnotatedClass(Transaction.class)
//                    .addAnnotatedClass(Refund.class)
//                    .addAnnotatedClass(Meeting.class)
//                    .addAnnotatedClass(Zoom.class)
//                    .addAnnotatedClass(SuperMaster.class)
//                    .addAnnotatedClass(Master.class)
//                    .addAnnotatedClass(Subscriber.class)
//                    .addAnnotatedClass(Plan.class)
//                    .addAnnotatedClass(Owner.class)
//                    .addAnnotatedClass(Manager.class)
//                    .addAnnotatedClass(Admin.class);
//
//            Metadata metadata = metadataSources.buildMetadata();
//
//            for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//                Table table = persistentClass.getTable();
//                String originalTableName = table.getName();
//
//                if (originalTableName.contains(schemaPlaceholder)) {
//                    String newTableName = originalTableName.replace(schemaPlaceholder, tenantId);
//                    table.setName(newTableName); // This directly sets the table's name string
//                    System.out.println("Prepared rename for table: " + originalTableName + " -> " + newTableName);
//                }
//
//                // --- Unique Key Constraints ---
//                Map<Identifier, UniqueKey> uniqueKeysMap = table.getUniqueKeys();
//                List<UniqueKey> uksToProcess = new ArrayList<>(uniqueKeysMap.values());
//                uniqueKeysMap.clear(); // Clear the live map
//
//                for (UniqueKey uk : uksToProcess) {
//                    String originalUkName = uk.getName();
//                    String newUkName = originalUkName; // Default to original
//                    if (originalUkName.contains(schemaPlaceholder)) {
//                        newUkName = originalUkName.replace(schemaPlaceholder, tenantId);
//                        uk.setName(newUkName); // Modify the UniqueKey object's name
//                        System.out.println("Table " + table.getName() + ": Prepared rename for UK: " + originalUkName + " -> " + newUkName);
//                    }
//                    // Add back to the map using the (potentially new) name as the basis for the Identifier key
//                    uniqueKeysMap.put(Identifier.toIdentifier(newUkName, uk.isQuoted()), uk);
//                }
//
//                // --- Indexes ---
//                Map<Identifier, Index> indexesMap = table.getIndexes();
//                List<Index> indexesToProcess = new ArrayList<>(indexesMap.values());
//                indexesMap.clear(); // Clear the live map
//
//                for (Index index : indexesToProcess) {
//                    String originalIndexName = index.getName();
//                    String newIndexName = originalIndexName; // Default to original
//                    if (originalIndexName.contains(schemaPlaceholder)) {
//                        newIndexName = originalIndexName.replace(schemaPlaceholder, tenantId);
//                        index.setName(newIndexName); // Modify the Index object's name
//                        System.out.println("Table " + table.getName() + ": Prepared rename for Index: " + originalIndexName + " -> " + newIndexName);
//                    }
//                    indexesMap.put(Identifier.toIdentifier(newIndexName, index.isQuoted()), index);
//                }
//
//                // --- Foreign Key Constraints ---
//                Map<Identifier, ForeignKey> foreignKeysMap = table.getForeignKeys();
//                List<ForeignKey> fksToProcess = new ArrayList<>(foreignKeysMap.values());
//                foreignKeysMap.clear(); // Clear the live map
//
//                for (ForeignKey fk : fksToProcess) {
//                    String originalFkName = fk.getName();
//                    String newFkName = originalFkName; // Default to original
//                    if (originalFkName.contains(schemaPlaceholder)) {
//                        newFkName = originalFkName.replace(schemaPlaceholder, tenantId);
//                        fk.setName(newFkName); // Modify the ForeignKey object's name (this is the constraint name)
//                        System.out.println("Table " + table.getName() + ": Prepared rename for FK: " + originalFkName + " -> " + newFkName);
//                    }
//                    // The referenced table name (fk.getReferencedTable().getName())
//                    // should already be tenant-specific due to the table.setName() loop on all persistentClasses.
//                    foreignKeysMap.put(Identifier.toIdentifier(newFkName, fk.isQuoted()), fk);
//                }
//            }
//
//            Map<String, Object> configurationValues = new HashMap<>();
//            configurationValues.put("hibernate.hbm2ddl.auto", "update");
//            configurationValues.put("hibernate.show_sql", "true");
//
//            SchemaManagementToolCoordinator.process(
//                    metadata,
//                    registry,
//                    configurationValues,
//                    null
//            );
//
//            System.out.println("Schema update process completed for tenant: " + tenantId);
//
//            // Optional: Call applyConstraints if needed for verification or specific DDL handling
//            // applyConstraints(metadata, tenantId, schemaPlaceholder);
//
//        } finally {
//            StandardServiceRegistryBuilder.destroy(registry);
//        }
//    }
//
//    /**
//     * Applies constraints manually. This might be redundant if hibernate.hbm2ddl.auto="update"
//     * correctly creates all constraints with tenant-specific names after metadata modification.
//     * If used, ensure its naming convention aligns with the metadata modification.
//     */
//    public void applyConstraints(Metadata metadata, String tenantId, String schemaPlaceholder) {
//        // Iterate over all entity bindings, not just User.class, if constraints apply to all tables.
//        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//            Table table = persistentClass.getTable(); // This table object has the tenant-specific name.
//            String tableName = table.getName(); // e.g., "tenantId_users"
//
//            // Handle Unique Constraints
//            for (UniqueKey uniqueKey : table.getUniqueKeys().values()) {
//                if (uniqueKey.getColumns().isEmpty()) continue;
//
//                // Name should already be tenant-specific from metadata modification
//                String constraintName = uniqueKey.getName();
//                // Example: if original was uk_royal_user_email, it's now uk_tenantId_user_email
//
//                String columns = uniqueKey.getColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//
//                if (!databaseService.constraintExists(tableName, constraintName)) {
//                    String sql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " UNIQUE (" + columns + ");";
//                    databaseService.executeSql(sql);
//                    System.out.println("Applied UK: " + constraintName + " on " + tableName);
//                }
//            }
//
//            // Handle Foreign Key Constraints
//            for (ForeignKey foreignKey : table.getForeignKeys().values()) {
//                if (foreignKey.getColumns().isEmpty()) continue;
//
//                String fkName = foreignKey.getName(); // Should be tenant-specific
//                String columnNames = foreignKey.getColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//
//                // Referenced table name should also be tenant-specific from metadata
//                String referencedTableName = foreignKey.getReferencedTable().getName();
//                String referencedColumns = foreignKey.getReferencedColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//
//                if (!databaseService.constraintExists(tableName, fkName)) {
//                    String fkSql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + fkName +
//                            " FOREIGN KEY (" + columnNames + ") REFERENCES " + referencedTableName +
//                            " (" + referencedColumns + ");";
//                    databaseService.executeSql(fkSql);
//                    System.out.println("Applied FK: " + fkName + " on " + tableName + " referencing " + referencedTableName);
//                }
//            }
//
//            // Handle Indexes
//            for (org.hibernate.mapping.Index index : table.getIndexes().values()) {
//                if (index.getColumns().isEmpty()) continue;
//
//                String indexName = index.getName(); // Should be tenant-specific
//
//                String columnNames = index.getColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//
//                if (!databaseService.indexExists(indexName)) { // Check with the tenant-specific name
//                    String indexSql = "CREATE INDEX " + indexName + " ON " + tableName + " (" + columnNames + ");";
//                    databaseService.executeSql(indexSql);
//                    System.out.println("Applied Index: " + indexName + " on " + tableName);
//                }
//            }
//        }
//        System.out.println("Finished applying constraints for tenant: " + tenantId);
//    }
//}
///  xxxxxxxxxxxxworking
//package com.easyliveline.streamingbackend.Services;
//
//import com.easyliveline.streamingbackend.Models.*;
//import com.easyliveline.streamingbackend.Util.SnakeCaseNamingStrategy;
//import org.hibernate.boot.Metadata;
//import org.hibernate.boot.MetadataSources;
//import org.hibernate.boot.registry.StandardServiceRegistry;
//import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
//import org.hibernate.mapping.Table;
//import org.hibernate.mapping.Index;
//import org.hibernate.mapping.UniqueKey;
//import org.hibernate.mapping.ForeignKey; // Ensure this import is correct
//import org.hibernate.mapping.PersistentClass;
//import org.hibernate.mapping.Column;
//// Ensure this import is present and correct if Identifier is used for other maps
//import org.hibernate.boot.model.naming.Identifier;
//import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//
//@Service
//public class TenantTableService {
//
//    private final DatabaseService databaseService;
//
//    @Value("${spring.datasource.url}")
//    private String dataSourceUrl;
//
//    @Value("${spring.datasource.username}")
//    private String dbUsername;
//
//    @Value("${spring.datasource.password}")
//    private String dbPassword;
//
//    public TenantTableService(DatabaseService databaseService) {
//        this.databaseService = databaseService;
//    }
//
//    public void createTenantTables(String tenantId) {
//        if (tenantId == null || tenantId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
//        }
//
//        String schemaPlaceholder = "royal"; // Your placeholder string
//
//        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//                .applySetting("hibernate.connection.url", dataSourceUrl)
//                .applySetting("hibernate.connection.username", dbUsername)
//                .applySetting("hibernate.connection.password", dbPassword)
//                .applySetting("hibernate.physical_naming_strategy", SnakeCaseNamingStrategy.class.getName())
//                .build();
//
//        try {
//            MetadataSources metadataSources = new MetadataSources(registry)
//                    .addAnnotatedClass(User.class)
//                    // Add all your other annotated classes here
//                    .addAnnotatedClass(Transaction.class)
//                    .addAnnotatedClass(Refund.class)
//                    .addAnnotatedClass(Meeting.class)
//                    .addAnnotatedClass(Zoom.class)
//                    .addAnnotatedClass(SuperMaster.class)
//                    .addAnnotatedClass(Master.class)
//                    .addAnnotatedClass(Subscriber.class)
//                    .addAnnotatedClass(Plan.class)
//                    .addAnnotatedClass(Owner.class)
//                    .addAnnotatedClass(Manager.class)
//                    .addAnnotatedClass(Admin.class);
//
//            Metadata metadata = metadataSources.buildMetadata();
//
//            for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//                Table table = persistentClass.getTable();
//                String originalTableName = table.getName();
//
//                if (originalTableName.contains(schemaPlaceholder)) {
//                    String newTableName = originalTableName.replace(schemaPlaceholder, tenantId);
//                    table.setName(newTableName);
//                    System.out.println("Prepared rename for table: " + originalTableName + " -> " + newTableName);
//                }
//
//                // --- Unique Key Constraints ---
//                // Assuming getUniqueKeys() returns Map<Identifier, UniqueKey> as per typical Hibernate 6
//                System.out.println("checking table" + table.getName() + " for unique keys");
//                System.out.println("Unique Keys before clearing: " + table.getUniqueKeys().values());
//                Map<String, UniqueKey> uniqueKeysMap = new HashMap<>(table.getUniqueKeys());
//                System.out.println("Unique Keys before clearing: " + uniqueKeysMap);
//                List<UniqueKey> uksToProcess = new ArrayList<>(uniqueKeysMap.values());
//                uniqueKeysMap.clear(); // ✅ Now this works without exception
//
//                for (UniqueKey uk : uksToProcess) {
//                    String originalUkName = uk.getName();
//                    String newUkName = originalUkName;
//                    if (originalUkName.contains(schemaPlaceholder)) {
//                        newUkName = originalUkName.replace(schemaPlaceholder, tenantId);
//                        uk.setName(newUkName);
//                        System.out.println("Table " + table.getName() + ": Prepared rename for UK: " + originalUkName + " -> " + newUkName);
//                    }
//                    uniqueKeysMap.put(String.valueOf(Identifier.toIdentifier(newUkName)), uk);
//                }
//
//                // ✅ Handle Unique Constraints
////        for (UniqueKey uniqueKey : table.getUniqueKeys().values()) {
////            if (uniqueKey.getColumns().isEmpty()) continue;
////
////            String constraintName = "uk_" + tenantId + "_" + uniqueKey.getName();
////            String columns = uniqueKey.getColumns().stream()
////                    .map(Column::getName)
////                    .collect(Collectors.joining(", "));
////
////            if (!databaseService.constraintExists(tableName, constraintName)) {
////                String sql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " UNIQUE (" + columns + ");";
////                databaseService.executeSql(sql);
////            }
////        }
//
//                // --- Indexes ---
//                // Assuming getIndexes() returns Map<Identifier, Index> as per typical Hibernate 6
//                Map<String, Index> indexesMap = new HashMap<>(table.getIndexes());
//                List<Index> indexesToProcess = new ArrayList<>(indexesMap.values());
//                indexesMap.clear(); // ✅ Now it's safe
//
//                for (Index index : indexesToProcess) {
//                    String originalIndexName = index.getName();
//                    String newIndexName = originalIndexName;
//                    if (originalIndexName.contains(schemaPlaceholder)) {
//                        newIndexName = originalIndexName.replace(schemaPlaceholder, tenantId);
//                        index.setName(newIndexName);
//                        System.out.println("Table " + table.getName() + ": Prepared rename for Index: " + originalIndexName + " -> " + newIndexName);
//                    }
//                    // Use simpler Identifier.toIdentifier without isQuoted()
//                    indexesMap.put(String.valueOf(Identifier.toIdentifier(newIndexName)), index);
//                }
//
//                // --- Foreign Key Constraints ---
//                // Adjusted based on the error: "Provided: Map<ForeignKeyKey, ForeignKey>"
//                // This means we do NOT clear and re-key with Identifier.
//                // We simply update the name on the ForeignKey objects themselves.
//                // The actual type of the key in the map (e.g., ForeignKeyKey) is preserved.
//                // It's crucial that table.getForeignKeys().values() gives mutable ForeignKey objects.
//                // Map<?, ForeignKey> foreignKeysMap = table.getForeignKeys(); // Get the map
//                for (ForeignKey fk : table.getForeignKeys().values()) { // Iterate over the values
//                    String originalFkName = fk.getName(); // This is the FK constraint name
//                    String newFkName = originalFkName;
//                    if (originalFkName.contains(schemaPlaceholder)) {
//                        newFkName = originalFkName.replace(schemaPlaceholder, tenantId);
//                        fk.setName(newFkName); // Modify the ForeignKey object's name string
//                        System.out.println("Table " + table.getName() + ": Updated FK name (constraint): " + originalFkName + " -> " + newFkName);
//                    }
//                    // No map clearing or re-putting with Identifier keys for foreign keys
//                    // if the map is not keyed by Identifier.
//                }
//            }
//
//            Map<String, Object> configurationValues = new HashMap<>();
//            configurationValues.put("hibernate.hbm2ddl.auto", "update");
//            configurationValues.put("hibernate.show_sql", "true");
//
//            SchemaManagementToolCoordinator.process(
//                    metadata,
//                    registry,
//                    configurationValues,
//                    null
//            );
//
//            System.out.println("Schema update process completed for tenant: " + tenantId);
//
//             applyConstraints(metadata, tenantId, schemaPlaceholder); // Optional
//
//        } finally {
//            StandardServiceRegistryBuilder.destroy(registry);
//        }
//    }
//
//    // The applyConstraints method remains the same as previous versions,
//    // as it reads the (now modified) names directly from the constraint/index objects.
//    public void applyConstraints(Metadata metadata, String tenantId, String schemaPlaceholder) {
//        // ... (implementation from previous response)
//        for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//            Table table = persistentClass.getTable();
//            String tableName = table.getName();
//
//            // Handle Unique Constraints
//            for (UniqueKey uniqueKey : table.getUniqueKeys().values()) {
//                if (uniqueKey.getColumns().isEmpty()) continue;
//                String constraintName = uniqueKey.getName(); // Should be tenant-specific
//                String columns = uniqueKey.getColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//                if (!databaseService.constraintExists(tableName, constraintName)) {
//                    String sql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + constraintName + " UNIQUE (" + columns + ");";
//                    databaseService.executeSql(sql);
//                }
//            }
//
//            // Handle Foreign Key Constraints
//            for (ForeignKey foreignKey : table.getForeignKeys().values()) {
//                if (foreignKey.getColumns().isEmpty() || foreignKey.getReferencedColumns().isEmpty()) continue;
//
//                String fkName = "fk_" + tenantId + "_" + foreignKey.getName(); // optionally tenant-specific
//                String columnNames = foreignKey.getColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//
//                String referencedTableName = foreignKey.getReferencedTable().getName(); // also make tenant-specific if needed
//                String referencedColumns = foreignKey.getReferencedColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//
//                if (!databaseService.constraintExists(tableName, fkName)) {
//                    String fkSql = "ALTER TABLE " + tableName + " ADD CONSTRAINT " + fkName +
//                            " FOREIGN KEY (" + columnNames + ") REFERENCES " + referencedTableName +
//                            " (" + referencedColumns + ");";
//                    databaseService.executeSql(fkSql);
//                    System.out.println("Added FK: " + fkName + " on table: " + tableName);
//                } else {
//                    System.out.println("FK already exists: " + fkName);
//                }
//            }
//
//            // Handle Indexes
//            for (org.hibernate.mapping.Index index : table.getIndexes().values()) {
//                if (index.getColumns().isEmpty()) continue;
//                String indexName = index.getName(); // Should be tenant-specific
//                String columnNames = index.getColumns().stream()
//                        .map(Column::getName)
//                        .collect(Collectors.joining(", "));
//                if (!databaseService.indexExists(indexName)) {
//                    String indexSql = "CREATE INDEX " + indexName + " ON " + tableName + " (" + columnNames + ");";
//                    databaseService.executeSql(indexSql);
//                }
//            }
//        }
//        System.out.println("Finished applying constraints for tenant: " + tenantId);
//    }
//}
//----------------Working----------------
//for (PersistentClass persistentClass : metadata.getEntityBindings()) {
//Table table = persistentClass.getTable();
//String originalTableName = table.getName();
//
//                if (originalTableName.contains(schemaPlaceholder)) {
//String newTableName = originalTableName.replace(schemaPlaceholder, tenantId);
//                    table.setName(newTableName);
//                    System.out.println("Prepared rename for table: " + originalTableName + " -> " + newTableName);
//                }
//
//                        // --- Unique Keys --- (Rename in-place, no map clearing)
//                        for (UniqueKey uk : table.getUniqueKeys().values()) {
//String originalUkName = uk.getName();
//                    if (originalUkName.contains(schemaPlaceholder)) {
//String newUkName = originalUkName.replace(schemaPlaceholder, tenantId);
//                        uk.setName(newUkName);
//                        System.out.println("Table " + table.getName() + ": Prepared rename for UK: " + originalUkName + " -> " + newUkName);
//        }
//        }
//
//        // --- Indexes --- (Rename in-place)
//        for (Index index : table.getIndexes().values()) {
//String originalIndexName = index.getName();
//                    if (originalIndexName.contains(schemaPlaceholder)) {
//String newIndexName = originalIndexName.replace(schemaPlaceholder, tenantId);
//                        index.setName(newIndexName);
//                        System.out.println("Table " + table.getName() + ": Prepared rename for Index: " + originalIndexName + " -> " + newIndexName);
//        }
//        }
//
//        // --- Foreign Keys --- (Rename in-place)
//        for (ForeignKey fk : table.getForeignKeys().values()) {
//String originalFkName = fk.getName();
//                    if (originalFkName.contains(schemaPlaceholder)) {
//String newFkName = originalFkName.replace(schemaPlaceholder, tenantId);
//                        fk.setName(newFkName);
//                        System.out.println("Table " + table.getName() + ": Updated FK name (constraint): " + originalFkName + " -> " + newFkName);
//        }
//        }
//        }
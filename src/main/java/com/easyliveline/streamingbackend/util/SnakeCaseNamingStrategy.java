package com.easyliveline.streamingbackend.util;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class SnakeCaseNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return name == null ? null : convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return name == null ? null : convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return name == null ? null : convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        // You can return the identifier directly or apply the snake_case transformation.
        return name == null ? null : convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return name == null ? null : convertToSnakeCase(name);
    }

    private Identifier convertToSnakeCase(Identifier name) {
        if (name == null) {
            return null;
        }

        String newName = name.getText().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return Identifier.toIdentifier(newName);
    }
}

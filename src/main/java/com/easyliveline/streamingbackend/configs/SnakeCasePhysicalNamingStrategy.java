package com.easyliveline.streamingbackend.configs;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.stereotype.Component;

@Component
public class SnakeCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return convertToSnakeCase(name);
    }

    private Identifier convertToSnakeCase(Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        String name = identifier.getText();
        String snakeCaseName = name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        return new Identifier(snakeCaseName, identifier.isQuoted());
    }
}
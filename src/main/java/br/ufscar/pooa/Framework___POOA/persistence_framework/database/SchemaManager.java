package br.ufscar.pooa.Framework___POOA.persistence_framework.database;

import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Entity;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gerencia o esquema (schema) do banco de dados, criando tabelas para as entidades registradas, caso elas não existam.
 * Esta classe orquestra o processo de configuração do esquema na inicialização da aplicação.
 */
public class SchemaManager {
    private final DatabaseManager databaseManager;
    private final DDLGenerator ddlGenerator;
    private final List<Class<?>> entityClasses;

    public SchemaManager(DatabaseManager databaseManager, DDLGenerator ddlGenerator, List<Class<?>> entityClasses) {
        this.databaseManager = databaseManager;
        this.ddlGenerator = ddlGenerator;
        this.entityClasses = entityClasses;
    }

    /**
     * Executa o processo de configuração do esquema. Ele itera sobre todas as classes de entidade registradas,
     * verifica se uma tabela correspondente existe e a cria caso não exista.
     */
    public void initializeSchema() {
        System.out.println("Starting database schema initialization...");
        try {
            for (Class<?> entityClass : entityClasses) {
                String tableName = getTableName(entityClass);
                if (!tableExists(tableName)) {
                    System.out.println("Table '" + tableName + "' not found. Creating...");
                    createTable(tableName, entityClass);
                    System.out.println("Table '" + tableName + "' created successfully.");
                } else {
                    System.out.println("Table '" + tableName + "' already exists. Skipping.");
                }
            }
            System.out.println("Schema initialization finished successfully.");
        } catch (SQLException e) {
            System.err.println("A critical error occurred during schema initialization. Application may not work correctly.");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private void createTable(String tableName, Class<?> entityClass) throws SQLException {
        List<Field> columns = getColumns(entityClass);
        String createTableSql = ddlGenerator.generateCreateTableSQL(tableName, columns);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(createTableSql)) {
            statement.executeUpdate();
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        var metaData = databaseManager.getConnection().getMetaData();
        var tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"});
        boolean exists = tables.next();
        tables.close();
        return exists;
    }

    private String getTableName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " is not an @Entity");
        }
        return clazz.getAnnotation(Entity.class).tableName();
    }

    private List<Field> getColumns(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        fields.removeIf(field -> !field.isAnnotationPresent(Column.class));
        return fields;
    }
}
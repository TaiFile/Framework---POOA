package br.ufscar.pooa.Framework___POOA.framework;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Entity;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Id;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersistenceFramework {
    private final DatabaseManager databaseManager;
    private final DDLGenerator ddlGenerator = new DDLGenerator();
    private final DQLGenerator dqlGenerator = new DQLGenerator();

    public PersistenceFramework(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private String getTableName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Object is not an entity");
        }
        return clazz.getAnnotation(Entity.class).tableName();
    }

    private List<Field> getColumns(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(List.of(clazz.getDeclaredFields()));
        fields.removeIf(field -> !field.isAnnotationPresent(Column.class));
        fields.forEach(field -> field.setAccessible(true));
        return fields;
    }

    private Field getIdField(List<Field> columns) {
        return columns.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entity must have an @Id field"));
    }

    private <T> T mapResultSetToEntity(ResultSet resultSet, Class<T> clazz, List<Field> columns) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Field field : columns) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.name();
            Object value = resultSet.getObject(columnName);
            field.set(instance, value);
        }
        return instance;
    }

    private void handleSQLException(SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        System.err.println(Arrays.toString(e.getStackTrace()));
    }

    private void handleReflectionException(Exception e) {
        String exceptionType = e.getClass().getSimpleName();
        System.err.println(exceptionType + ": " + e.getMessage());
        System.err.println(Arrays.toString(e.getStackTrace()));
    }

    public void insert(Object object) {
        Class<?> clazz = object.getClass();
        String tableName = getTableName(clazz);
        List<Field> columns = getColumns(clazz);

        if (!tableExists(tableName)) {
            createTableIfNotExists(tableName, columns);
        }

        List<Field> insertColumns = columns.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .toList();

        executeInsert(object, tableName, insertColumns);
    }

    private void createTableIfNotExists(String tableName, List<Field> columns) {
        String createTableSql = ddlGenerator.generateCreateTableSQL(tableName, columns);
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(createTableSql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private void executeInsert(Object object, String tableName, List<Field> insertColumns) {
        String insertSql = dqlGenerator.generateInsertSQL(tableName, insertColumns);
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            int parameterIndex = 1;
            for (Field field : insertColumns) {
                Object value = field.get(object);
                statement.setObject(parameterIndex++, value);
            }
            statement.executeUpdate();
            System.out.println("Executed:" + insertSql);
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (IllegalAccessException e) {
            handleReflectionException(e);
        }
    }

    private boolean tableExists(String tableName) {
        try {
            DatabaseMetaData metaData = databaseManager.getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(),
                    new String[]{"TABLE"});
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existência da tabela: " + e.getMessage());
            return false;
        }
    }

    public <T> T findById(Class<T> clazz, Object id) {
        String tableName = getTableName(clazz);
        List<Field> columns = getColumns(clazz);
        Field idField = getIdField(columns);

        String selectSql = dqlGenerator.generateSelectByIdSQL(tableName, columns, idField);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(selectSql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    T instance = mapResultSetToEntity(resultSet, clazz, columns);
                    System.out.println("Executed: " + selectSql);
                    return instance;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (Exception e) {
            handleReflectionException(e);
        }

        return null;
    }

    public <T> List<T> findAll(Class<T> clazz) {
        String tableName = getTableName(clazz);
        List<Field> columns = getColumns(clazz);
        String selectAllSql = dqlGenerator.generateSelectAllSQL(tableName, columns);

        List<T> results = new ArrayList<>();
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(selectAllSql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    T instance = mapResultSetToEntity(resultSet, clazz, columns);
                    results.add(instance);
                }
            }
            System.out.println("Executed: " + selectAllSql);
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (Exception e) {
            handleReflectionException(e);
        }

        return results;
    }

    public <T> boolean exists(Class<T> clazz, Object id) {
        String tableName = getTableName(clazz);
        List<Field> columns = getColumns(clazz);
        Field idField = getIdField(columns);

        String existsSql = dqlGenerator.generateExistsSQL(tableName, idField);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(existsSql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next() && resultSet.getInt(1) > 0;
                System.out.println("Executed: " + existsSql);
                return exists;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
}
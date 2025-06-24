package br.ufscar.pooa.Framework___POOA.framework;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Entity;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Id;
import br.ufscar.pooa.Framework___POOA.framework.database.DDLGenerator;
import br.ufscar.pooa.Framework___POOA.framework.database.DQLGenerator;
import br.ufscar.pooa.Framework___POOA.framework.database.DatabaseManager;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SimpleFrameworkRepository<T, ID extends Serializable> implements IFrameworkRepository<T, ID> {

    private final DatabaseManager databaseManager;
    private final Class<T> domainClass;
    private final DDLGenerator ddlGenerator = new DDLGenerator();
    private final DQLGenerator dqlGenerator = new DQLGenerator();

    public SimpleFrameworkRepository(DatabaseManager databaseManager, Class<T> domainClass) {
        this.databaseManager = databaseManager;
        this.domainClass = domainClass;
    }

    @Override
    public T save(T entity) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);

        if (!tableExists(tableName)) {
            createTableIfNotExists(tableName, columns);
        }

        List<Field> insertColumns = columns.stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .toList();

        return executeInsert(entity, tableName, insertColumns);
    }

    @Override
    public Optional<T> findById(ID id) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field idField = getIdField(columns);

        String selectSql = dqlGenerator.generateSelectByIdSQL(tableName, columns, idField);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(selectSql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    T instance = mapResultSetToEntity(resultSet, domainClass, columns);
                    return Optional.of(instance);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (Exception e) {
            handleReflectionException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<T> findAll() {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        String selectAllSql = dqlGenerator.generateSelectAllSQL(tableName, columns);

        List<T> results = new ArrayList<>();
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(selectAllSql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    T instance = mapResultSetToEntity(resultSet, domainClass, columns);
                    results.add(instance);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (Exception e) {
            handleReflectionException(e);
        }

        return results;
    }

    @Override
    public boolean existsById(ID id) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field idField = getIdField(columns);

        String existsSql = dqlGenerator.generateExistsSQL(tableName, idField);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(existsSql)) {
            statement.setObject(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }

    // Helper methods from PersistenceFramework
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

    private T mapResultSetToEntity(ResultSet resultSet, Class<T> clazz, List<Field> columns) throws Exception {
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

    private void createTableIfNotExists(String tableName, List<Field> columns) {
        String createTableSql = ddlGenerator.generateCreateTableSQL(tableName, columns);
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(createTableSql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private T executeInsert(T entity, String tableName, List<Field> insertColumns) {
        String insertSql = dqlGenerator.generateInsertSQL(tableName, insertColumns);
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            int parameterIndex = 1;
            for (Field field : insertColumns) {
                Object value = field.get(entity);
                statement.setObject(parameterIndex++, value);
            }

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating entity failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Field idField = getIdField(getColumns(entity.getClass()));
                    Object generatedId = generatedKeys.getObject(1);
                    idField.set(entity, generatedId);
                } else {
                    throw new SQLException("Creating entity failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (IllegalAccessException e) {
            handleReflectionException(e);
        }
        return entity;
    }

    private boolean tableExists(String tableName) {
        try {
            var metaData = databaseManager.getConnection().getMetaData();
            var tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"});
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (SQLException e) {
            System.err.println("Erro ao verificar existência da tabela: " + e.getMessage());
            return false;
        }
    }
}
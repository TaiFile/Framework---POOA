package br.ufscar.pooa.Framework___POOA.persistence_framework;



import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Entity;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Enumerated;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Id;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.DDLGenerator;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.DQLGenerator;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.DatabaseManager;

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
        Field idField = getIdField(domainClass);

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
    public Optional<T> findBy(String fieldName, Object value) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field searchField = findFieldByName(columns, fieldName);

        String selectSql = dqlGenerator.generateSelectByFieldSQL(tableName, columns, searchField);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(selectSql)) {
            statement.setObject(1, value);

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
        Field idField = getIdField(domainClass);

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

    @Override
    public boolean existsBy(String fieldName, Object value) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field searchField = findFieldByName(columns, fieldName);

        String existsSql = dqlGenerator.generateExistsByFieldSQL(tableName, searchField);

        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(existsSql)) {
            statement.setObject(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T update(T entity) {
        try {
            String tableName = getTableName(domainClass);
            List<Field> columns = getColumns(domainClass);
            Field idField = getIdField(domainClass);
            createTableIfNotExists(tableName, columns);

            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            
            if (idValue == null) {
                throw new IllegalArgumentException("Entidade deve ter um ID para ser atualizada");
            }

            if (!existsById((ID) idValue)) {
                throw new IllegalArgumentException("Entidade com ID " + idValue + " não existe");
            }

            String updateSQL = dqlGenerator.generateUpdateSQL(tableName, columns, idField);
            
            try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(updateSQL)) {
                int parameterIndex = 1;
                for (Field field : columns) {
                    if (field.isAnnotationPresent(Id.class)) {
                        continue;
                    }
                    
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (field.isAnnotationPresent(Enumerated.class) && value != null) {
                        value = mapEnumToDatabase(value, field);
                    }
                    
                    statement.setObject(parameterIndex++, value);
                }

                statement.setObject(parameterIndex, idValue);
                int rowsAffected = statement.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new RuntimeException("Nenhuma linha foi atualizada");
                }
                
                return entity;
                
            } catch (SQLException e) {
                handleSQLException(e);
                return null;
            }
            
        } catch (Exception e) {
            handleReflectionException(e);
            return null;
        }
    }

    @Override
    public void delete(T entity) {
        try {
            String tableName = getTableName(domainClass);
            Field idField = getIdField(domainClass);

            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            if (idValue == null) {
                throw new IllegalArgumentException("Entidade deve ter um ID para ser deletada");
            }

            if (!existsById((ID) idValue)) {
                throw new IllegalArgumentException("Entidade com ID " + idValue + " não existe");
            }

            String deleteSql = dqlGenerator.generateDeleteSQL(tableName, idField);

            try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(deleteSql)) {
                statement.setObject(1, idValue);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new RuntimeException("Nenhuma linha foi deletada");
                }

            } catch (SQLException e) {
                handleSQLException(e);
            }

        } catch (Exception e) {
            handleReflectionException(e);
        }
    }

    /**
     * Helper methods
     */
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

    private Field getIdField(Class<?> clazz) {
        List<Field> fields = new ArrayList<>(List.of(clazz.getDeclaredFields()));
        Field idField = fields.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entity must have an @Id field"));

        idField.setAccessible(true);
        return idField;
    }

    private Field findFieldByName(List<Field> columns, String fieldName) {
        return columns.stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No field named '" + fieldName + "' in entity " + domainClass.getSimpleName()));
    }

    private T mapResultSetToEntity(ResultSet resultSet, Class<T> clazz, List<Field> columns) throws Exception {
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (Field field : columns) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.name();

            Object value;

            // Tratamento especial para ENUMs
            if (field.getType().isEnum()) {
                value = mapEnumFromDatabase(resultSet, columnName, field);  // ← AQUI!
            } else {
                value = resultSet.getObject(columnName);
            }

            field.set(instance, value);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private Object mapEnumFromDatabase(ResultSet resultSet, String columnName, Field field) throws SQLException {
        Class<Enum> enumClass = (Class<Enum>) field.getType();
        Enumerated enumAnnotation = field.getAnnotation(Enumerated.class);

        if (enumAnnotation != null && enumAnnotation.value() == Enumerated.EnumType.STRING) {
            String enumStringValue = resultSet.getString(columnName);
            if (enumStringValue == null) return null;

            return Enum.valueOf(enumClass, enumStringValue);
        } else {
            int ordinalValue = resultSet.getInt(columnName);
            if (resultSet.wasNull()) return null;

            Enum[] enumConstants = enumClass.getEnumConstants();
            if (ordinalValue >= 0 && ordinalValue < enumConstants.length) {
                return enumConstants[ordinalValue];
            }
            return null;
        }
    }

    private Object mapEnumToDatabase(Object enumValue, Field field) {
        if (enumValue == null) return null;

        Enumerated enumAnnotation = field.getAnnotation(Enumerated.class);

        if (enumAnnotation != null && enumAnnotation.value() == Enumerated.EnumType.STRING) {
            return ((Enum<?>) enumValue).name();
        } else {
            return ((Enum<?>) enumValue).ordinal();
        }
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

                if (field.getType().isEnum() && value != null) {
                    Object enumValue = mapEnumToDatabase(value, field);
                    statement.setObject(parameterIndex++, enumValue);
                } else {
                    statement.setObject(parameterIndex++, value);
                }
            }

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating entity failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Field idField = getIdField(domainClass);

                    Object generatedId = generatedKeys.getObject(1);
                    Object convertedId = convertIdValue(generatedId, idField.getType());

                    idField.set(entity, convertedId);
                } else {
                    throw new SQLException("Creating entity failed, no ID obtained.");
                }
            }

            return entity;
        } catch (SQLException e) {
            handleSQLException(e);
        } catch (IllegalAccessException e) {
            handleReflectionException(e);
        }

        return null;
    }

    private Object convertIdValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        // Se os tipos já são compatíveis
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Conversões específicas
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        } else if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }

        return value;
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
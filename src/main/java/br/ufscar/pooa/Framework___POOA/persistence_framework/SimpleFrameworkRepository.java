package br.ufscar.pooa.Framework___POOA.persistence_framework;

import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Entity;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Enumerated;
import br.ufscar.pooa.Framework___POOA.persistence_framework.annotation.Id;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.DQLGenerator;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.JDBCExecutor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SimpleFrameworkRepository<T, ID extends Serializable> implements IFrameworkRepository<T, ID> {

    private final Class<T> domainClass;
    private final DQLGenerator dqlGenerator;
    private final JDBCExecutor jdbcExecutor;

    public SimpleFrameworkRepository(JDBCExecutor jdbcExecutor, DQLGenerator dqlGenerator, Class<T> domainClass) {
        this.jdbcExecutor = jdbcExecutor;
        this.domainClass = domainClass;
        this.dqlGenerator = dqlGenerator;
    }

    @Override
    public T save(T entity) {
        String tableName = getTableName(domainClass);

        List<Field> insertColumns = getColumns(domainClass).stream()
                .filter(field -> !field.isAnnotationPresent(Id.class))
                .toList();

        String insertSql = dqlGenerator.generateInsertSQL(tableName, insertColumns);
        try {

            List<Object> params = new ArrayList<>();
            for (Field field : insertColumns) {
                field.setAccessible(true);
                Object value = field.get(entity);
                params.add(field.isAnnotationPresent(Enumerated.class) ? mapEnumToDatabase(value, field) : value);
            }

            try (ResultSet generatedKeys = jdbcExecutor.executeInsertAndReturnGeneratedKeys(insertSql, params.toArray())) {
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
        } catch (Exception e) {
            handleReflectionException(e);
            throw new RuntimeException("Save operation failed", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field idField = getIdField(domainClass);
        String selectSql = dqlGenerator.generateSelectByIdSQL(tableName, columns, idField);
        return jdbcExecutor.executeQuery(selectSql, rs -> {
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs, domainClass, columns));
            }
            return Optional.empty();
        }, id);
    }

    @Override
    public Optional<T> findBy(String fieldName, Object value) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field searchField = findFieldByName(columns, fieldName);
        String selectSql = dqlGenerator.generateSelectByFieldSQL(tableName, columns, searchField);
        return jdbcExecutor.executeQuery(selectSql, rs -> {
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs, domainClass, columns));
            }
            return Optional.empty();
        }, value);
    }

    @Override
    public List<T> findAll() {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        String selectAllSql = dqlGenerator.generateSelectAllSQL(tableName, columns);
        return jdbcExecutor.executeQuery(selectAllSql, rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs, domainClass, columns));
            }
            return results;
        });
    }

    @Override
    public boolean existsById(ID id) {
        String tableName = getTableName(domainClass);
        Field idField = getIdField(domainClass);
        String existsSql = dqlGenerator.generateExistsSQL(tableName, idField);
        return jdbcExecutor.executeQuery(existsSql, rs -> rs.next() && rs.getInt(1) > 0, id);
    }

    @Override
    public boolean existsBy(String fieldName, Object value) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field searchField = findFieldByName(columns, fieldName);
        String existsSql = dqlGenerator.generateExistsByFieldSQL(tableName, searchField);
        return jdbcExecutor.executeQuery(existsSql, rs -> rs.next() && rs.getInt(1) > 0, value);
    }

    @Override
    public T update(T entity) {
        String tableName = getTableName(domainClass);
        List<Field> columns = getColumns(domainClass);
        Field idField = getIdField(domainClass);
        try {
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            if (idValue == null) throw new IllegalArgumentException("Entity must have an ID to be updated");
            String updateSQL = dqlGenerator.generateUpdateSQL(tableName, columns, idField);
            List<Object> params = new ArrayList<>();
            for (Field field : columns) {
                if (!field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    params.add(field.isAnnotationPresent(Enumerated.class) ? mapEnumToDatabase(value, field) : value);
                }
            }
            params.add(idValue);
            int rowsAffected = jdbcExecutor.executeModification(updateSQL, params.toArray());
            if (rowsAffected == 0) throw new RuntimeException("No rows were updated for entity ID: " + idValue);
            return entity;
        } catch (Exception e) {
            handleReflectionException(e);
            throw new RuntimeException("Update failed", e);
        }
    }

    @Override
    public void delete(T entity) {
        try {
            String tableName = getTableName(domainClass);
            Field idField = getIdField(domainClass);
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            if (idValue == null) throw new IllegalArgumentException("Entity must have an ID to be deleted");
            String deleteSql = dqlGenerator.generateDeleteSQL(tableName, idField);
            int rowsAffected = jdbcExecutor.executeModification(deleteSql, idValue);
            if (rowsAffected == 0) throw new RuntimeException("No rows were deleted for entity ID: " + idValue);
        } catch (Exception e) {
            handleReflectionException(e);
            throw new RuntimeException("Delete failed", e);
        }
    }

    // --- Helpers ---

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
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .map(field -> {
                    field.setAccessible(true);
                    return field;
                })
                .orElseThrow(() -> new IllegalArgumentException("Entity must have an @Id field"));
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
            String columnName = field.getAnnotation(Column.class).name();
            Object value = field.getType().isEnum()
                    ? mapEnumFromDatabase(resultSet, columnName, field)
                    : resultSet.getObject(columnName);
            field.set(instance, value);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private Object mapEnumFromDatabase(ResultSet resultSet, String columnName, Field field) throws SQLException {
        Enumerated enumAnnotation = field.getAnnotation(Enumerated.class);
        if (enumAnnotation != null && enumAnnotation.value() == Enumerated.EnumType.STRING) {
            String enumStringValue = resultSet.getString(columnName);
            return enumStringValue == null ? null : Enum.valueOf((Class<Enum>) field.getType(), enumStringValue);
        } else {
            int ordinalValue = resultSet.getInt(columnName);
            if (resultSet.wasNull()) return null;
            Enum<?>[] enumConstants = (Enum<?>[]) field.getType().getEnumConstants();
            return (ordinalValue >= 0 && ordinalValue < enumConstants.length) ? enumConstants[ordinalValue] : null;
        }
    }

    private Object mapEnumToDatabase(Object enumValue, Field field) {
        if (enumValue == null) return null;
        Enumerated enumAnnotation = field.getAnnotation(Enumerated.class);
        return (enumAnnotation != null && enumAnnotation.value() == Enumerated.EnumType.STRING)
                ? ((Enum<?>) enumValue).name()
                : ((Enum<?>) enumValue).ordinal();
    }

    private Object convertIdValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
        } else if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) return ((Number) value).intValue();
        }
        return value;
    }

    // --- Exception Handling ---

    private void handleSQLException(SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        System.err.println(Arrays.toString(e.getStackTrace()));
    }

    private void handleReflectionException(Exception e) {
        System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        System.err.println(Arrays.toString(e.getStackTrace()));
    }
}
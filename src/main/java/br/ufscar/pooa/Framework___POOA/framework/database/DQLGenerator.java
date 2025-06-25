package br.ufscar.pooa.Framework___POOA.framework.database;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Column;
import br.ufscar.pooa.Framework___POOA.framework.annotation.Id;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class DQLGenerator {

    public String generateInsertSQL(String tableName, List<Field> insertColumns) {
        String columnNames = insertColumns.stream().map(Field::getName).collect(Collectors.joining(","));
        String questionMarks = insertColumns.stream().map(i -> "?").collect(Collectors.joining(","));

        return String.format("""
                INSERT INTO %s (%s)
                VALUES (%s)
                """, tableName, columnNames, questionMarks);
    }
    
    public String generateUpdateSQL(String tableName, List<Field> updateColumns, Field idField) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName);
        sql.append(" SET ");

        for (int i = 0; i < updateColumns.size(); i++) {
            Field field = updateColumns.get(i);
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            
            Column columnAnnotation = field.getAnnotation(Column.class);
            sql.append(columnAnnotation.name()).append(" = ?");
            
            if (i < updateColumns.size() - 1) {
                sql.append(", ");
            }
        }

        String sqlStr = sql.toString();
        if (sqlStr.endsWith(", ")) {
            sqlStr = sqlStr.substring(0, sqlStr.length() - 2);
        }
        sql = new StringBuilder(sqlStr);
        sql.append(" WHERE ");
        Column idColumnAnnotation = idField.getAnnotation(Column.class);
        sql.append(idColumnAnnotation.name()).append(" = ?");
        
        return sql.toString();
    }

    public String generateSelectByIdSQL(String tableName, List<Field> columns, Field idField) {
        StringBuilder sql = new StringBuilder("SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            Field currentField = columns.get(i);
            Column columnAnnotation = currentField.getAnnotation(Column.class);
            sql.append(columnAnnotation.name());

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(" FROM ").append(tableName);
        sql.append(" WHERE ");

        Column idColumnAnnotation = idField.getAnnotation(Column.class);
        sql.append(idColumnAnnotation.name()).append(" = ?");

        return sql.toString();
    }

    public String generateSelectByFieldSQL(String tableName, List<Field> columns, Field field) {
        StringBuilder sql = new StringBuilder("SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            Field currentField = columns.get(i);
            Column columnAnnotation = currentField.getAnnotation(Column.class);
            sql.append(columnAnnotation.name());

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(" FROM ").append(tableName);
        sql.append(" WHERE ");

        Column fieldAnnotation = field.getAnnotation(Column.class);
        sql.append(fieldAnnotation.name()).append(" = ?");

        return sql.toString();
    }

    public String generateSelectAllSQL(String tableName, List<Field> columns) {
        StringBuilder sql = new StringBuilder("SELECT ");

        for (int i = 0; i < columns.size(); i++) {
            Field currentField = columns.get(i);
            Column columnAnnotation = currentField.getAnnotation(Column.class);
            sql.append(columnAnnotation.name());

            if (i < columns.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(" FROM ").append(tableName);

        return sql.toString();
    }

    public String generateExistsSQL(String tableName, Field idField) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");

        Column idColumnAnnotation = idField.getAnnotation(Column.class);
        sql.append(idColumnAnnotation.name()).append(" = ?");

        return sql.toString();
    }

    public String generateExistsByFieldSQL(String tableName, java.lang.reflect.Field field) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(tableName);
        sql.append(" WHERE ");

        Column columnAnnotation = field.getAnnotation(Column.class);
        sql.append(columnAnnotation.name()).append(" = ?");

        return sql.toString();
    }
}
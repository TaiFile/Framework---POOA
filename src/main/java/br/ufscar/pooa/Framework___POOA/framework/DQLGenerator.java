package br.ufscar.pooa.Framework___POOA.framework;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Column;

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
public String generateSelectByIdSQL(String tableName, List<Field> columns, Field idField) {
    StringBuilder sql = new StringBuilder("SELECT ");
    
    // Adicionar todas as colunas
    for (int i = 0; i < columns.size(); i++) {
        Field field = columns.get(i);
        Column columnAnnotation = field.getAnnotation(Column.class);
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
public String generateSelectAllSQL(String tableName, List<Field> columns) {
    StringBuilder sql = new StringBuilder("SELECT ");
    
    // Adicionar todas as colunas
    for (int i = 0; i < columns.size(); i++) {
        Field field = columns.get(i);
        Column columnAnnotation = field.getAnnotation(Column.class);
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
}
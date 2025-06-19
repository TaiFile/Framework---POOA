package br.ufscar.pooa.Framework___POOA.framework;

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
}


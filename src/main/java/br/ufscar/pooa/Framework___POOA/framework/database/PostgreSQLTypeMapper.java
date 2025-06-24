package br.ufscar.pooa.Framework___POOA.framework.database;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Enumerated;

import java.lang.reflect.Field;
public class PostgreSQLTypeMapper {
    
    public static String getPostgreSQLType(Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType.isEnum()) {
            return getEnumPostgreSQLType(field);
        }

        if (fieldType == String.class) return "VARCHAR(255)";
        if (fieldType == Integer.class || fieldType == int.class) return "INTEGER";
        if (fieldType == Long.class || fieldType == long.class) return "BIGINT";
        if (fieldType == Boolean.class || fieldType == boolean.class) return "BOOLEAN";
        if (fieldType == Double.class || fieldType == double.class) return "DOUBLE PRECISION";
        if (fieldType == Float.class || fieldType == float.class) return "REAL";
        
        return "TEXT";
    }

    private static String getEnumPostgreSQLType(Field field) {
        Enumerated enumAnnotation = field.getAnnotation(Enumerated.class);
        
        if (enumAnnotation != null && enumAnnotation.value() == Enumerated.EnumType.STRING) {
            return "VARCHAR(50)";
        } else {
            return "INTEGER";
        }
    }
}
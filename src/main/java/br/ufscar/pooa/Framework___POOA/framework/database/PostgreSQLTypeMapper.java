package br.ufscar.pooa.Framework___POOA.framework.database;

import br.ufscar.pooa.Framework___POOA.framework.annotation.Enumerated;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PostgreSQLTypeMapper {

    private PostgreSQLTypeMapper() {
    }

    private static final Map<Class<?>, String> TYPE_MAPPINGS = new HashMap<>();

    static {
        // Tipos primitivos e wrappers
        TYPE_MAPPINGS.put(byte.class, "SMALLINT");
        TYPE_MAPPINGS.put(Byte.class, "SMALLINT");
        TYPE_MAPPINGS.put(short.class, "SMALLINT");
        TYPE_MAPPINGS.put(Short.class, "SMALLINT");
        TYPE_MAPPINGS.put(int.class, "INTEGER");
        TYPE_MAPPINGS.put(Integer.class, "INTEGER");
        TYPE_MAPPINGS.put(long.class, "BIGINT");
        TYPE_MAPPINGS.put(Long.class, "BIGINT");
        TYPE_MAPPINGS.put(float.class, "REAL");
        TYPE_MAPPINGS.put(Float.class, "REAL");
        TYPE_MAPPINGS.put(double.class, "DOUBLE PRECISION");
        TYPE_MAPPINGS.put(Double.class, "DOUBLE PRECISION");
        TYPE_MAPPINGS.put(boolean.class, "BOOLEAN");
        TYPE_MAPPINGS.put(Boolean.class, "BOOLEAN");
        TYPE_MAPPINGS.put(char.class, "CHAR(1)");
        TYPE_MAPPINGS.put(Character.class, "CHAR(1)");

        // Tipos de objeto comuns
        TYPE_MAPPINGS.put(String.class, "VARCHAR(255)");
        TYPE_MAPPINGS.put(BigDecimal.class, "DECIMAL(19,2)");

        // Tipos de data/hora
        TYPE_MAPPINGS.put(LocalDate.class, "DATE");
        TYPE_MAPPINGS.put(LocalDateTime.class, "TIMESTAMP");
        TYPE_MAPPINGS.put(java.sql.Date.class, "DATE");
        TYPE_MAPPINGS.put(java.sql.Timestamp.class, "TIMESTAMP");

        // Arrays
        TYPE_MAPPINGS.put(byte[].class, "BYTEA");
    }

    public static String getPostgreSQLType(Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType.isEnum()) {
            return getEnumPostgreSQLType(field);
        }

        String sqlType = TYPE_MAPPINGS.get(fieldType);
        if (sqlType != null) {
            return sqlType;
        }

        return "VARCHAR(255)";
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
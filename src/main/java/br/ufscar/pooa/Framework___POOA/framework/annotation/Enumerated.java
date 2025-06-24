package br.ufscar.pooa.Framework___POOA.framework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Enumerated {
    EnumType value() default EnumType.ORDINAL;
    
    enum EnumType {
        ORDINAL,
        STRING
    }
}
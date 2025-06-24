package br.ufscar.pooa.Framework___POOA.framework;

import br.ufscar.pooa.Framework___POOA.framework.database.DatabaseManager;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class FrameworkRepositoryFactory<T, ID extends Serializable> {
    private final DatabaseManager databaseManager;

    public FrameworkRepositoryFactory(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public SimpleFrameworkRepository<T, ID> getRepository(Class<T> entityClass) {
        return new SimpleFrameworkRepository<>(databaseManager, entityClass);
    }
}
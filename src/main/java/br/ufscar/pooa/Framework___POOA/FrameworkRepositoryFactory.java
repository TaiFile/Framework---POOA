package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.persistence_framework.IFrameworkRepository;
import br.ufscar.pooa.Framework___POOA.persistence_framework.SimpleFrameworkRepository;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.DatabaseManager;

import java.io.Serializable;

public class FrameworkRepositoryFactory<T, ID extends Serializable> {
    private final DatabaseManager databaseManager;

    public FrameworkRepositoryFactory(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public IFrameworkRepository<T, ID> getRepository(Class<T> entityClass) {
        return new SimpleFrameworkRepository<>(databaseManager, entityClass);
    }
}
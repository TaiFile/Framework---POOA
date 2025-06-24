package br.ufscar.pooa.Framework___POOA.framework;

import br.ufscar.pooa.Framework___POOA.framework.database.DatabaseManager;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class FrameworkRepositoryFactory {
    private final DatabaseManager databaseManager;

    public FrameworkRepositoryFactory(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public <T, ID extends Serializable, R extends IFrameworkRepository<T, ID>> R getRepository(Class<R> repositoryInterface) {
        Class<T> domainClass = getDomainClassFromInterface(repositoryInterface);
        SimpleFrameworkRepository<T, ID> backingRepository = new SimpleFrameworkRepository<>(databaseManager, domainClass);
        return createRepositoryProxy(repositoryInterface, backingRepository);
    }

    /**
     * Creates a dynamic proxy for the given repository interface that delegates method calls
     * to the provided backing repository implementation.
     */
    @SuppressWarnings("unchecked")
    private <T, ID extends Serializable, R> R createRepositoryProxy(Class<R> repositoryInterface,
                                                                    SimpleFrameworkRepository<T, ID> backingRepository) {
        return (R) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class[]{repositoryInterface},
                (proxy, method, args) -> method.invoke(backingRepository, args)
        );
    }

    /**
     * Extracts the domain class type from the repository interface by analyzing its generic type parameters.
     *
     * @param <T>                 The domain type
     * @param repositoryInterface The repository interface to analyze
     * @return The domain class
     * @throws IllegalArgumentException if the interface doesn't properly extend IFrameworkRepository
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> getDomainClassFromInterface(Class<?> repositoryInterface) {
        for (Type genericInterface : repositoryInterface.getGenericInterfaces()) {
            if (!(genericInterface instanceof ParameterizedType parameterizedType)) {
                continue;
            }

            if (parameterizedType.getRawType().equals(IFrameworkRepository.class)) {
                Type domainType = parameterizedType.getActualTypeArguments()[0];
                if (domainType instanceof Class) {
                    return (Class<T>) domainType;
                }
            }
        }
        throw new IllegalArgumentException(
                "The repository interface must extend IFrameworkRepository and specify the entity type."
        );
    }
}
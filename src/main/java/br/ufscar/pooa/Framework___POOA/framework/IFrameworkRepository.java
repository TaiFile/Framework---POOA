package br.ufscar.pooa.Framework___POOA.framework;

import java.util.List;
import java.util.Optional;

public interface IFrameworkRepository<T, ID> {
    T save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);
}

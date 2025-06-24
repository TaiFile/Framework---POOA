package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.framework.IFrameworkRepository;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final IFrameworkRepository<User, Long> repository;

    public UserRepository(IFrameworkRepository<User, Long> repository) {
        this.repository = repository;
    }

    public User save(User entity) {
        return repository.save(entity);
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> findBy(String fieldName, Object value) {
        return repository.findBy(fieldName, value);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    public boolean existsBy(String fieldName, Object value) {
        return repository.existsBy(fieldName, value);
    }
}

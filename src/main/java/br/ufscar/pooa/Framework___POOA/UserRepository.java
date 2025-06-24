package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.framework.IFrameworkRepository;
import br.ufscar.pooa.Framework___POOA.framework.SimpleFrameworkRepository;

import java.util.List;
import java.util.Optional;

public class UserRepository implements IFrameworkRepository<User, Long> {
    private final IFrameworkRepository<User, Long> repository;

    public UserRepository(SimpleFrameworkRepository<User, Long> repository) {
        this.repository = repository;
    }

    @Override
    public User save(User entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }
}

package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.framework.FrameworkRepositoryFactory;
import br.ufscar.pooa.Framework___POOA.framework.database.DatabaseManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseManager databaseManager = new DatabaseManager(
                "jdbc:postgresql://localhost:5432/framework-db",
                "root",
                "root");

        FrameworkRepositoryFactory factory = new FrameworkRepositoryFactory(databaseManager);
        IUserRepository userRepository = factory.getRepository(IUserRepository.class);

        User user = new User();
        user.setName("Vitor");
        user.setAge(23);

        User savedUser = userRepository.save(user);
        System.out.println("Usuário salvo: " + savedUser);
        System.out.println();

        Optional<User> foundUser = userRepository.findById(1L);
        if (foundUser.isPresent()) {
            System.out.println("Usuário encontrado: " + foundUser.get().getName());
        } else {
            System.out.println("Usuário não encontrado");
        }
        System.out.println();

        List<User> allUsers = userRepository.findAll();
        if (!allUsers.isEmpty()) {
            System.out.println("Usuários encontrados:");
            for (User u : allUsers) {
                System.out.println("ID: " + u.getId() + ", Nome: " + u.getName() + ", Idade: " + u.getAge());
            }
        } else {
            System.out.println("Nenhum usuário encontrado");
        }
        System.out.println();

        boolean userExists = userRepository.existsById(1L);
        if (userExists) {
            System.out.println("Usuário com ID 1 existe no banco de dados");
        } else {
            System.out.println("Usuário com ID 1 não existe no banco de dados");
        }
        System.out.println();
    }
}
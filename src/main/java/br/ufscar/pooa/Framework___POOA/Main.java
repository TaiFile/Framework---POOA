package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.Enum.UserGender;
import br.ufscar.pooa.Framework___POOA.framework.IFrameworkRepository;
import br.ufscar.pooa.Framework___POOA.framework.database.DatabaseManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseManager databaseManager = DatabaseManager.getInstance(
                "jdbc:postgresql://localhost:5432/framework-db",
                "root",
                "root");

        FrameworkRepositoryFactory<User, Long> factory = new FrameworkRepositoryFactory<>(databaseManager);
        IFrameworkRepository<User, Long> frameworkRepository = factory.getRepository(User.class);
        UserRepository userRepository = new UserRepository(frameworkRepository);

        User user = new User();
        user.setName("Vitor");
        user.setAge(23);
        user.setGender(UserGender.MALE);

        User savedUser = userRepository.save(user);
        System.out.println("Usuário salvo: " + savedUser);
        System.out.println();

        Optional<User> foundUser = userRepository.findById(1L);
        if (foundUser.isPresent()) {
            System.out.println("Usuário encontrado pelo ID 1: " + foundUser.get().getName());
        } else {
            System.out.println("Usuário não encontrado");
        }
        System.out.println();

        Optional<User> foundUserByName = userRepository.findBy("name", "Vitor");
        if (foundUserByName.isPresent()) {
            System.out.println("Usuário encontrado pelo Nome Vitor: " + foundUserByName.get());
        } else {
            System.out.println("Usuário não encontrado");
        }
        System.out.println();

        List<User> allUsers = userRepository.findAll();
        if (!allUsers.isEmpty()) {
            System.out.println("Usuários encontrados:");
            for (User u : allUsers) {
                System.out.println(u);
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

        boolean userExistsByAge = userRepository.existsBy("age", 23);
        if (userExistsByAge) {
            System.out.println("Usuário com idade 23 existe no banco de dados");
        } else {
            System.out.println("Usuário com ID 23 não existe no banco de dados");
        }
        System.out.println();
    }
}
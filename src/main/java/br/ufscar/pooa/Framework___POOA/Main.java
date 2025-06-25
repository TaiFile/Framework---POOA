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

        Optional<User> userToUpdate = userRepository.findById(savedUser.getId());
        if (userToUpdate.isPresent()) {
            User updateUser = userToUpdate.get();
            updateUser.setName("Vitor Atualizado");
            updateUser.setAge(50);
            updateUser.setGender(UserGender.MALE);
            User loadedUser = userRepository.update(updateUser);
            System.out.println("Dados após o load (update): " + loadedUser);
        } else {
            System.out.println("Usuário não encontrado para atualização");
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

        boolean userExistsByAge = userRepository.existsBy("age", 25);

        if (userExistsByAge) {
            System.out.println("Usuário com idade 25 existe no banco de dados");
        } else {
            System.out.println("Usuário com idade 25 não existe no banco de dados");
        }
        System.out.println();

        System.out.println("Testando load() com usuário inexistente");
        try {
            User nonExistentUser = new User();
            nonExistentUser.setId(999L);
            nonExistentUser.setName("Usuário Inexistente");
            nonExistentUser.setAge(30);
            nonExistentUser.setGender(UserGender.MALE);
            
            userRepository.update(nonExistentUser);
        } catch (Exception e) {
            System.out.println("Erro esperado ao tentar fazer load de usuário inexistente: " + e.getMessage());
        }
        System.out.println();

        System.out.println("Testando delete de usuário");
        Optional<User> userToDelete = userRepository.findById(savedUser.getId());
        if (userToDelete.isPresent()) {
            User deletedUser = userRepository.delete(userToDelete.get());
            if (deletedUser != null) {
                System.out.println("Usuário deletado com sucesso: " + deletedUser.getName());

                boolean stillExists = userRepository.existsById(savedUser.getId());
                if (!stillExists) {
                    System.out.println("Confirmado: Usuário foi removido do banco de dados");
                } else {
                    System.out.println("Erro: Usuário ainda existe no banco de dados");
                }
            } else {
                System.out.println("Erro ao deletar usuário");
            }
        } else {
            System.out.println("Usuário não encontrado para deletar");
        }
        System.out.println();

        System.out.println("Testando delete de usuário inexistente");
        try {
            User nonExistentUser = new User();
            nonExistentUser.setId(999L);
            nonExistentUser.setName("Usuário Inexistente");
            nonExistentUser.setAge(30);
            nonExistentUser.setGender(UserGender.MALE);

            userRepository.delete(nonExistentUser);
        } catch (Exception e) {
            System.out.println("Erro esperado ao tentar deletar usuário inexistente: " + e.getMessage());
        }
        System.out.println();
    }
}
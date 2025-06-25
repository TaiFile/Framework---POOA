package br.ufscar.pooa.Framework___POOA;

import br.ufscar.pooa.Framework___POOA.Enum.UserGender;
import br.ufscar.pooa.Framework___POOA.persistence_framework.IFrameworkRepository;
import br.ufscar.pooa.Framework___POOA.persistence_framework.database.DatabaseManager;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws SQLException {
        // 1. Configuração do Banco de Dados e Repositório
        System.out.println("--- Iniciando a demonstração ---");
        DatabaseManager databaseManager = DatabaseManager.getInstance(
                "jdbc:postgresql://localhost:5432/framework-db", // Sua URL
                "root", // Seu usuário
                "root"  // Sua senha
        );

        FrameworkRepositoryFactory<User, Long> factory = new FrameworkRepositoryFactory<>(databaseManager);
        UserRepository userRepository = new UserRepository(factory.getRepository(User.class));
        System.out.println("Configuração concluída.\n");

        // 2. CREATE: Salvando um novo usuário
        System.out.println("--- 1. CRIANDO USUÁRIO ---");
        User user = new User();
        user.setName("Carlos");
        user.setAge(45);
        user.setGender(UserGender.MALE);

        User savedUser = userRepository.save(user);
        System.out.println("Usuário salvo no banco: " + savedUser + "\n");

        // 3. READ: Buscando o usuário para confirmar que ele existe
        System.out.println("--- 2. LENDO USUÁRIO ---");
        Long userId = savedUser.getId();
        Optional<User> foundUser = userRepository.findById(userId);
        foundUser.ifPresent(u -> System.out.println("Usuário encontrado por ID: " + u));

        // Demonstração rápida do findAll e existsBy
        List<User> allUsers = userRepository.findAll();
        System.out.println("Total de usuários no banco agora: " + allUsers.size());
        System.out.println("Existe um usuário com o nome 'Carlos'? " + userRepository.existsBy("name", "Carlos") + "\n");

        // 4. UPDATE: Atualizando os dados do usuário
        System.out.println("--- 3. ATUALIZANDO USUÁRIO ---");
        if (foundUser.isPresent()) {
            User userToUpdate = foundUser.get();
            System.out.println("Dados antes da atualização: " + userToUpdate);

            userToUpdate.setName("Carlos Alberto");
            userToUpdate.setAge(46);
            User updatedUser = userRepository.update(userToUpdate);

            System.out.println("Dados depois da atualização: " + updatedUser + "\n");
        }

        // 5. DELETE: Removendo o usuário do banco de dados
        System.out.println("--- 4. DELETANDO USUÁRIO ---");
        userRepository.delete(savedUser);
        System.out.println("Usuário com ID " + userId + " foi deletado.\n");

        // 6. VERIFICAÇÃO FINAL: Confirmando que o usuário não existe mais
        System.out.println("--- 5. VERIFICAÇÃO FINAL ---");
        boolean stillExists = userRepository.existsById(userId);
        System.out.println("O usuário com ID " + userId + " ainda existe? " + stillExists);

        System.out.println("Usuários restantes no banco: " + userRepository.findAll().size());
        System.out.println("\n--- Fim da demonstração ---");
    }
}
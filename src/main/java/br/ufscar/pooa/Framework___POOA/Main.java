package br.ufscar.pooa.Framework___POOA;


import br.ufscar.pooa.Framework___POOA.framework.DatabaseManager;
import br.ufscar.pooa.Framework___POOA.framework.PersistenceFramework;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseManager databaseManager = new DatabaseManager(
                "jdbc:postgresql://localhost:5432/framework-db",
                "root",
                "root");

        PersistenceFramework framework = new PersistenceFramework(databaseManager);

        User user = new User();
        user.setName("Vitor");
        user.setAge(23);

        framework.insert(user);
        User foundUser = framework.findById(User.class, 1L);
        if (foundUser != null) {
            System.out.println("Usuário encontrado: " + foundUser.getName());
        } else {
            System.out.println("Usuário não encontrado");
        }
        List<User> allUsers = framework.findAll(User.class);

        if (!allUsers.isEmpty()) {
            System.out.println("Usuários encontrados:");
            for (User users : allUsers) {
                System.out.println("ID: " + users.getId() + ", Nome: " + users.getName() + ", Idade: " + users.getAge());
            }
        } else {
            System.out.println("Nenhum usuário encontrado");
        }
        boolean userExists = framework.exists(User.class, 1L);

        if (userExists) {
            System.out.println("Usuário com ID 1 existe no banco de dados");
        } else {
            System.out.println("Usuário com ID 1 não existe no banco de dados");
        }

        Long userId = 5L;
        if (framework.exists(User.class, userId)) {
            User users = framework.findById(User.class, userId);
            System.out.println("Usuário encontrado: " + users.getName());
        } else {
            System.out.println("Usuário com ID " + userId + " não existe");
        }
    }
}
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
        user.setName("Eduardo");
        user.setAge(25);

        framework.insert(user);
        // Exemplo de uso no Main.java
        User foundUser = framework.findById(User.class, 1L);
        if (foundUser != null) {
            System.out.println("Usuário encontrado: " + foundUser.getName());
        } else {
            System.out.println("Usuário não encontrado");
        }
        // Exemplo de uso no Main.java
        List<User> allUsers = framework.findAll(User.class);

        if (!allUsers.isEmpty()) {
            System.out.println("Usuários encontrados:");
            for (User users : allUsers) {
                System.out.println("ID: " + users.getId() + ", Nome: " + users.getName() + ", Idade: " + users.getAge());
            }
        } else {
            System.out.println("Nenhum usuário encontrado");
        }
    }
}
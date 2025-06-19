package br.ufscar.pooa.Framework___POOA;


import br.ufscar.pooa.Framework___POOA.framework.DatabaseManager;
import br.ufscar.pooa.Framework___POOA.framework.PersistenceFramework;

import java.sql.SQLException;

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
    }
}
package br.ufscar.pooa.Framework___POOA.persistence_framework.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Lida com a execução de baixo nível de instruções JDBC.
 * Esta classe encapsula o código repetitivo (boilerplate) para criar PreparedStatements,
 * definir parâmetros, executar consultas/atualizações e gerenciar recursos.
 */
public class JDBCExecutor {

    private final DatabaseManager databaseManager;

    public JDBCExecutor(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Uma interface funcional para extrair dados de um ResultSet.
     * @param <R> O tipo do objeto de resultado.
     */
    @FunctionalInterface
    public interface ResultSetExtractor<R> {
        R extractData(ResultSet rs) throws Exception;
    }

    /**
     * Executa uma consulta de modificação (UPDATE, DELETE).
     */
    public int executeModification(String sql, Object... params) {
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha na modificação do banco de dados para o SQL: " + sql, e);
        }
    }

    /**
     * Executa uma consulta de seleção (select) e processa o ResultSet usando o extrator fornecido.
     */
    public <R> R executeQuery(String sql, ResultSetExtractor<R> extractor, Object... params) {
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return extractor.extractData(resultSet);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha na consulta ao banco de dados para o SQL: " + sql, e);
        }
    }

    /**
     * Executa uma instrução INSERT e retorna o ResultSet contendo as chaves geradas.
     * O chamador é responsável por fechar o ResultSet retornado.
     */
    public ResultSet executeInsertAndReturnGeneratedKeys(String sql, Object... params) {
        try {
            PreparedStatement statement = databaseManager.getConnection()
                    .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                statement.close();
                throw new SQLException("A criação da entidade falhou, nenhuma linha foi afetada.");
            }

            return statement.getGeneratedKeys();

        } catch (SQLException e) {
            throw new RuntimeException("A operação de inserção falhou para o SQL: " + sql, e);
        }
    }
}
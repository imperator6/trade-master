package tradingmaster.telegram.database;

import org.telegram.bot.services.BotLogger;

import java.sql.*;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief TODO
 * @date 16 of October of 2016
 */
public class ConnectionDB {
    private static final String LOGTAG = "CONNECTIONDB";
    private Connection currentConection;

    public ConnectionDB() {
        this.currentConection = openConexion();
    }

    private Connection openConexion() {
        Connection connection = null;
        try {
            Class.forName(DatabaseConstants.controllerDB).newInstance();
            connection = DriverManager.getConnection(DatabaseConstants.linkDB, DatabaseConstants.userDB, DatabaseConstants.password);
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            BotLogger.error(LOGTAG, e);
        }

        return connection;
    }

    public void closeConexion() {
        try {
            this.currentConection.close();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }

    }

    public ResultSet runSqlQuery(String query) throws SQLException {
        final Statement statement;
        statement = this.currentConection.createStatement();
        return statement.executeQuery(query);
    }

    public Boolean executeQuery(String query) throws SQLException {
        final Statement statement = this.currentConection.createStatement();
        return statement.execute(query);
    }

    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        return this.currentConection.prepareStatement(query);
    }

    public PreparedStatement getPreparedStatement(String query, int flags) throws SQLException {
        return this.currentConection.prepareStatement(query, flags);
    }

    public int checkVersion() {
        int max = 0;
        try {
            final DatabaseMetaData metaData = this.currentConection.getMetaData();
            final ResultSet res = metaData.getTables(null, null, "",
                    new String[]{"TABLE"});
            while (res.next()) {
                if (res.getString("TABLE_NAME").compareTo("Versions") == 0) {
                    final ResultSet result = runSqlQuery("SELECT Max(Version) FROM Versions");
                    while (result.next()) {
                        max = (max > result.getInt(1)) ? max : result.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }
        return max;
    }

    /**
     * Initilize a transaction in database
     * @throws SQLException If initialization fails
     */
    public void initTransaction() throws SQLException {
        this.currentConection.setAutoCommit(false);
    }

    /**
     * Finish a transaction in database and commit changes
     * @throws SQLException If a rollback fails
     */
    public void commitTransaction() throws SQLException {
        try {
            this.currentConection.commit();
        } catch (SQLException e) {
            if (this.currentConection != null) {
                this.currentConection.rollback();
            }
        } finally {
            this.currentConection.setAutoCommit(false);
        }
    }
}

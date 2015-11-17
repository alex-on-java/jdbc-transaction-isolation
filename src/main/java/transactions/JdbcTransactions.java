package transactions;

import java.sql.*;

/**
 */
public class JdbcTransactions {
    public static final String DB_NAME = "trantest";
    public static final String URL = "jdbc:postgresql://localhost:5432/"+ DB_NAME;
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgrespass";

    public static void main(String[] args) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        statement.execute("DROP TABLE IF EXISTS data;");
        statement.execute("CREATE TABLE data (id int primary key, content varchar(255));");
        statement.execute("INSERT INTO data (id, content)  VALUES(1, 'base');");

        new Thread(() -> changeData("t1", 10, 2000, Connection.TRANSACTION_READ_COMMITTED)).start();
        new Thread(() -> changeData("t2", 1000, 10, Connection.TRANSACTION_READ_COMMITTED)).start();
        sleep(3000);
        System.out.println("\nAfter all, content is: " + loadContent(connection));
        // with READ_COMMITTED result is "base+t2", so the update of first transaction is lost
        // with REPEATABLE_READ and SERIALIZABLE there is an exception in second transaction on commit attempt
    }

    public static void changeData(String transactionName, long sleepBefore, long sleepAfter, int isolation) {
        try {
            sleep(sleepBefore);
            Connection connection = createConnection();
            connection.setTransactionIsolation(isolation);
            connection.setAutoCommit(false);

            String content = loadContent(connection);
            System.out.println(transactionName + " content loaded: " + content);
            PreparedStatement ps = connection.prepareStatement("UPDATE data SET content = ? WHERE id = 1");
            String newContent = content + "+" + transactionName;
            ps.setString(1, newContent);
            ps.executeUpdate();
            sleep(sleepAfter);

            System.out.println("Trying to commit: " + newContent);
            connection.commit();
            System.out.println("Commited: " + newContent);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadContent(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT content FROM data WHERE id = 1");
        rs.next();
        return rs.getString(1);
    }

    public static Connection createConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleep(long sleepBefore) {
        try {
            Thread.sleep(sleepBefore);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

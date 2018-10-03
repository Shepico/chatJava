package gb.j2.chat.server.core;

import java.sql.*;

public class SqlClient {

    private static Connection connection = null;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:chatDB.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

   synchronized static String getNickname(String login, String password) {
        String request = String.format("select nickname from users where login='%s' and password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(request)) {
            if (set.next()) {
                return set.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /////////// Смена НИКа ////////////
    synchronized static boolean changeNickname (String login, String password, String newNickname) {

        String request = String.format("select nickname from users where nickname='%s'", newNickname);
        String requestUpdate = String.format("UPDATE users SET nickname='%s' where login='%s' and password='%s'",newNickname,login,password);
        // проверка уникальности ника
        try {
            ResultSet set = statement.executeQuery(request); //проверим ник на совпадение
            set.last();
            int size = set.getRow();

            if (size != 0) {
                return false;
            }

            statement.executeUpdate(requestUpdate);
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

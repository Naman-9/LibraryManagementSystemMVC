
package library.config;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection != null && connection.isValid(2)) {
                System.out.println("MySQL connection successful!");
                System.out.println("Database: " +
                        connection.getCatalog());
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}

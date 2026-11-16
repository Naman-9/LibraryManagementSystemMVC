
package library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/library_management";

    private static final String USER = "root";

    // Read the password from an environment variable
    private static final String PASSWORD =
            System.getenv("LIBRARY_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException(
                    "LIBRARY_DB_PASSWORD environment variable is not set."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void shutdown() {
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread
                .checkedShutdown();
    }
}

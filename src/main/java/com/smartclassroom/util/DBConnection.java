package com.smartclassroom.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton utility class for obtaining JDBC connections to the MySQL database.
 * Database credentials are read from {@code db.properties} on the classpath.
 *
 * <p>Expected db.properties keys:
 * <ul>
 *   <li>{@code db.driver}   – JDBC driver class (e.g. com.mysql.cj.jdbc.Driver)</li>
 *   <li>{@code db.url}      – JDBC URL (e.g. jdbc:mysql://localhost:3306/smartclassroom)</li>
 *   <li>{@code db.username} – Database username</li>
 *   <li>{@code db.password} – Database password</li>
 * </ul>
 */
public class DBConnection {

    private static final Properties props = new Properties();

    static {
        try (InputStream is = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("db.properties not found in classpath");
            }
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("DBConnection init failed: " + e.getMessage(), e);
        }
    }

    /**
     * Private constructor – this class must not be instantiated.
     */
    private DBConnection() {
    }

    /**
     * Opens and returns a new JDBC {@link Connection} using the properties
     * loaded from {@code db.properties}. Callers are responsible for closing
     * the connection (use try-with-resources).
     *
     * @return a new {@link Connection} to the configured database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
        );
    }

    /**
     * Tests whether a database connection can be established successfully.
     * Useful for startup health checks and configuration validation.
     *
     * @return {@code true} if the connection can be opened and closed without error;
     *         {@code false} otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("DBConnection.testConnection() failed: " + e.getMessage());
            return false;
        }
    }
}

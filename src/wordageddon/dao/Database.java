package wordageddon.dao;

import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

/**
 * Database utility class for managing SQLite database connections and initialization.
 */
public class Database {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    private static final String SCHEMA_FILE = "/resources/database.sql";
    
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }
    
    /**
     * Gets a database connection.
     * 
     * @return a database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Initializes the database by creating tables if they don't exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates the database tables by executing the SQL schema file.
     * 
     * @param conn the database connection
     * @throws SQLException if table creation fails
     */
    private static void createTables(Connection conn) throws SQLException {
        try (InputStream is = Database.class.getResourceAsStream(SCHEMA_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                throw new SQLException("Cannot find schema file: " + SCHEMA_FILE);
            }
            
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    sql.append(line).append(" ");
                    if (line.endsWith(";")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sql.toString());
                        }
                        sql.setLength(0);
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Failed to read schema file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Closes a database connection safely.
     * 
     * @param conn the connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // registra l'errore ma non lancia eccezioni
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes a PreparedStatement safely.
     * 
     * @param stmt the statement to close
     */
    public static void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                // registra l'errore ma non lancia eccezioni
                System.err.println("Error closing statement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes a ResultSet safely.
     * 
     * @param rs the result set to close
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // registra l'errore ma non lancia eccezioni
                System.err.println("Error closing result set: " + e.getMessage());
            }
        }
    }
}

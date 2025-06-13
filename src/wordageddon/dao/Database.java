package wordageddon.dao;

import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import wordageddon.util.PasswordUtils;

/**
 * Database utility class for managing SQLite database connections and initialization.
 * 
 * This singleton utility provides centralized database connection management,
 * automatic schema initialization, and connection pooling for the Wordageddon application.
 * It handles SQLite JDBC driver loading and provides methods for database setup.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class Database {
    
    /** Database connection URL for SQLite database file */
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /** Path to the SQL schema file containing table definitions */
    private static final String SCHEMA_FILE = "/resources/database.sql";
    
    // carica il driver jdbc per sqlite all'avvio della classe
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }
    
    /**
     * Gets a new database connection to the SQLite database.
     * 
     * // crea una nuova connessione al database sqlite
     * 
     * @return a new database connection instance
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Initializes the database by creating all required tables if they don't exist.
     * Also inserts default users if the database is being created for the first time.
     * 
     * @throws RuntimeException if database initialization fails
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Check if database tables exist
            boolean tablesExist = doTablesExist(conn);
            
            // Create tables if they don't exist
            if (!tablesExist) {
                createTables(conn);
                // Insert default users only when creating tables for the first time
                insertDefaultUsers(conn);
            }
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
     * Checks if the required database tables exist.
     * 
     * @param conn the database connection
     * @return true if tables exist, false otherwise
     * @throws SQLException if checking fails
     */
    private static boolean doTablesExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'")) {
            
            return rs.next(); // Returns true if users table exists
        } catch (SQLException e) {
            // If we can't check, assume tables don't exist
            return false;
        }
    }
    
    /**
     * Inserts the default users into the database.
     * Creates admin@wordageddon.it (admin123) and demo@wordageddon.it (demo123).
     * 
     * @param conn the database connection
     * @throws SQLException if insertion fails
     */
    private static void insertDefaultUsers(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO users (username, password, first_name, last_name, email, is_admin) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            // Insert admin user
            pstmt.setString(1, "admin");
            pstmt.setString(2, PasswordUtils.hashPassword("admin123"));
            pstmt.setString(3, "Admin");
            pstmt.setString(4, "User");
            pstmt.setString(5, "admin@wordageddon.it");
            pstmt.setInt(6, 1); // is_admin = true
            pstmt.executeUpdate();
            
            // Insert demo user
            pstmt.setString(1, "demo");
            pstmt.setString(2, PasswordUtils.hashPassword("demo123"));
            pstmt.setString(3, "Demo");
            pstmt.setString(4, "User");
            pstmt.setString(5, "demo@wordageddon.it");
            pstmt.setInt(6, 0); // is_admin = false
            pstmt.executeUpdate();
            
            System.out.println("Default users created successfully:");
            System.out.println("- admin@wordageddon.it (password: admin123)");
            System.out.println("- demo@wordageddon.it (password: demo123)");
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

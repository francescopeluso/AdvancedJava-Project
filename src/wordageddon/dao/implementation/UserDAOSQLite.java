package wordageddon.dao.implementation;

import wordageddon.dao.UserDAO;
import wordageddon.dao.Database;
import wordageddon.model.User;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * SQLite implementation of the UserDAO interface for user data persistence.
 * 
 * This class provides concrete implementation of all user-related database operations
 * using SQLite as the underlying database. It handles:
 * - User registration and authentication
 * - User information retrieval and validation
 * - Administrative user management
 * - Database connection management and error handling
 * 
 * All methods use prepared statements to prevent SQL injection attacks
 * and proper resource management with try-with-resources blocks.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class UserDAOSQLite implements UserDAO {

    @Override
    public void addUser(String username, String fname, String lname, String password, String email, Boolean isAdmin) {
        String sql = "INSERT INTO users (username, password, first_name, last_name, email, is_admin) VALUES (?, ?, ?, ?, ?, ?)";
        
        // utilizza prepared statement per sicurezza contro sql injection
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password); // password già hashata dal service
            pstmt.setString(3, fname);
            pstmt.setString(4, lname);
            pstmt.setString(5, email);
            pstmt.setBoolean(6, isAdmin != null ? isAdmin : false); // default false se null
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking user existence: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUser(String username) {
        String sql = "SELECT id, username, password, first_name, last_name, email, is_admin FROM users WHERE username = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT id, username, password, first_name, last_name, email, is_admin FROM users WHERE email = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getBoolean("is_admin")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by email: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void updateUser(String username, String fname, String lname, String password, String email, Boolean isAdmin) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, password = ?, email = ?, is_admin = ? WHERE username = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fname);
            pstmt.setString(2, lname);
            pstmt.setString(3, password);
            pstmt.setString(4, email);
            pstmt.setBoolean(5, isAdmin != null ? isAdmin : false);
            pstmt.setString(6, username);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, first_name, last_name, email, is_admin FROM users ORDER BY username";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getBoolean("is_admin")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all users: " + e.getMessage(), e);
        }
        return users;
    }

    @Override
    public void updateUserAdminStatus(int userId, boolean isAdmin) {
        String sql = "UPDATE users SET is_admin = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isAdmin);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No user found with ID: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user admin status: " + e.getMessage(), e);
        }
    }
}
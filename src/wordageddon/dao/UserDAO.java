package wordageddon.dao;

import wordageddon.model.User;
import java.util.List;

/**
 * Data Access Object interface for managing user data in the database.
 * 
 * This interface defines the contract for user data persistence operations
 * including authentication, user registration, retrieval, and administrative
 * functions. Implementations handle the actual database interactions.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public interface UserDAO {

    /**
     * Adds a new user to the database.
     * 
     * // registra un nuovo utente nel sistema
     *
     * @param username the unique username for authentication
     * @param fname the user's first name  
     * @param lname the user's last name
     * @param password the user's hashed password
     * @param email the user's email address
     * @param isAdmin whether the user has administrative privileges
     * @throws RuntimeException if the user already exists or database error occurs
     */
    void addUser(String username, String fname, String lname, String password, String email, Boolean isAdmin);

    /**
     * Checks if a user exists in the database by username.
     * 
     * // verifica se l'username è già presente nel database
     *
     * @param username the username to check for existence
     * @return true if a user with this username exists, false otherwise
     */
    boolean userExists(String username);

    /**
     * Retrieves a user from the database by username.
     * 
     * // cerca un utente specifico tramite username
     *
     * @param username the username of the user to retrieve
     * @return a User object if found, null if no user exists with this username
     */
    User getUser(String username);

    /**
     * Retrieves a user from the database by email address.
     * 
     * // cerca un utente specifico tramite indirizzo email
     *
     * @param email the email address of the user to retrieve
     * @return a User object if found, null if no user exists with this email
     */
    User getUserByEmail(String email);

    /**
     * Retrieves all users from the database.
     * 
     * // ottiene la lista completa di tutti gli utenti registrati
     *
     * @return a List containing all User objects in the database, empty list if no users exist
     */
    List<User> getAllUsers();

    /**
     * Updates the details of an existing user in the database.
     *
     * @param username the username of the user to update
     * @param fname the new first name of the user
     * @param lname the new last name of the user
     * @param password the new password of the user
     * @param email the new email of the user
     * @param isAdmin whether the user is an admin
     */
    void updateUser(String username, String fname, String lname, String password, String email, Boolean isAdmin);

    /**
     * Updates the admin status of a user.
     *
     * @param userId the ID of the user to update
     * @param isAdmin the new admin status
     */
    void updateUserAdminStatus(int userId, boolean isAdmin);

    /**
     * Deletes a user from the database.
     *
     * @param username the username of the user to delete
     */
    void deleteUser(String username);

}

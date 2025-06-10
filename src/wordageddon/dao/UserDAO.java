package wordageddon.dao;

import wordageddon.model.User;
import java.util.List;

/**
 * UserDAO interface for managing user data in the database.
 * Provides basic CRUD operations for User entities.
 */
public interface UserDAO {

    /**
     * Adds a new user to the database.
     *
     * @param username the username of the user
     * @param fname the first name of the user
     * @param lname the last name of the user
     * @param password the password of the user
     * @param email the email of the user
     * @param isAdmin whether the user is an admin
     */
    void addUser(String username, String fname, String lname, String password, String email, Boolean isAdmin);

    /**
     * Checks if a user exists in the database.
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    boolean userExists(String username);

    /**
     * Retrieves a user from the database.
     *
     * @param username the username of the user to retrieve
     * @return a User object if found, null otherwise
     */
    User getUser(String username);

    /**
     * Retrieves a user from the database by email.
     *
     * @param email the email of the user to retrieve
     * @return a User object if found, null otherwise
     */
    User getUserByEmail(String email);

    /**
     * Retrieves all users from the database.
     *
     * @return a List of all User objects
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

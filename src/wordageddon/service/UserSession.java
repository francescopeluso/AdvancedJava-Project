package wordageddon.service;

import wordageddon.model.User;

/**
 * Singleton class for managing the current user session.
 * Tracks the currently logged-in user throughout the application.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class UserSession {
    
    private static UserSession instance;
    private User currentUser;
    private long loginTime;
    
    private UserSession() {
        // Private constructor for singleton
    }
    
    /**
     * Gets the singleton instance of UserSession.
     * 
     * @return the UserSession instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    /**
     * Sets the current logged-in user.
     * 
     * @param user the user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.loginTime = System.currentTimeMillis();
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return the current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Gets the current user's ID.
     * 
     * @return the current user's ID, or -1 if no user is logged in
     */
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
    
    /**
     * Gets the current user's username.
     * 
     * @return the current user's username, or null if no user is logged in
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Checks if the current user is an admin.
     * 
     * @return true if current user is admin, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.getIsAdmin();
    }
    
    /**
     * Gets the time when the current user logged in.
     * 
     * @return the login timestamp in milliseconds
     */
    public long getLoginTime() {
        return loginTime;
    }
    
    /**
     * Logs out the current user by clearing the session.
     */
    public void logout() {
        this.currentUser = null;
        this.loginTime = 0;
    }
    
    /**
     * Gets the session duration in milliseconds.
     * 
     * @return the time since login, or 0 if no user is logged in
     */
    public long getSessionDuration() {
        return isLoggedIn() ? System.currentTimeMillis() - loginTime : 0;
    }
}

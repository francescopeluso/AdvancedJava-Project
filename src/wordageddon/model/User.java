package wordageddon.model;

/**
 * Represents a user in the Wordageddon application.
 * This class contains user information including authentication details,
 * personal information, and administrative privileges.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class User {

    /** The unique identifier for the user */
    private int id;
    
    /** The unique username for authentication */
    private String username;
    
    /** The user's first name */
    private String fname;
    
    /** The user's last name */
    private String lname;
    
    /** The user's hashed password */
    private String password;
    
    /** The user's email address */
    private String email;
    
    /** Whether the user has administrative privileges */
    private Boolean isAdmin;

    /**
     * Constructor with ID for creating a User object from database retrieval.
     * 
     * @param id the unique identifier for the user
     * @param username the unique username for authentication
     * @param fname the user's first name
     * @param lname the user's last name
     * @param password the user's hashed password
     * @param email the user's email address
     * @param isAdmin whether the user has administrative privileges
     */
    public User(int id, String username, String fname, String lname, String password, String email, Boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    /**
     * Constructor without ID for creating a new User object before database insertion.
     * The ID will be automatically assigned by the database.
     * 
     * @param username the unique username for authentication
     * @param fname the user's first name
     * @param lname the user's last name
     * @param password the user's hashed password
     * @param email the user's email address
     * @param isAdmin whether the user has administrative privileges
     */
    public User(String username, String fname, String lname, String password, String email, Boolean isAdmin) {
        this.id = -1; // sarà impostato dal database
        this.username = username;
        this.fname = fname;
        this.lname = lname;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
    }

    // Getter methods
    
    /**
     * Gets the user's unique identifier.
     * 
     * @return the user ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets the user's username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the user's first name.
     * 
     * @return the first name
     */
    public String getFname() {
        return fname;
    }

    /**
     * Gets the user's last name.
     * 
     * @return the last name
     */
    public String getLname() {
        return lname;
    }

    /**
     * Gets the user's hashed password.
     * 
     * @return the hashed password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's email address.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets whether the user has administrative privileges.
     * 
     * @return true if the user is an admin, false otherwise, null if not set
     */
    public Boolean getIsAdmin() {
        return isAdmin;
    }

    // Setter methods
    
    /**
     * Sets the user's unique identifier.
     * 
     * @param id the user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the user's username.
     * 
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the user's first name.
     * 
     * @param fname the first name to set
     */
    public void setFname(String fname) {
        this.fname = fname;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lname the last name to set
     */
    public void setLname(String lname) {
        this.lname = lname;
    }

    /**
     * Sets the user's hashed password.
     * 
     * @param password the hashed password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets whether the user has administrative privileges.
     * 
     * @param isAdmin true if the user should be an admin, false otherwise
     */
    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Returns a string representation of the user.
     * Includes user type (admin or regular user), username, full name, and email.
     * 
     * @return a formatted string containing user information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.isAdmin ? "Admin: " : "Utente: ");

        sb.append(this.username)
            .append(" (")
            .append(this.fname + " " + this.lname)
            .append(") - Email: ")
            .append(this.email);

        return sb.toString();
    }

}

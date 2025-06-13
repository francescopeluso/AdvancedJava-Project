package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import wordageddon.dao.DAOFactory;
import wordageddon.dao.UserDAO;
import wordageddon.model.User;
import wordageddon.util.PasswordUtils;

/**
 * JavaFX Service for handling user authentication in the background.
 * This service manages login and registration operations without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class AuthenticationService extends Service<AuthenticationService.AuthenticationResult> {

    private final UserDAO userDAO;

    public enum AuthenticationType {
        LOGIN, REGISTRATION
    }

    /**
     * Result container for authentication operation.
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String errorMessage;
        private final String userId;
        private final AuthenticationType type;

        public AuthenticationResult(boolean success, String errorMessage, String userId, AuthenticationType type) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.userId = userId;
            this.type = type;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getUserId() { return userId; }
        public AuthenticationType getType() { return type; }
    }

    /**
     * User credentials container.
     */
    public static class UserCredentials {
        private final String email;
        private final String password;
        private final String firstName;
        private final String lastName;
        private final String username;

        // costruttore per il login
        public UserCredentials(String email, String password) {
            this.email = email;
            this.password = password;
            this.firstName = null;
            this.lastName = null;
            this.username = null;
        }

        // costruttore per la registrazione
        public UserCredentials(String email, String password, String firstName, String lastName, String username) {
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getUsername() { return username; }
    }

    private final AuthenticationType authenticationType;
    private final UserCredentials credentials;

    /**
     * Constructs a new AuthenticationService.
     * 
     * @param authenticationType the type of authentication (LOGIN or REGISTRATION)
     * @param credentials the user credentials
     */
    public AuthenticationService(AuthenticationType authenticationType, UserCredentials credentials) {
        this.authenticationType = authenticationType;
        this.credentials = credentials;
        this.userDAO = DAOFactory.getUserDAO();
    }

    @Override
    protected Task<AuthenticationResult> createTask() {
        return new Task<AuthenticationResult>() {
            @Override
            protected AuthenticationResult call() throws Exception {
                if (authenticationType == AuthenticationType.LOGIN) {
                    return performLogin();
                } else {
                    return performRegistration();
                }
            }

            private AuthenticationResult performLogin() throws InterruptedException {
                updateMessage("Verifica credenziali...");
                updateProgress(0, 100);

                // simula il ritardo di rete per la query del database
                Thread.sleep(1000);
                updateProgress(50, 100);

                updateMessage("Accesso in corso...");
                
                // autentica l'utente con il database
                User user = authenticateUser(credentials.getEmail(), credentials.getPassword());
                
                boolean loginSuccess = user != null;

                Thread.sleep(500);
                updateProgress(100, 100);

                if (loginSuccess) {
                    updateMessage("Login completato!");
                    return new AuthenticationResult(true, null, credentials.getEmail(), AuthenticationType.LOGIN);
                } else {
                    return new AuthenticationResult(false, "Credenziali non valide", null, AuthenticationType.LOGIN);
                }
            }

            private AuthenticationResult performRegistration() throws InterruptedException {
                updateMessage("Validazione dati...");
                updateProgress(0, 100);

                // valida i dati di registrazione
                if (!validateEmailFormat(credentials.getEmail())) {
                    return new AuthenticationResult(false, "Formato email non valido", null, AuthenticationType.REGISTRATION);
                }

                if (credentials.getFirstName() == null || credentials.getFirstName().trim().isEmpty()) {
                    return new AuthenticationResult(false, "Nome obbligatorio", null, AuthenticationType.REGISTRATION);
                }

                updateProgress(25, 100);
                Thread.sleep(500);

                updateMessage("Verifica disponibilità email...");
                updateProgress(50, 100);

                // controlla se l'username è disponibile
                if (!isUsernameAvailable(credentials.getUsername())) {
                    return new AuthenticationResult(false, "Username già in uso", null, AuthenticationType.REGISTRATION);
                }
                
                Thread.sleep(1000);

                updateMessage("Creazione account...");
                updateProgress(75, 100);

                // Create user in database
                boolean userCreated = registerUser(
                    credentials.getUsername(),
                    credentials.getFirstName(),
                    credentials.getLastName(),
                    credentials.getPassword(),
                    credentials.getEmail(),
                    false // regular user, not admin
                );
                
                Thread.sleep(500);

                updateProgress(100, 100);
                
                if (userCreated) {
                    updateMessage("Registrazione completata!");
                    return new AuthenticationResult(true, null, credentials.getEmail(), AuthenticationType.REGISTRATION);
                } else {
                    return new AuthenticationResult(false, "Errore durante la registrazione", null, AuthenticationType.REGISTRATION);
                }
            }

            private boolean validateEmailFormat(String email) {
                String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
                return email != null && email.matches(emailRegex);
            }
        };
    }
    
    /**
     * Authenticates a user login using email and password.
     * 
     * @param email the user's email address for login
     * @param password the plaintext password to verify
     * @return the User object if authentication is successful, null otherwise
     */
    private User authenticateUser(String email, String password) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user == null) {
                return null; // utente non trovato
            }
            
            String storedPassword = user.getPassword();
            
            // verifica la password usando l'hash memorizzato nel database
            boolean isAuthenticated = PasswordUtils.verifyPassword(password, storedPassword);
            
            if (isAuthenticated) {
                // Imposta la sessione utente
                UserSession.getInstance().setCurrentUser(user);
                return user;
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Registers a new user.
     * 
     * @param username the username
     * @param firstName the first name
     * @param lastName the last name
     * @param password the password
     * @param email the email
     * @param isAdmin whether the user is an admin
     * @return true if registration is successful, false if user already exists
     */
    private boolean registerUser(String username, String firstName, String lastName, String password, String email, boolean isAdmin) {
        try {
            if (userDAO.userExists(username)) {
                return false;
            }
            
            // Crea un hash sicuro della password prima di memorizzarla
            String hashedPassword = PasswordUtils.hashPassword(password);
            
            userDAO.addUser(username, firstName, lastName, hashedPassword, email, isAdmin);
            return true;
        } catch (Exception e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a username is available.
     * 
     * @param username the username to check
     * @return true if available, false if already taken
     */
    private boolean isUsernameAvailable(String username) {
        try {
            return !userDAO.userExists(username);
        } catch (Exception e) {
            System.err.println("Error checking username availability: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String toString() {
        return "AuthenticationService{" +
                "authenticationType=" + authenticationType +
                ", credentials=" + credentials +
                '}';
    }
}

package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * JavaFX Service for handling user authentication in the background.
 * This service manages login and registration operations without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class AuthenticationService extends Service<AuthenticationService.AuthenticationResult> {

    private final GameIntegrationService gameIntegrationService;

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
        this.gameIntegrationService = new GameIntegrationService();
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
                wordageddon.model.User user = gameIntegrationService.authenticateUser(
                    credentials.getEmail(), 
                    credentials.getPassword()
                );
                
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
                if (!gameIntegrationService.isUsernameAvailable(credentials.getUsername())) {
                    return new AuthenticationResult(false, "Username già in uso", null, AuthenticationType.REGISTRATION);
                }
                
                Thread.sleep(1000);

                updateMessage("Creazione account...");
                updateProgress(75, 100);

                // Create user in database
                boolean userCreated = gameIntegrationService.registerUser(
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
}

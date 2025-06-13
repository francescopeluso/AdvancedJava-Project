package wordageddon.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import wordageddon.service.AuthenticationService;
import wordageddon.service.SceneNavigationService;

/**
 * Controller for the start screen managing user authentication in Wordageddon.
 * 
 * This controller handles the initial user interface for the application,
 * providing both login and registration functionality. It manages:
 * - User login with username/password validation
 * - New user registration with input validation
 * - Form switching between login and registration modes
 * - Authentication state management and error handling
 * - Navigation to the main dashboard upon successful authentication
 * 
 * The controller uses asynchronous authentication services to prevent UI blocking
 * and provides real-time feedback to users during the authentication process.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class StartViewController implements Initializable {

    // componenti del form di login
    /** Login button */
    @FXML
    private Button loginButton;
    /** Login form container */
    @FXML
    private VBox loginPane;
    /** Email input field */
    @FXML
    private TextField loginEmailField;
    /** Password input field */
    @FXML
    private PasswordField loginPasswordField;
    /** Login error message label */
    @FXML
    private Label loginErrorLabel;
    
    // componenti del form di registrazione
    /** Registration form container */
    @FXML
    private VBox registerPane;
    /** First name input field */
    @FXML
    private TextField regFirstNameField;
    /** Last name input field */
    @FXML
    private TextField regLastNameField;
    /** Username input field */
    @FXML
    private TextField regUsernameField;
    /** Email input field */
    @FXML
    private TextField regEmailField;
    /** Password input field */
    @FXML
    private PasswordField regPasswordField;
    /** Password confirmation field */
    @FXML
    private PasswordField regConfirmPasswordField;
    /** Registration button */
    @FXML
    private Button registerButton;
    /** Registration error message label */
    @FXML
    private Label registerErrorLabel;

    // Services for asynchronous operations
    /** Service for user authentication */
    private AuthenticationService authenticationService;
    /** Service for scene navigation */
    private SceneNavigationService sceneNavigationService;

    /**
     * Initializes the controller.
     * 
     * @param url the location used to resolve relative paths
     * @param rb the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize UI components
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
    }

    /**
     * Handles login action with input validation using asynchronous authentication.
     * Validates email format and redirects to dashboard on success.
     * 
     * @param event the login button click event
     */
    @FXML
    private void handleLoginAction(ActionEvent event) {
        // prendo tutti i valori inseriti
        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText().trim();

        // se qualcuno tra i campi richiesti non è stato compilato, allora indico l'errore tramite la label
        if (email.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Inserisci email e password.");
            loginErrorLabel.setVisible(true);
            return;
        }

        // verifico correttezza del formato della mail inserita
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            loginErrorLabel.setText("Formato email non valido.");
            loginErrorLabel.setVisible(true);
            return;
        }

        // Disabilita il pulsante di login durante l'operazione
        loginButton.setDisable(true);
        loginErrorLabel.setVisible(false);

        // Crea e configura il service di autenticazione
        AuthenticationService.UserCredentials credentials = 
            new AuthenticationService.UserCredentials(email, password);
        authenticationService = new AuthenticationService(
            AuthenticationService.AuthenticationType.LOGIN, credentials);

        // Gestisce il completamento dell'autenticazione
        authenticationService.setOnSucceeded(authEvent -> {
            AuthenticationService.AuthenticationResult result = authenticationService.getValue();
            
            if (result.isSuccess()) {
                // Avvia la navigazione alla dashboard
                navigateToDashboard();
            } else {
                Platform.runLater(() -> {
                    loginErrorLabel.setText(result.getErrorMessage());
                    loginErrorLabel.setVisible(true);
                    loginButton.setDisable(false);
                });
            }
        });

        // Gestisce gli errori durante l'autenticazione
        authenticationService.setOnFailed(authEvent -> {
            Platform.runLater(() -> {
                Throwable exception = authenticationService.getException();
                loginErrorLabel.setText("Errore durante il login: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
                loginErrorLabel.setVisible(true);
                loginButton.setDisable(false);
            });
        });

        // Avvia il service di autenticazione
        authenticationService.start();
    }

    /**
     * Navigates to the dashboard using asynchronous scene loading.
     */
    private void navigateToDashboard() {
        Stage stage = (Stage) loginPane.getScene().getWindow();
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/DashboardView.fxml", 
            "Wordageddon - Dashboard", 
            stage);

        // Gestisce il completamento della navigazione
        sceneNavigationService.setOnSucceeded(navEvent -> {
            SceneNavigationService.NavigationResult result = sceneNavigationService.getValue();
            
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    sceneNavigationService.applySceneToStage(result);
                    stage.show();
                } else {
                    loginErrorLabel.setText(result.getErrorMessage());
                    loginErrorLabel.setVisible(true);
                }
                loginButton.setDisable(false);
            });
        });

        // Gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(navEvent -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                loginErrorLabel.setText("Errore nel caricamento della dashboard: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
                loginErrorLabel.setVisible(true);
                loginButton.setDisable(false);
            });
        });

        // Avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Switches from login view to registration view.
     * 
     * @param event the switch button click event
     */
    @FXML
    private void switchToRegister(ActionEvent event) {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }

    /**
     * Handles registration action with form validation using asynchronous authentication.
     * Validates all fields, email format, and password confirmation.
     * 
     * @param event the register button click event
     */
    @FXML
    private void handleRegisterAction(ActionEvent event) {

        // prendo tutti i valori inseriti
        String nome = regFirstNameField.getText().trim();
        String cognome = regLastNameField.getText().trim();
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();

        // se qualcuno tra i campi richiesti non è stato compilato, allora indico l'errore tramite la label
        if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            registerErrorLabel.setText("Compila tutti i campi.");
            registerErrorLabel.setVisible(true);
            return;
        }

        // verifico correttezza del formato della mail inserita
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            registerErrorLabel.setText("Formato email non valido.");
            registerErrorLabel.setVisible(true);
            return;
        }

        // conferma della password
        if (!password.equals(confirmPassword)) {
            registerErrorLabel.setText("Le password non corrispondono.");
            registerErrorLabel.setVisible(true);
            return;
        }

        // Disabilita il pulsante di registrazione durante l'operazione
        registerButton.setDisable(true);
        registerErrorLabel.setVisible(false);

        // Crea e configura il service di autenticazione per la registrazione
        AuthenticationService.UserCredentials credentials = 
            new AuthenticationService.UserCredentials(email, password, nome, cognome, username);
        authenticationService = new AuthenticationService(
            AuthenticationService.AuthenticationType.REGISTRATION, credentials);

        // Gestisce il completamento della registrazione
        authenticationService.setOnSucceeded(authEvent -> {
            AuthenticationService.AuthenticationResult result = authenticationService.getValue();
            
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    // Mostra un messaggio di successo e torna al login
                    registerErrorLabel.setText("Registrazione completata con successo! Effettua il login.");
                    registerErrorLabel.setStyle("-fx-text-fill: green;");
                    registerErrorLabel.setVisible(true);
                    
                    // Pulisce i campi e torna al login dopo 2 secondi
                    clearRegistrationFields();
                    
                    // Torna automaticamente al login
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(2000);
                            switchToLogin(null);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                } else {
                    registerErrorLabel.setText(result.getErrorMessage());
                    registerErrorLabel.setStyle("-fx-text-fill: red;");
                    registerErrorLabel.setVisible(true);
                }
                registerButton.setDisable(false);
            });
        });

        // Gestisce gli errori durante la registrazione
        authenticationService.setOnFailed(authEvent -> {
            Platform.runLater(() -> {
                Throwable exception = authenticationService.getException();
                registerErrorLabel.setText("Errore durante la registrazione: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
                registerErrorLabel.setStyle("-fx-text-fill: red;");
                registerErrorLabel.setVisible(true);
                registerButton.setDisable(false);
            });
        });

        // Avvia il service di autenticazione
        authenticationService.start();
    }

    /**
     * Clears all registration form fields.
     */
    private void clearRegistrationFields() {
        regFirstNameField.clear();
        regLastNameField.clear();
        regUsernameField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
    }

    /**
     * Switches from registration view back to login view.
     * 
     * @param event the switch button click event
     */
    @FXML
    private void switchToLogin(ActionEvent event) {
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
    }

}

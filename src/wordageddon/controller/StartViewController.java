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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller class for the initial start screen of the Wordageddon application.
 * This controller manages user authentication flow including both login and registration
 * functionality. It provides a dual-panel interface where users can switch between
 * login and registration forms.
 * 
 * Key features:
 * - User login with email/password validation
 * - New user registration with comprehensive form validation
 * - Email format validation using regex patterns
 * - Password confirmation for registration
 * - Error messaging and user feedback
 * - Seamless transition to dashboard upon successful authentication
 * 
 * The controller implements form validation and provides visual feedback
 * through error labels. Currently includes placeholder logic for database
 * integration which is planned for future implementation.
 * 
 * @author Francesco Peluso
 * @version 1.0
 * @since 2024
 */
public class StartViewController implements Initializable {

    // Login form components
    /** Button to trigger login action */
    @FXML
    private Button loginButton;
    /** Container for the login form panel */
    @FXML
    private VBox loginPane;
    /** Text field for user email input in login form */
    @FXML
    private TextField loginEmailField;
    /** Password field for user password input in login form */
    @FXML
    private PasswordField loginPasswordField;
    /** Label for displaying login error messages */
    @FXML
    private Label loginErrorLabel;
    
    // Registration form components
    /** Container for the registration form panel */
    @FXML
    private VBox registerPane;
    /** Text field for first name input in registration form */
    @FXML
    private TextField regFirstNameField;
    /** Text field for last name input in registration form */
    @FXML
    private TextField regLastNameField;
    /** Text field for username input in registration form */
    @FXML
    private TextField regUsernameField;
    /** Text field for email input in registration form */
    @FXML
    private TextField regEmailField;
    /** Password field for password input in registration form */
    @FXML
    private PasswordField regPasswordField;
    /** Password field for password confirmation in registration form */
    @FXML
    private PasswordField regConfirmPasswordField;
    /** Button to trigger registration action */
    @FXML
    private Button registerButton;
    /** Label for displaying registration error messages */
    @FXML
    private Label registerErrorLabel;

    /**
     * Initializes the start view controller.
     * This method is automatically called by JavaFX after loading the FXML file.
     * Currently serves as a placeholder for future initialization logic such as
     * setting up default form states or loading user preferences.
     * 
     * @param url the location used to resolve relative paths for the root object, or null if unknown
     * @param rb the resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    /**
     * Handles user login action with comprehensive input validation.
     * This method processes the login form submission by validating user input,
     * checking email format, and attempting authentication. Upon successful validation,
     * the user is redirected to the main dashboard.
     * 
     * Validation steps:
     * 1. Checks that both email and password fields are not empty
     * 2. Validates email format using regex pattern
     * 3. Authenticates user credentials (placeholder for database integration)
     * 4. Redirects to dashboard on success or displays error messages
     * 
     * Email regex pattern: ^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$
     * - Accepts alphanumeric characters, dots, and hyphens before @
     * - Requires @ symbol
     * - Accepts domain name with dot and 2+ letter extension
     * 
     * @param event the action event triggered by clicking the login button
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

        // TODO - LOGICA LOGIN DA IMPLEMENTARE CON DB
        System.out.println("Login con: " + email + " / " + password);
        loginErrorLabel.setVisible(false);

        // per ora porto direttamente alla dashboard, indifferentemente se il login è corretto o meno
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wordageddon/view/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginErrorLabel.setText("Errore nel caricamento della dashboard.");
            loginErrorLabel.setVisible(true);
        }

    }

    /**
     * Switches the interface from login view to registration view.
     * This method hides the login panel and makes the registration panel visible,
     * allowing users to access the new user registration form. The visibility
     * and management properties are updated to ensure proper layout behavior.
     * 
     * @param event the action event triggered by clicking the switch to register link
     */
    @FXML
    private void switchToRegister(ActionEvent event) {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }

    /**
     * Handles user registration action with comprehensive form validation.
     * This method processes the registration form submission by validating all
     * required fields, checking email format, and confirming password match.
     * 
     * Validation process:
     * 1. Ensures all required fields (name, surname, username, email, passwords) are filled
     * 2. Validates email format using the same regex pattern as login
     * 3. Confirms that password and confirmation password match exactly
     * 4. Processes registration (placeholder for database integration)
     * 5. Displays appropriate error messages for validation failures
     * 
     * Required fields:
     * - First Name (nome)
     * - Last Name (cognome) 
     * - Username
     * - Email (with format validation)
     * - Password
     * - Password Confirmation (must match password)
     * 
     * @param event the action event triggered by clicking the register button
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
            loginErrorLabel.setText("Formato email non valido.");
            loginErrorLabel.setVisible(true);
            return;
        }

        // conferma della password
        if (!password.equals(confirmPassword)) {
            registerErrorLabel.setText("Le password non corrispondono.");
            registerErrorLabel.setVisible(true);
            return;
        }

        // TODO - LOGICA REGISTRAZIONE DA IMPLEMENTARE CON DB
        System.out.println("Registrazione: " + nome + " " + cognome + ", Username: " + username + ", Email: " + email);
        registerErrorLabel.setVisible(false);
    }

    /**
     * Switches the interface from registration view back to login view.
     * This method hides the registration panel and makes the login panel visible,
     * allowing users to return to the login form. The visibility and management
     * properties are updated to ensure proper layout behavior.
     * 
     * @param event the action event triggered by clicking the switch to login link
     */
    @FXML
    private void switchToLogin(ActionEvent event) {
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
    }

}

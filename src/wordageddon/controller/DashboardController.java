package wordageddon.controller;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import wordageddon.service.SceneNavigationService;
import wordageddon.service.UserSession;
import wordageddon.model.User;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller for the main dashboard interface.
 * Manages navigation to game, leaderboard, settings, and logout functionality.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 */
public class DashboardController implements Initializable {

    /** Welcome label for personalized greeting */
    @FXML
    private Label welcomeLabel;

    /** Button for admin panel access */
    @FXML
    private Button adminButton;

    /** Service for scene navigation */
    private SceneNavigationService sceneNavigationService;

    /**
     * Initializes the dashboard controller.
     * Sets up the welcome message based on the current logged-in user
     * and configures the visibility of admin-only controls.
     * 
     * @param location the location used to resolve relative paths for the root object, or null if not known
     * @param resources the resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // personalizza il messaggio di benvenuto con il nome dell'utente
        UserSession userSession = UserSession.getInstance();
        if (userSession.isLoggedIn()) {
            User currentUser = userSession.getCurrentUser();
            String firstName = currentUser.getFname();
            
            // usa il nome se disponibile, altrimenti il username
            if (firstName != null && !firstName.trim().isEmpty()) {
                welcomeLabel.setText("Benvenuto, " + firstName + "! Cosa vuoi fare oggi?");
            } else {
                welcomeLabel.setText("Benvenuto, " + currentUser.getUsername() + "! Cosa vuoi fare oggi?");
            }
            
            // mostra il pulsante di amministrazione solo se l'utente è admin
            if (adminButton != null) {
                boolean isAdmin = currentUser.getIsAdmin() != null && currentUser.getIsAdmin();
                adminButton.setVisible(isAdmin);
                adminButton.setManaged(isAdmin); // rimuove lo spazio riservato se invisibile
            }
        }
    }

    /**
     * Handles the leaderboard and game history action by transitioning to the leaderboard view.
     * Creates and configures a navigation service to load the leaderboard interface asynchronously.
     * 
     * @param event the action event triggered by clicking the leaderboard button
     */
    @FXML
    private void handleLeaderboardAndHistory(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // crea e configura il service di navigazione per la classifica
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/LeaderboardView.fxml", 
            "Wordageddon - Classifica", 
            stage);

        // gestisce il completamento della navigazione
        sceneNavigationService.setOnSucceeded(e -> {
            SceneNavigationService.NavigationResult result = sceneNavigationService.getValue();
            
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    sceneNavigationService.applySceneToStage(result);
                    stage.show();
                    System.out.println("Classifica caricata!");
                } else {
                    System.err.println("Errore nel caricamento della classifica: " + result.getErrorMessage());
                }
            });
        });

        // gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel caricamento della classifica: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Handles the start game action by transitioning to the game interface.
     * Creates and launches a new game session for the current user.
     * 
     * @param event the action event triggered by clicking the start game button
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // avvia normalmente il gioco
        loadGameView(stage);
    }
    
    /**
     * Loads the game view asynchronously.
     * Creates and configures a navigation service to load the game interface.
     * 
     * @param stage the current stage to apply the scene to
     */
    private void loadGameView(Stage stage) {
        // crea e configura il service di navigazione per il gioco
        String gameViewPath = "/wordageddon/view/GameView.fxml";
        String windowTitle = "Wordageddon - Gioco in corso";
        
        sceneNavigationService = new SceneNavigationService(
            gameViewPath, 
            windowTitle, 
            stage);

        // gestisce il completamento della navigazione
        sceneNavigationService.setOnSucceeded(e -> {
            SceneNavigationService.NavigationResult result = sceneNavigationService.getValue();
            
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    sceneNavigationService.applySceneToStage(result);
                    stage.show();
                    System.out.println("Partita avviata!");
                } else {
                    System.err.println("Errore nel caricamento della vista del gioco: " + result.getErrorMessage());
                }
            });
        });

        // gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel caricamento della vista del gioco: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Handles the settings/administration action by transitioning to the admin panel.
     * Verifies that the current user has administrative privileges before allowing access.
     * Only users with admin rights can access the administration interface.
     * 
     * @param event the action event triggered by clicking the settings button
     */
    @FXML
    private void handleSettings(ActionEvent event) {
        UserSession userSession = UserSession.getInstance();
        
        // verifica se l'utente è loggato e se è admin
        if (!userSession.isLoggedIn()) {
            System.err.println("Utente non autenticato");
            return;
        }
        
        User currentUser = userSession.getCurrentUser();
        if (currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            System.out.println("Accesso negato: l'utente non è un amministratore");
            // TODO: mostra messaggio di errore nella UI
            return;
        }
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // crea e configura il service di navigazione per il pannello admin
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/AdminView.fxml", 
            "Wordageddon - Pannello Amministratore", 
            stage);

        // gestisce il completamento della navigazione
        sceneNavigationService.setOnSucceeded(e -> {
            SceneNavigationService.NavigationResult result = sceneNavigationService.getValue();
            
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    sceneNavigationService.applySceneToStage(result);
                    stage.show();
                    System.out.println("Pannello amministratore caricato!");
                } else {
                    System.err.println("Errore nel caricamento del pannello admin: " + result.getErrorMessage());
                }
            });
        });

        // gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel caricamento del pannello admin: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Handles the user logout action by clearing the user session and redirecting to the login screen.
     * Terminates the current user session and navigates back to the start view.
     * 
     * @param event the action event triggered by clicking the logout button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // logout dell'utente corrente
        UserSession userSession = UserSession.getInstance();
        userSession.logout();
        
        System.out.println("Logout effettuato con successo");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // crea e configura il service di navigazione per tornare al login
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/StartView.fxml", 
            "Wordageddon - Login", 
            stage);

        // gestisce il completamento della navigazione
        sceneNavigationService.setOnSucceeded(e -> {
            SceneNavigationService.NavigationResult result = sceneNavigationService.getValue();
            
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    sceneNavigationService.applySceneToStage(result);
                    stage.show();
                    System.out.println("Reindirizzamento al login completato!");
                } else {
                    System.err.println("Errore nel reindirizzamento al login: " + result.getErrorMessage());
                }
            });
        });

        // gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel reindirizzamento al login: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // avvia il service di navigazione
        sceneNavigationService.start();
    }
}

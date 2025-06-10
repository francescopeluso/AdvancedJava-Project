package wordageddon.controller;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;
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

    /** Service for scene navigation */
    private SceneNavigationService sceneNavigationService;

    /**
     * Initializes the dashboard controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // personalizza il messaggio di benvenuto con il nome dell'utente
        UserSession userSession = UserSession.getInstance();
        if (userSession.isLoggedIn()) {
            User currentUser = userSession.getCurrentUser();
            String firstName = currentUser.getFname();
            if (firstName != null && !firstName.trim().isEmpty()) {
                welcomeLabel.setText("Benvenuto, " + firstName + "! Cosa vuoi fare oggi?");
            } else {
                welcomeLabel.setText("Benvenuto, " + currentUser.getUsername() + "! Cosa vuoi fare oggi?");
            }
        }
    }

    /**
     * Handles the leaderboard and game history action by transitioning to the leaderboard view.
     * 
     * @param event the action event triggered by clicking the leaderboard button
     */
    @FXML
    private void handleLeaderboardAndHistory(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Crea e configura il service di navigazione
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/LeaderboardView.fxml", 
            "Wordageddon - Classifica", 
            stage);

        // Gestisce il completamento della navigazione
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

        // Gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel caricamento della classifica: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // Avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Handles the start game action by transitioning to the game interface using asynchronous loading.
     * 
     * @param event the action event triggered by clicking the start game button
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Crea e configura il service di navigazione
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/GameView.fxml", 
            "Wordageddon - Gioco in corso", 
            stage);

        // Gestisce il completamento della navigazione
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

        // Gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel caricamento della vista del gioco: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // Avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Handles the settings/administration action by transitioning to the settings view (admin panel)
     * 
     * @param event the action event triggered by clicking the settings button
     */
    @FXML
    private void handleSettings(ActionEvent event) {
        UserSession userSession = UserSession.getInstance();
        
        // Verifica se l'utente è loggato e se è admin
        if (!userSession.isLoggedIn()) {
            System.err.println("Utente non autenticato");
            return;
        }
        
        User currentUser = userSession.getCurrentUser();
        if (currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            System.out.println("Accesso negato: l'utente non è un amministratore");
            // TODO: Mostra messaggio di errore nella UI
            return;
        }
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Crea e configura il service di navigazione per il pannello admin
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/AdminView.fxml", 
            "Wordageddon - Pannello Amministratore", 
            stage);

        // Gestisce il completamento della navigazione
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

        // Gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel caricamento del pannello admin: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // Avvia il service di navigazione
        sceneNavigationService.start();
    }

    /**
     * Handles the user logout action by deleting user session redirecting to the login screen.
     * 
     * @param event the action event triggered by clicking the logout button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // Logout dell'utente
        UserSession userSession = UserSession.getInstance();
        userSession.logout();
        
        System.out.println("Logout effettuato con successo");
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Crea e configura il service di navigazione per tornare al login
        sceneNavigationService = new SceneNavigationService(
            "/wordageddon/view/StartView.fxml", 
            "Wordageddon - Login", 
            stage);

        // Gestisce il completamento della navigazione
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

        // Gestisce gli errori durante la navigazione
        sceneNavigationService.setOnFailed(e -> {
            Platform.runLater(() -> {
                Throwable exception = sceneNavigationService.getException();
                System.err.println("Errore nel reindirizzamento al login: " + 
                    (exception != null ? exception.getMessage() : "Errore sconosciuto"));
            });
        });

        // Avvia il service di navigazione
        sceneNavigationService.start();
    }
}

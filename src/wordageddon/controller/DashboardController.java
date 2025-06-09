package wordageddon.controller;

import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;
import wordageddon.service.SceneNavigationService;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
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

    /** Service for scene navigation */
    private SceneNavigationService sceneNavigationService;

    /**
     * Initializes the dashboard controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO
    }

    /**
     * Handles the leaderboard and game history action by transitioning to the leaderboard view.
     * 
     * @param event the action event triggered by clicking the leaderboard button
     */
    @FXML
    private void handleLeaderboardAndHistory(ActionEvent event) {
        System.out.println("classifica, lista partite, ecc...");
        // TODO: carica una nuova scena o mostra una sezione della dashboard
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
        sceneNavigationService.setOnSucceeded(navEvent -> {
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
        sceneNavigationService.setOnFailed(navEvent -> {
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
        System.out.println("apro pagina admin...");
        // TODO: verifica se l'utente è admin, quindi mostra impostazioni
    }

    /**
     * Handles the user logout action by deleting user session redirecting to the login screen.
     * 
     * @param event the action event triggered by clicking the logout button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("logout...");
        // TODO: reindirizza alla schermata di login
    }
}

package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX Service for handling scene navigation in the background.
 * This service loads FXML files and creates scenes without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class SceneNavigationService extends Service<SceneNavigationService.NavigationResult> {

    private final String fxmlPath;
    private final String sceneTitle;
    private final Stage targetStage;

    /**
     * Result container for scene navigation operation.
     */
    public static class NavigationResult {
        private final Scene scene;
        private final Object controller;
        private final boolean success;
        private final String errorMessage;

        public NavigationResult(Scene scene, Object controller, boolean success, String errorMessage) {
            this.scene = scene;
            this.controller = controller;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public Scene getScene() { return scene; }
        public Object getController() { return controller; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Constructs a new SceneNavigationService.
     * 
     * @param fxmlPath the path to the FXML file to load
     * @param sceneTitle the title for the new scene
     * @param targetStage the stage where the scene will be displayed
     */
    public SceneNavigationService(String fxmlPath, String sceneTitle, Stage targetStage) {
        this.fxmlPath = fxmlPath;
        this.sceneTitle = sceneTitle;
        this.targetStage = targetStage;
    }

    @Override
    protected Task<NavigationResult> createTask() {
        return new Task<NavigationResult>() {
            @Override
            protected NavigationResult call() throws Exception {
                updateMessage("Caricamento della vista...");
                updateProgress(0, 100);

                try {
                    updateMessage("Caricamento FXML: " + fxmlPath);
                    updateProgress(30, 100);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    
                    updateMessage("Creazione della scena...");
                    updateProgress(70, 100);

                    Scene scene = new Scene(root);
                    Object controller = loader.getController();

                    updateMessage("Navigazione completata!");
                    updateProgress(100, 100);

                    return new NavigationResult(scene, controller, true, null);
                    
                } catch (IOException e) {
                    return new NavigationResult(null, null, false, 
                        "Errore nel caricamento della vista: " + e.getMessage());
                }
            }
        };
    }

    /**
     * Applies the loaded scene to the target stage.
     * This method should be called on the JavaFX Application Thread.
     * 
     * @param result the navigation result containing the scene
     */
    public void applySceneToStage(NavigationResult result) {
        if (result.isSuccess() && targetStage != null) {
            // salva le dimensioni correnti della finestra prima di cambiare scena
            double currentWidth = targetStage.getWidth();
            double currentHeight = targetStage.getHeight();
            boolean isMaximized = targetStage.isMaximized();
            
            targetStage.setScene(result.getScene());
            if (sceneTitle != null && !sceneTitle.isEmpty()) {
                targetStage.setTitle(sceneTitle);
            }
            
            // ripristina le dimensioni precedenti se la finestra non era massimizzata
            if (!isMaximized) {
                targetStage.setWidth(currentWidth);
                targetStage.setHeight(currentHeight);
            }
        }
    }
}

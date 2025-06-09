package wordageddon.service;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * Utility class for managing progress indicators and status messages
 * during asynchronous operations.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class ProgressIndicatorManager {

    /**
     * Binds a progress indicator and status label to a JavaFX Service.
     * This automatically updates the UI components based on the service state.
     * 
     * @param service the service to bind
     * @param progressIndicator the progress indicator to update (can be null)
     * @param statusLabel the status label to update (can be null)
     */
    public static void bindToService(Service<?> service, 
                                   ProgressIndicator progressIndicator, 
                                   Label statusLabel) {
        if (progressIndicator != null) {
            // Collega visibilità e progresso dell'indicatore di progresso
            progressIndicator.visibleProperty().bind(service.runningProperty());
            progressIndicator.progressProperty().bind(service.progressProperty());
        }

        if (statusLabel != null) {
            // Collega il testo della label di stato al messaggio del service
            statusLabel.textProperty().bind(service.messageProperty());
            
            // Gestisce i cambiamenti di stato del service
            service.stateProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    switch (newValue) {
                        case SCHEDULED:
                            statusLabel.setText("Operazione in attesa...");
                            break;
                        case RUNNING:
                            // Il messaggio sarà aggiornato dal service
                            break;
                        case SUCCEEDED:
                            statusLabel.setText("Operazione completata con successo!");
                            break;
                        case CANCELLED:
                            statusLabel.setText("Operazione annullata");
                            break;
                        case FAILED:
                            statusLabel.setText("Errore durante l'operazione");
                            break;
                        case READY:
                            statusLabel.setText("Pronto");
                            break;
                    }
                });
            });
        }
    }

    /**
     * Shows a progress indicator and status label for the duration of a service execution.
     * Automatically hides them when the service completes.
     * 
     * @param service the service to monitor
     * @param progressIndicator the progress indicator to show/hide
     * @param statusLabel the status label to show/hide
     */
    public static void showDuringExecution(Service<?> service, 
                                         ProgressIndicator progressIndicator, 
                                         Label statusLabel) {
        // Mostra i componenti quando il service inizia
        service.setOnRunning(e -> Platform.runLater(() -> {
            if (progressIndicator != null) {
                progressIndicator.setVisible(true);
            }
            if (statusLabel != null) {
                statusLabel.setVisible(true);
            }
        }));

        // Nasconde i componenti quando il service completa (successo o fallimento)
        service.setOnSucceeded(e -> Platform.runLater(() -> hideComponents(progressIndicator, statusLabel)));
        service.setOnFailed(e -> Platform.runLater(() -> hideComponents(progressIndicator, statusLabel)));
        service.setOnCancelled(e -> Platform.runLater(() -> hideComponents(progressIndicator, statusLabel)));
    }

    /**
     * Helper method to hide progress components.
     */
    private static void hideComponents(ProgressIndicator progressIndicator, Label statusLabel) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
        if (statusLabel != null) {
            // Mantiene la label visibile per un breve periodo per mostrare lo stato finale
            Timeline hideDelay = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                statusLabel.setVisible(false);
            }));
            hideDelay.play();
        }
    }

    /**
     * Creates a formatted status message for operations with progress.
     * 
     * @param operation the operation name
     * @param current the current step
     * @param total the total steps
     * @return formatted status message
     */
    public static String createProgressMessage(String operation, int current, int total) {
        return String.format("%s (%d/%d)", operation, current, total);
    }
}

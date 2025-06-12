package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import wordageddon.model.TextAnalysisService;

import java.io.File;
import java.io.IOException;

/**
 * JavaFX Service for initializing the game components in the background.
 * This service handles the loading of stopwords and creation of TextAnalysisService
 * without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class GameInitializationService extends Service<GameInitializationService.GameInitializationResult> {

    private final File stopwordsFile;

    /**
     * Result container for game initialization operation.
     */
    public static class GameInitializationResult {
        private final TextAnalysisService textAnalysisService;
        private final boolean success;
        private final String errorMessage;

        public GameInitializationResult(TextAnalysisService textAnalysisService, boolean success, String errorMessage) {
            this.textAnalysisService = textAnalysisService;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public TextAnalysisService getTextAnalysisService() { return textAnalysisService; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Constructs a new GameInitializationService.
     * 
     * @param stopwordsFile the file containing stopwords to load
     */
    public GameInitializationService(File stopwordsFile) {
        this.stopwordsFile = stopwordsFile;
    }

    @Override
    protected Task<GameInitializationResult> createTask() {
        return new Task<GameInitializationResult>() {
            @Override
            protected GameInitializationResult call() throws Exception {
                updateMessage("Inizializzazione servizio di analisi del testo...");
                updateProgress(0, 100);

                try {
                    // Inizializza il TextAnalysisService
                    TextAnalysisService textAnalysisService = new TextAnalysisService();
                    
                    updateMessage("Caricamento stopwords...");
                    updateProgress(50, 100);
                    
                    if (stopwordsFile != null && stopwordsFile.exists()) {
                        textAnalysisService.loadStopwords(stopwordsFile);
                    }

                    updateMessage("Inizializzazione completata!");
                    updateProgress(100, 100);

                    return new GameInitializationResult(textAnalysisService, true, null);
                    
                } catch (IOException e) {
                    return new GameInitializationResult(null, false, 
                        "Errore durante l'inizializzazione: " + e.getMessage());
                }
            }
        };
    }
}

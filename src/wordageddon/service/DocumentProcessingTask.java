package wordageddon.service;

import javafx.concurrent.Task;
import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.TextAnalysisService;

import java.io.File;

/**
 * JavaFX Task for processing a single document asynchronously.
 * This task handles document processing and DTM population
 * without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class DocumentProcessingTask extends Task<Void> {

    private final TextAnalysisService textAnalysisService;
    private final DocumentTermMatrix dtm;
    private final File documentFile;

    /**
     * Constructs a new DocumentProcessingTask.
     * 
     * @param textAnalysisService the service for text analysis
     * @param dtm the Document-Term Matrix to populate
     * @param documentFile the file to process
     */
    public DocumentProcessingTask(TextAnalysisService textAnalysisService, 
                                DocumentTermMatrix dtm, File documentFile) {
        this.textAnalysisService = textAnalysisService;
        this.dtm = dtm;
        this.documentFile = documentFile;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Processo...  " + documentFile.getName() + "...");
        updateProgress(0, 100);

        // Processa il documento
        textAnalysisService.processDocument(dtm, documentFile);

        updateMessage("Completato: " + documentFile.getName());
        updateProgress(100, 100);

        return null;
    }
}

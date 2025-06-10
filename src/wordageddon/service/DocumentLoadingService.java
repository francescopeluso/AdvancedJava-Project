package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.TextAnalysisService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JavaFX Service for loading and processing documents in the background.
 * This service handles document loading, content reading, and DTM creation
 * without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class DocumentLoadingService extends Service<DocumentLoadingService.DocumentLoadingResult> {

    private final TextAnalysisService textAnalysisService;
    private final File documentsDirectory;
    private final int numberOfDocuments;

    /**
     * Result container for document loading operation.
     */
    public static class DocumentLoadingResult {
        private final DocumentTermMatrix dtm;
        private final List<String> visibleDocuments;
        private final List<String> documentContents;
        private final List<File> selectedFiles;

        public DocumentLoadingResult(DocumentTermMatrix dtm, List<String> visibleDocuments, 
                                   List<String> documentContents, List<File> selectedFiles) {
            this.dtm = dtm;
            this.visibleDocuments = visibleDocuments;
            this.documentContents = documentContents;
            this.selectedFiles = selectedFiles;
        }

        public DocumentTermMatrix getDtm() { return dtm; }
        public List<String> getVisibleDocuments() { return visibleDocuments; }
        public List<String> getDocumentContents() { return documentContents; }
        public List<File> getSelectedFiles() { return selectedFiles; }
    }

    /**
     * Constructs a new DocumentLoadingService.
     * 
     * @param textAnalysisService the service for text analysis
     * @param documentsDirectory the directory containing documents
     * @param numberOfDocuments the number of documents to load
     */
    public DocumentLoadingService(TextAnalysisService textAnalysisService, 
                                File documentsDirectory, int numberOfDocuments) {
        this.textAnalysisService = textAnalysisService;
        this.documentsDirectory = documentsDirectory;
        this.numberOfDocuments = numberOfDocuments;
    }

    @Override
    protected Task<DocumentLoadingResult> createTask() {
        return new Task<DocumentLoadingResult>() {
            @Override
            protected DocumentLoadingResult call() throws Exception {
                updateMessage("Caricamento documenti...");
                updateProgress(0, 100);

                // ottieni tutti i documenti disponibili
                File[] files = documentsDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
                if (files == null || files.length == 0) {
                    throw new IOException("Nessun documento trovato nella directory documents/");
                }

                updateMessage("Selezione documenti casuali...");
                updateProgress(20, 100);

                // seleziona documenti casuali
                List<File> allFiles = new ArrayList<>();
                Collections.addAll(allFiles, files);
                Collections.shuffle(allFiles);

                int numDocuments = Math.min(numberOfDocuments, allFiles.size());
                List<File> selectedFiles = allFiles.subList(0, numDocuments);

                updateMessage("Creazione Document-Term Matrix...");
                updateProgress(40, 100);

                // crea la DTM per i documenti selezionati
                DocumentTermMatrix dtm = new DocumentTermMatrix();
                int processedFiles = 0;
                
                for (File file : selectedFiles) {
                    if (isCancelled()) {
                        throw new InterruptedException("Operazione annullata dall'utente");
                    }
                    
                    textAnalysisService.processDocument(dtm, file);
                    processedFiles++;
                    
                    updateMessage("Processamento " + file.getName() + "...");
                    updateProgress(40 + (processedFiles * 30.0 / selectedFiles.size()), 100);
                }

                updateMessage("Caricamento contenuti documenti...");
                updateProgress(70, 100);

                // carica i nomi e contenuti dei documenti
                List<String> visibleDocuments = new ArrayList<>();
                List<String> documentContents = new ArrayList<>();
                
                int loadedContents = 0;
                for (File file : selectedFiles) {
                    if (isCancelled()) {
                        throw new InterruptedException("Operazione annullata dall'utente");
                    }
                    
                    String fileName = file.getName();
                    visibleDocuments.add(fileName);
                    
                    try {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        documentContents.add(content);
                    } catch (IOException e) {
                        documentContents.add("Errore nel caricamento del documento " + fileName);
                    }
                    
                    loadedContents++;
                    updateProgress(70 + (loadedContents * 25.0 / selectedFiles.size()), 100);
                }

                updateMessage("Completato!");
                updateProgress(100, 100);

                return new DocumentLoadingResult(dtm, visibleDocuments, documentContents, selectedFiles);
            }
        };
    }
}

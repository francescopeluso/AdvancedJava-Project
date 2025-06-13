package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.TextAnalysisService;
import wordageddon.model.GameDataContainer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified service for document management, processing and persistence.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class DocumentServices {
    
    private static final String GAME_DATA_FILE = "game_data.ser";
    private static final String DOCUMENTS_DIR = "data/documents";
    
    private GameDataContainer gameData;
    private TextAnalysisService textAnalysisService;
    
    /**
     * Constructs a new DocumentServices with initialized components.
     */
    public DocumentServices() {
        this.textAnalysisService = new TextAnalysisService();
        createDocumentsDirectory();
        loadGameData();
        loadDocumentsFromDirectory();
    }
    
    /**
     * Loads a new document from file and adds it to the collection.
     * 
     * @param filePath path to the document file
     * @return true if successful, false otherwise
     */
    public boolean loadDocumentFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            String content = readFileContent(path);
            
            if (content.trim().isEmpty()) {
                return false;
            }
            
            // salvo il documento nella directory dei documenti
            String fileName = path.getFileName().toString();
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
            }
            saveDocumentToFile(fileName, content);
            
            // ricarico tutti i documenti dalla directory
            loadDocumentsFromDirectory();
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Error loading document: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads stopwords from file and updates the collection.
     * 
     * @param filePath path to the stopwords file
     * @return true if successful, false otherwise
     */
    public boolean loadStopwordsFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            
            // uso uno stream per leggere, filtrare e raccogliere le stopwords
            Set<String> stopwords = Files.lines(path)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
            
            if (gameData == null) {
                gameData = new GameDataContainer();
                gameData.setDocuments(new ArrayList<>());
            }
            
            gameData.setStopwords(stopwords);
            
            // rigenero la DTM con le nuove stopwords
            regenerateDocumentTermMatrix();
            
            // salvo i dati aggiornati
            saveGameData();
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Error loading stopwords: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates stopwords from a comma-separated string.
     * 
     * @param stopwordsText comma-separated string of stopwords
     * @return true if successful, false otherwise
     */
    public boolean updateStopwordsFromText(String stopwordsText) {
        try {
            // uso uno stream per processare la stringa delle stopwords
            Set<String> stopwords = stopwordsText != null && !stopwordsText.trim().isEmpty() ?
                Arrays.stream(stopwordsText.split(","))
                    .map(String::trim)
                    .filter(word -> !word.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet()) :
                new HashSet<>();
            
            if (gameData == null) {
                gameData = new GameDataContainer();
                gameData.setDocuments(new ArrayList<>());
            }
            
            gameData.setStopwords(stopwords);
            
            // rigenero la DTM con le nuove stopwords
            regenerateDocumentTermMatrix();
            
            // salvo i dati aggiornati
            saveGameData();
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error updating stopwords: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Removes a document from the collection by index.
     * 
     * @param index index of the document to remove
     * @return true if successful, false otherwise
     */
    public boolean removeDocument(int index) {
        File dir = new File(DOCUMENTS_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || index < 0 || index >= files.length) {
            return false;
        }
        
        // rimuovo il file fisico
        boolean deleted = files[index].delete();
        if (!deleted) {
            System.err.println("Failed to delete file: " + files[index].getName());
            return false;
        }
        
        // ricarico tutti i documenti dalla directory
        loadDocumentsFromDirectory();
        
        return true;
    }
    
    /**
     * Gets the current list of documents.
     * 
     * @return list of document contents
     */
    public List<String> getDocuments() {
        if (gameData == null || gameData.getDocuments() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(gameData.getDocuments());
    }
    
    /**
     * Gets the current set of stopwords.
     * 
     * @return set of stopwords
     */
    public Set<String> getStopwords() {
        if (gameData == null || gameData.getStopwords() == null) {
            return new HashSet<>();
        }
        return new HashSet<>(gameData.getStopwords());
    }
    
    /**
     * Gets the current document term matrix.
     * 
     * @return the DTM
     */
    public DocumentTermMatrix getDocumentTermMatrix() {
        if (gameData == null) {
            return null;
        }
        return gameData.getDocumentTermMatrix();
    }
    
    /**
     * Gets basic statistics about the current data.
     * 
     * @return map with statistics
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        
        if (gameData == null) {
            stats.put("documents", 0);
            stats.put("stopwords", 0);
            stats.put("vocabulary", 0);
        } else {
            stats.put("documents", gameData.getDocuments() != null ? gameData.getDocuments().size() : 0);
            stats.put("stopwords", gameData.getStopwords() != null ? gameData.getStopwords().size() : 0);
            
            DocumentTermMatrix dtm = gameData.getDocumentTermMatrix();
            stats.put("vocabulary", dtm != null ? dtm.getVocabularySize() : 0);
        }
        
        return stats;
    }
    
    /**
     * Regenerates the Document Term Matrix from current documents and stopwords.
     */
    private void regenerateDocumentTermMatrix() {
        if (gameData == null || gameData.getDocuments() == null || gameData.getDocuments().isEmpty()) {
            return;
        }
        
        Set<String> stopwords = gameData.getStopwords();
        if (stopwords == null) {
            stopwords = new HashSet<>();
        }
        
        // assicuro che il servizio di analisi del testo sia inizializzato
        if (this.textAnalysisService == null) {
            this.textAnalysisService = new TextAnalysisService();
        }

        // creo la DTM usando il servizio di analisi del testo
        DocumentTermMatrix dtm = textAnalysisService.createDocumentTermMatrix(
            gameData.getDocuments(), stopwords);
        
        gameData.setDocumentTermMatrix(dtm);
        gameData.updateTimestamp();
    }

    /**
     * Forces regeneration of the Document Term Matrix and saves the game data.
     * To be called from the admin panel.
     */
    public void regenerateAndSaveDtm() {
        regenerateDocumentTermMatrix();
        saveGameData();
    }
    
    /**
     * Loads game data from serialized file.
     */
    private void loadGameData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GAME_DATA_FILE))) {
            gameData = (GameDataContainer) ois.readObject();
        } catch (FileNotFoundException e) {
            // il file non esiste ancora, inizio con dati vuoti
            gameData = new GameDataContainer();
            gameData.setDocuments(new ArrayList<>());
            gameData.setStopwords(new HashSet<>());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading game data: " + e.getMessage());
            gameData = new GameDataContainer();
            gameData.setDocuments(new ArrayList<>());
            gameData.setStopwords(new HashSet<>());
        }
    }
    
    /**
     * Saves game data to serialized file.
     */
    private void saveGameData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GAME_DATA_FILE))) {
            oos.writeObject(gameData);
        } catch (IOException e) {
            System.err.println("Error saving game data: " + e.getMessage());
        }
    }
    
    /**
     * Creates the documents directory if it doesn't exist.
     */
    private void createDocumentsDirectory() {
        File dir = new File(DOCUMENTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Loads all documents from the documents directory.
     */
    private void loadDocumentsFromDirectory() {
        File dir = new File(DOCUMENTS_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) {
            return;
        }
        
        // uso uno stream per processare i file di documenti
        List<String> documents = Arrays.stream(files)
            .map(file -> {
                try {
                    String content = readFileContent(file.toPath());
                    return content.trim().isEmpty() ? null : content;
                } catch (IOException e) {
                    System.err.println("Error reading document " + file.getName() + ": " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (gameData == null) {
            gameData = new GameDataContainer();
            gameData.setStopwords(new HashSet<>());
        }
        
        gameData.setDocuments(documents);
        
        // rigenero la DTM se abbiamo documenti
        if (!documents.isEmpty()) {
            regenerateDocumentTermMatrix();
            saveGameData();
        }
    }
    
    /**
     * Saves a document to the documents directory.
     * 
     * @param fileName name of the file
     * @param content content to save
     * @throws IOException if saving fails
     */
    private void saveDocumentToFile(String fileName, String content) throws IOException {
        File file = new File(DOCUMENTS_DIR, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
    
    /**
     * Reads the content of a file.
     * 
     * @param path path to the file
     * @return file content as string
     * @throws IOException if reading fails
     */
    private String readFileContent(Path path) throws IOException {
        return Files.readAllLines(path)
            .stream()
            .collect(Collectors.joining("\n"));
    }
    
    /**
     * Gets stopwords as a comma-separated string for display in UI.
     * 
     * @return comma-separated string of stopwords
     */
    public String getStopwordsAsText() {
        Set<String> stopwords = getStopwords();
        if (stopwords.isEmpty()) {
            return "";
        }
        return String.join(", ", stopwords);
    }
    
    /**
     * Creates a task for processing a single document asynchronously.
     * This is used to avoid blocking the UI thread during document processing.
     * 
     * @param dtm the Document-Term Matrix to populate
     * @param documentFile the file to process
     * @return a JavaFX Task for async processing
     */
    public Task<Void> createDocumentProcessingTask(DocumentTermMatrix dtm, File documentFile) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Processo... " + documentFile.getName() + "...");
                updateProgress(0, 100);

                // processa il documento
                textAnalysisService.processDocument(dtm, documentFile);

                updateMessage("Completato: " + documentFile.getName());
                updateProgress(100, 100);

                return null;
            }
        };
    }
    
    /**
     * Creates a service for saving or loading DTM in background.
     * Provides asynchronous operations for document term matrix persistence.
     * 
     * @param dtm the DTM to save, or null if loading
     * @param filePath the file path for save/load operation
     * @param isLoadOperation true if loading, false if saving
     * @return a JavaFX Service that performs the operation
     */
    public Service<Boolean> createPersistenceService(DocumentTermMatrix dtm, String filePath, boolean isLoadOperation) {
        return new Service<Boolean>() {
            /** The DTM loaded from file (only used during load operations) */
            private DocumentTermMatrix loadedDtm;
            
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (!isLoadOperation) {
                            return saveMatrix(dtm, filePath);
                        } else {
                            return loadMatrix(filePath);
                        }
                    }
                    
                    /**
                     * Saves the document term matrix to file.
                     * Provides progress updates during the save operation.
                     * 
                     * @param dtmToSave the DTM to save
                     * @param path the file path to save to
                     * @return true if successful, false otherwise
                     * @throws IOException if an I/O error occurs
                     */
                    private Boolean saveMatrix(DocumentTermMatrix dtmToSave, String path) throws IOException {
                        updateMessage("Salvataggio DTM...");
                        updateProgress(0, 100);

                        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
                            updateProgress(30, 100);
                            
                            // serializza la DTM
                            oos.writeObject(dtmToSave);
                            
                            updateProgress(80, 100);
                            updateMessage("Verifica integrità file...");
                            
                            // verifica dell'integrità del file salvato
                            File savedFile = new File(path);
                            if (!savedFile.exists() || savedFile.length() == 0) {
                                throw new IOException("Il file non è stato salvato correttamente");
                            }
                            
                            updateProgress(100, 100);
                            updateMessage("Document-Term Matrix salvata con successo!");
                            
                            return true;
                        }
                    }
                    
                    /**
                     * Loads the document term matrix from file.
                     * Provides progress updates during the load operation and validates the loaded data.
                     * 
                     * @param path the file path to load from
                     * @return true if successful, false otherwise
                     * @throws IOException if an I/O error occurs
                     * @throws ClassNotFoundException if the file doesn't contain a valid DTM
                     */
                    private Boolean loadMatrix(String path) throws IOException, ClassNotFoundException {
                        updateMessage("Caricamento DTM...");
                        updateProgress(0, 100);

                        File file = new File(path);
                        if (!file.exists()) {
                            throw new FileNotFoundException("File non trovato: " + path);
                        }

                        updateProgress(20, 100);

                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
                            updateProgress(50, 100);
                            updateMessage("Deserializzazione dati...");
                            
                            // carica e verifica il tipo dell'oggetto
                            Object loaded = ois.readObject();
                            if (!(loaded instanceof DocumentTermMatrix)) {
                                throw new IOException("Il file non contiene una Document-Term Matrix valida");
                            }
                            
                            loadedDtm = (DocumentTermMatrix) loaded;
                            
                            updateProgress(90, 100);
                            updateMessage("Validazione dati caricati...");
                            
                            // validazione di base della DTM caricata
                            if (loadedDtm == null) {
                                throw new IOException("Document-Term Matrix caricata è null");
                            }
                            
                            updateProgress(100, 100);
                            updateMessage("Document-Term Matrix caricata con successo!");
                            
                            return true;
                        }
                    }
                };
            }
        };
    }
}

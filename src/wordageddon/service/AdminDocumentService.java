package wordageddon.service;

import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.GameDataContainer;
import wordageddon.model.TextAnalysisService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service for managing documents and stopwords in the admin panel.
 * Handles loading new documents, updating stopwords, and regenerating the DTM.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class AdminDocumentService {
    
    private static final String GAME_DATA_FILE = "game_data.ser";
    private static final String DOCUMENTS_DIR = "documents";
    
    private GameDataContainer gameData;
    private TextAnalysisService textAnalysisService;
    
    public AdminDocumentService() {
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
            
            // Save document to documents directory
            String fileName = path.getFileName().toString();
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
            }
            saveDocumentToFile(fileName, content);
            
            // Reload all documents from directory
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
            List<String> lines = Files.readAllLines(path);
            
            Set<String> stopwords = new HashSet<>();
            for (String line : lines) {
                String trimmed = line.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    stopwords.add(trimmed);
                }
            }
            
            if (gameData == null) {
                gameData = new GameDataContainer();
                gameData.setDocuments(new ArrayList<>());
            }
            
            gameData.setStopwords(stopwords);
            
            // Regenerate DTM with new stopwords
            regenerateDocumentTermMatrix();
            
            // Save updated data
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
            Set<String> stopwords = new HashSet<>();
            
            if (stopwordsText != null && !stopwordsText.trim().isEmpty()) {
                String[] words = stopwordsText.split(",");
                for (String word : words) {
                    String trimmed = word.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        stopwords.add(trimmed);
                    }
                }
            }
            
            if (gameData == null) {
                gameData = new GameDataContainer();
                gameData.setDocuments(new ArrayList<>());
            }
            
            gameData.setStopwords(stopwords);
            
            // Regenerate DTM with new stopwords
            regenerateDocumentTermMatrix();
            
            // Save updated data
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
        
        // Remove the physical file
        boolean deleted = files[index].delete();
        if (!deleted) {
            System.err.println("Failed to delete file: " + files[index].getName());
            return false;
        }
        
        // Reload all documents from directory
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
        
        DocumentTermMatrix dtm = textAnalysisService.createDocumentTermMatrix(
            gameData.getDocuments(), stopwords);
        
        gameData.setDocumentTermMatrix(dtm);
        gameData.updateTimestamp();
    }
    
    /**
     * Loads game data from serialized file.
     */
    private void loadGameData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GAME_DATA_FILE))) {
            gameData = (GameDataContainer) ois.readObject();
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, start with empty data
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
        
        List<String> documents = new ArrayList<>();
        for (File file : files) {
            try {
                String content = readFileContent(file.toPath());
                if (!content.trim().isEmpty()) {
                    documents.add(content);
                }
            } catch (IOException e) {
                System.err.println("Error reading document " + file.getName() + ": " + e.getMessage());
            }
        }
        
        if (gameData == null) {
            gameData = new GameDataContainer();
            gameData.setStopwords(new HashSet<>());
        }
        
        gameData.setDocuments(documents);
        
        // Regenerate DTM if we have documents
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
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
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
}

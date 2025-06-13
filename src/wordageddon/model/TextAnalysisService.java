package wordageddon.model;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Service for analyzing text documents and "tokenize" their content into a Document-Term Matrix (DTM).
 * Handles text normalization, stopword filtering, and Document-Term Matrix population.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class TextAnalysisService {

    /** Set of stopwords to be ignored during document processing */
    private Set<String> stopwords;

    /**
     * Constructs a new TextAnalysisService with an empty stopwords set.
     * 
     * Note: by default, the service does not load any stopwords. You should load them
     * using {@link #loadStopwords(File)} for proper functionality.
     */
    public TextAnalysisService() {
        this.stopwords = new HashSet<>();
    }

    /**
     * Loads stopwords from the specified file.
     * 
     * The file should contain one stopword per line.
     * Comments are supported (lines starting with "//" are ignored).
     * 
     * @param stopwordsFile the file containing stopwords to load
     * @throws IOException if the file cannot be read
     */
    public void loadStopwords(File stopwordsFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(stopwordsFile))) {
            // uso uno stream per processare le linee del file
            reader.lines()
                  .map(String::trim)
                  .filter(line -> !line.isEmpty() && !line.startsWith("//"))
                  .map(String::toLowerCase)
                  .forEach(this.stopwords::add);
        }
    }

    /**
     * Processes all .txt files in the specified directory and adds their terms to the DTM.
     * 
     * @param dtm the Document-Term Matrix to populate
     * @param documentsDir the directory containing .txt files to process
     * @throws IOException if the directory is invalid or files cannot be read
     */
    public void processDocuments(DocumentTermMatrix dtm, File documentsDir) throws IOException {

        // recupero la lista dei files che finiscono in .txt nella directory passata al metodo
        File[] files = documentsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        // se il riferimento a files è nullo, allora ci sarà stato un errore, quindi sollevo una IOException
        if (files == null) {
            throw new IOException("Directory non valida o vuota: " + documentsDir.getAbsolutePath());
        }

        // utilizzo una stream sull'oggetto 'files' per accedere a ciascun file e processarlo
        Arrays.stream(files).forEach(file -> {
            try {
                processDocument(dtm, file);
            } catch (IOException e) {
                throw new RuntimeException("Errore nella lettura del file: " + file.getName(), e);
            }
        });
    }

    /**
     * Processes a single document and adds its terms to the DTM.
     * 
     * @param dtm the Document-Term Matrix to populate
     * @param file the file to process
     * @throws IOException if the file cannot be read
     */
    public void processDocument(DocumentTermMatrix dtm, File file) throws IOException {
        // leggo il contenuto del file, lo normalizzo e filtro le stopwords
        String content = new String(Files.readAllBytes(file.toPath()));
        Arrays.stream(content.toLowerCase().replaceAll("[^a-zàèéìòù]", " ").split("\\s+"))
            .filter(word -> !word.isEmpty() && !this.stopwords.contains(word))
            .forEach(word -> dtm.addTerm(file.getName(), word));
    }

    /**
     * Creates a Document Term Matrix from a list of document contents and stopwords.
     * 
     * @param documents list of document contents as strings
     * @param stopwords set of stopwords to filter out
     * @return a new DocumentTermMatrix populated with the processed documents
     */
    public DocumentTermMatrix createDocumentTermMatrix(List<String> documents, Set<String> currentStopwords) {
        // creo una copia difensiva delle stopwords
        final Set<String> finalStopwords = currentStopwords != null ? new HashSet<>(currentStopwords) : new HashSet<>();

        DocumentTermMatrix dtm = new DocumentTermMatrix();
        boolean firstDocLogged = false;

        if (documents == null || documents.isEmpty()) {
            return dtm;
        }

        // processo ogni documento nella lista
        for (int i = 0; i < documents.size(); i++) {
            String content = documents.get(i);
            if (content == null || content.trim().isEmpty()) {
                continue;
            }
            String documentId = "document_" + (i + 1);

            // raccolgo statistiche sul documento per il logging
            List<String> allWordsInDoc = new ArrayList<>();
            List<String> addedTermsForDoc = new ArrayList<>();
            List<String> filteredOutWords = new ArrayList<>();

            // normalizzazione del contenuto: lowercase e rimozione dei caratteri non alfabetici
            String[] processedWords = content.toLowerCase()
                    .replaceAll("[^a-zàèéìòù]", " ")
                    .split("\\s+");

            // processo ciascuna parola del documento
            for (String word : processedWords) {
                if (!word.isEmpty()) {
                    allWordsInDoc.add(word);
                    if (finalStopwords.contains(word)) {
                        filteredOutWords.add(word);
                    } else {
                        addedTermsForDoc.add(word);
                        dtm.addTerm(documentId, word);
                    }
                }
            }

            // logging solo per il primo documento processato
            if (!firstDocLogged) {
                logFirstDocumentProcessing(documentId, allWordsInDoc, filteredOutWords, 
                                          addedTermsForDoc, finalStopwords);
                firstDocLogged = true;
            }
        }
        return dtm;
    }
    
    /**
     * Logs processing information for the first document.
     * 
     * @param documentId ID of the document
     * @param allWordsInDoc all words in the document
     * @param filteredOutWords words filtered out as stopwords
     * @param addedTermsForDoc terms added to the DTM
     * @param finalStopwords set of stopwords used for filtering
     */
    private void logFirstDocumentProcessing(String documentId, List<String> allWordsInDoc, 
                                           List<String> filteredOutWords, List<String> addedTermsForDoc,
                                           Set<String> finalStopwords) {
        // Debug logging removed for production
    }


    /**
     * Main method for testing TextAnalysisService functionality.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Test method removed for production
    }

}

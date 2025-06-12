package wordageddon.model;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("//")) continue;          // commento in un file di stopwords (deprecato, non gestisco più le stopword da file)
                this.stopwords.add(line.trim().toLowerCase());
            }
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

        /* utilizzo una stream sull'oggetto 'files' (un array di String) per accedere a ciascun file di interesse.
         * per ogni file, leggo il contenuto, e tramite il suo stream vado a manipolare il contenuto per "normalizzare"
         * il tutto, filtrare le stopwoerds e gestire la parola trovata nella DTM
         * */
        Arrays.stream(files).forEach(file -> {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                Arrays.stream(content.toLowerCase().replaceAll("[^a-zàèéìòù]", " ").split("\\s+"))
                    .filter(word -> !word.isEmpty() && !this.stopwords.contains(word))
                    .forEach(word -> dtm.addTerm(file.getName(), word));
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
    public DocumentTermMatrix createDocumentTermMatrix(java.util.List<String> documents, Set<String> currentStopwords) {
        final Set<String> finalStopwords = currentStopwords != null ? new HashSet<>(currentStopwords) : new HashSet<>();
        System.out.println("[TextAnalysisService] Creating DTM. Stopwords received: " +
                           (currentStopwords == null ? "null" : currentStopwords.size()) +
                           ". Effective stopwords for this DTM build: " + finalStopwords.size() + 
                           " -> [" + finalStopwords.stream().limit(20).collect(Collectors.joining(", ")) + (finalStopwords.size() > 20 ? "..." : "") + "]");

        DocumentTermMatrix dtm = new DocumentTermMatrix();
        boolean firstDocLogged = false;

        if (documents == null || documents.isEmpty()) {
            System.out.println("[TextAnalysisService] No documents provided to create DTM. Returning empty DTM.");
            return dtm;
        }

        for (int i = 0; i < documents.size(); i++) {
            String content = documents.get(i);
            if (content == null || content.trim().isEmpty()) {
                System.out.println("[TextAnalysisService] Document " + i + " is null or empty. Skipping.");
                continue;
            }
            String documentId = "document_" + (i + 1);

            List<String> allWordsInDoc = new ArrayList<>();
            List<String> addedTermsForDoc = new ArrayList<>();
            List<String> filteredOutWords = new ArrayList<>();

            String[] processedWords = content.toLowerCase().replaceAll("[^a-zàèéìòù]", " ").split("\\s+");

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

            if (!firstDocLogged) {
                System.out.println("[TextAnalysisService] Processing first document (ID: " + documentId + ")");
                System.out.println("    Original words (sample): [" + allWordsInDoc.stream().limit(15).collect(Collectors.joining(", ")) + (allWordsInDoc.size() > 15 ? "..." : "") + "]");
                System.out.println("    Stopwords found in doc (sample): [" + filteredOutWords.stream().limit(15).collect(Collectors.joining(", ")) + (filteredOutWords.size() > 15 ? "..." : "") + "]");
                System.out.println("    Terms added to DTM for this doc (sample): [" + addedTermsForDoc.stream().limit(15).collect(Collectors.joining(", ")) + (addedTermsForDoc.size() > 15 ? "..." : "") + "]");
                // Example check for a common stopword if present in lists
                String testStopword = "il"; 
                boolean stopwordInSet = finalStopwords.contains(testStopword);
                boolean stopwordInDocWords = allWordsInDoc.contains(testStopword);
                if (stopwordInDocWords) {
                     System.out.println("    DEBUG: Test stopword '" + testStopword + "': In finalStopwordsSet? " + stopwordInSet + ". In document words? " + stopwordInDocWords + ". Should be filtered if both true.");
                }
                firstDocLogged = true;
            }
        }
        System.out.println("[TextAnalysisService] DTM creation complete. Vocabulary size: " + dtm.getVocabularySize());
        return dtm;
    }


    /**
     * Main method for testing TextAnalysisService functionality.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {

            // creo un'istanza del servizio di analisi dei documenti e carico le stopwords
            TextAnalysisService service = new TextAnalysisService();
            service.loadStopwords(new File("stopwords.txt"));

            // inizializzo un dtm vuoto e carico il contenuto della cartella /documents nella root del progetto
            DocumentTermMatrix dtm = new DocumentTermMatrix();
            service.processDocuments(dtm, new File("documents/"));  // DEVO PASSARE LA DIRECTORY, NON IL FILE!

            System.out.println(dtm);

            System.out.println("Frequenze in 'doc1.txt': " + dtm.getTermsForDocument("doc1.txt"));
            System.out.println("Frequenze in 'doc2.txt': " + dtm.getTermsForDocument("doc2.txt"));
            System.out.println("Frequenze in 'doc3.txt': " + dtm.getTermsForDocument("doc3.txt"));

            System.out.println("Lista di tutte le parole: " + dtm.getAllTerms());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

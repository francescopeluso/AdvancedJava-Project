package wordageddon.model;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Service class responsible for analyzing text documents and extracting meaningful terms.
 * This service handles the processing of text files by normalizing content, filtering 
 * stopwords, and populating a Document-Term Matrix with word frequencies.
 * 
 * The service provides functionality for:
 * - Loading and managing stopwords from external files
 * - Processing individual documents or entire directories
 * - Text normalization (lowercase conversion, special character removal)
 * - Integration with the DocumentTermMatrix for frequency tracking
 * 
 * Text processing workflow:
 * 1. Load content from text files
 * 2. Normalize text (convert to lowercase, remove punctuation)
 * 3. Split into individual words
 * 4. Filter out empty strings and stopwords
 * 5. Add remaining terms to the Document-Term Matrix
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
     * The service is ready to process documents but should load stopwords
     * using {@link #loadStopwords(File)} for optimal text filtering.
     */
    public TextAnalysisService() {
        this.stopwords = new HashSet<>();
    }

    /**
     * Loads stopwords from a specified file into the internal stopwords set.
     * Stopwords are common words (articles, prepositions, etc.) that should be
     * filtered out during text analysis to focus on meaningful content.
     * 
     * File format expectations:
     * - One stopword per line
     * - Lines starting with "//" are treated as comments and ignored
     * - All stopwords are converted to lowercase for case-insensitive matching
     * 
     * @param stopwordsFile the file containing stopwords to load
     * @throws IOException if the file cannot be read or does not exist
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
     * Processes all .txt files in the specified directory and adds their term frequencies
     * to the provided Document-Term Matrix. This method processes multiple documents
     * in batch, making it efficient for analyzing entire document collections.
     * 
     * Processing steps for each file:
     * 1. Read the complete file content
     * 2. Convert to lowercase and remove non-alphabetic characters (except àèéìòù)
     * 3. Split into individual words
     * 4. Filter out empty strings and stopwords
     * 5. Add each valid term to the DTM with the filename as document identifier
     * 
     * @param dtm the Document-Term Matrix to populate with term frequencies
     * @param documentsDir the directory containing .txt files to process
     * @throws IOException if the directory is invalid, empty, or files cannot be read
     * @throws RuntimeException if individual file reading fails during processing
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
     * Processes a single document file and adds its term frequencies to the 
     * provided Document-Term Matrix. This method is useful for processing
     * individual files or when selective document processing is required.
     * 
     * Text normalization process:
     * - Converts all text to lowercase
     * - Removes all non-alphabetic characters except Italian accented vowels (àèéìòù)
     * - Splits text into words using whitespace as delimiter
     * - Filters out empty strings and loaded stopwords
     * 
     * @param dtm the Document-Term Matrix to populate with term frequencies
     * @param file the specific file to process and analyze
     * @throws IOException if the file cannot be read or does not exist
     */
    public void processDocument(DocumentTermMatrix dtm, File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        Arrays.stream(content.toLowerCase().replaceAll("[^a-zàèéìòù]", " ").split("\\s+"))
            .filter(word -> !word.isEmpty() && !this.stopwords.contains(word))
            .forEach(word -> dtm.addTerm(file.getName(), word));
    }


    /**
     * Main method for testing and demonstrating the TextAnalysisService functionality.
     * This method creates a complete workflow example showing how to:
     * 1. Initialize the service and load stopwords
     * 2. Create a Document-Term Matrix
     * 3. Process documents from a directory
     * 4. Display results and term frequencies
     * 
     * Expected file structure:
     * - stopwords.txt in the project root
     * - documents/ directory containing doc1.txt, doc2.txt, doc3.txt
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

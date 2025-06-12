package wordageddon.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Document-Term Matrix implementation for text analysis.
 * 
 * Stores word frequencies across multiple documents using nested HashMaps.
 * Supports serialization for data persistence over multiple gameplay sessions.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 */
public class DocumentTermMatrix implements Serializable {
    
    /** Serial version UID for serialization compatibility */
    private static final long serialVersionUID = 1L;    // lo uso per garantire compatibilità tra versioni della classe serializzate (è una buona pratica)
    
    /** 
     * The main data structure representing the Document-Term Matrix.
     * Maps document IDs to their term frequency maps.
     */
    private Map<String, Map<String, Integer>> matrix;
    
    /**
     * Constructs a new empty Document-Term Matrix.
     * Initializes the internal HashMap structure.
     */
    public DocumentTermMatrix() {
        this.matrix = new HashMap<>();
    }
    
    /**
     * Adds a term to the specified document and increments its frequency count.
     * If the document doesn't exist in the matrix, it creates a new entry.
     * If the term doesn't exist for the document, it initializes the count to 1.
     * 
     * @param documentId the unique identifier of the document
     * @param word the term/word to add to the document
     */
    public void addTerm(String documentId, String word) {
        // ottengo la mappa per il documento (creandola se non esiste) e incremento il contatore della parola
        matrix.computeIfAbsent(documentId, k -> new HashMap<>())
              .compute(word, (k, v) -> v == null ? 1 : v + 1);
    }
    
    /**
     * Retrieves all unique terms across all documents in the matrix.
     * The terms are returned in a lexicographically ordered set.
     * 
     * @return a TreeSet containing all unique terms found in any document
     */
    public Set<String> getAllTerms() {
        // raccolgo tutte le parole da tutti i documenti in un unico set ordinato
        return matrix.values().stream()
                .flatMap(doc -> doc.keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }
    
    /**
     * Retrieves all terms and their frequencies for a specific document.
     * 
     * @param documentId the unique identifier of the document
     * @return a Map containing all terms and their frequencies for the document,
     *         or an empty map if the document doesn't exist
     */
    public Map<String, Integer> getTermsForDocument(String documentId) {
        // restituisco le frequenze di tutte le parole di uno specifico documento
        return matrix.getOrDefault(documentId, Collections.emptyMap());
    }

    /**
     * Retrieves the frequency of a specific term in a given document.
     * 
     * @param documentId the unique identifier of the document
     * @param word the term to look up
     * @return the frequency of the term in the document, or 0 if not found
     */
    public int getFrequency(String documentId, String word) {
        // restituisco la frequenza di una specifica parola all'interno di uno specifico documento
        return matrix.getOrDefault(documentId, Collections.emptyMap()).getOrDefault(word, 0);
    }

    /**
     * Retrieves all document identifiers stored in the matrix.
     * 
     * @return a Set containing all document identifiers
     */
    public Set<String> getDocuments() {
        return matrix.keySet();
    }

    /**
     * Gets the size of the vocabulary (total number of unique terms).
     * 
     * @return the number of unique terms across all documents
     */
    public int getVocabularySize() {
        return getAllTerms().size();
    }

    /**
     * Saves the Document-Term Matrix to a file using Java serialization.
     * 
     * @param file the File object representing where the matrix should be saved
     * @throws IOException if an I/O error occurs during saving
     */
    public void saveToFile(File file) throws IOException {
        // salvo la struttura su file serializzato
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    /**
     * Loads a Document-Term Matrix from a file using Java deserialization.
     * 
     * @param file the File object representing the file to load from
     * @return the loaded DocumentTermMatrix instance
     * @throws IOException if an I/O error occurs during loading
     * @throws ClassNotFoundException if the file doesn't contain a valid DocumentTermMatrix
     */
    public static DocumentTermMatrix loadFromFile(File file) throws IOException, ClassNotFoundException {
        // leggo da file serializzato e restituisco il contenuto
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (DocumentTermMatrix) ois.readObject();
        }
    }
    
    /**
     * Test method to demonstrate the functionality of the DocumentTermMatrix class.
     * Creates sample data, saves it to file, and loads it back to verify serialization.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        DocumentTermMatrix dtm = new DocumentTermMatrix();
        
        // test di inserimento di termini
        Stream.of(
            new String[]{"doc1.txt", "cane"},
            new String[]{"doc1.txt", "gatto"},
            new String[]{"doc1.txt", "cane"},
            new String[]{"doc2.txt", "topo"},
            new String[]{"doc2.txt", "gatto"},
            new String[]{"doc2.txt", "gatto"}
        ).forEach(data -> dtm.addTerm(data[0], data[1]));
        
        System.out.println("--- Document Term Matrix - Test 1 ---");
        System.out.println("Frequenze in doc1.txt: " + dtm.getTermsForDocument("doc1.txt"));
        System.out.println("Frequenza di 'gatto' in doc2.txt: " + dtm.getFrequency("doc2.txt", "gatto"));
        
        // test di serializzazione
        try {
            File exportFile = new File("demo_export");
            dtm.saveToFile(exportFile);
            
            System.out.println("File salvato in: " + exportFile.getAbsolutePath());
            
            // test di deserializzazione
            DocumentTermMatrix dtm2 = DocumentTermMatrix.loadFromFile(exportFile);
            
            System.out.println("\n--- Document Term Matrix - Test 2 ---");
            System.out.println("Frequenze in doc2.txt: " + dtm2.getTermsForDocument("doc2.txt"));
            System.out.println("Frequenza di 'cane' in doc1.txt: " + dtm2.getFrequency("doc1.txt", "cane"));
        } catch (Exception ex) {
            System.err.println("Errore di I/O: " + ex.getMessage());
        }
    }
}

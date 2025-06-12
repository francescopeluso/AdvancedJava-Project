package wordageddon.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Container class that holds the Document Term Matrix along with 
 * the associated documents and stopwords.
 * This allows for unified serialization of all game data.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class GameDataContainer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private DocumentTermMatrix documentTermMatrix;
    private List<String> documents;
    private Set<String> stopwords;
    private long lastUpdated;
    
    /**
     * Constructs a new GameDataContainer with default values.
     * Initializes the timestamp to current time.
     */
    public GameDataContainer() {
        // inizializzo con il timestamp corrente
        this.lastUpdated = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new GameDataContainer with specified values.
     * 
     * @param dtm the Document-Term Matrix
     * @param documents list of document contents
     * @param stopwords set of stopwords
     */
    public GameDataContainer(DocumentTermMatrix dtm, List<String> documents, Set<String> stopwords) {
        // inizializzo con i valori forniti
        this.documentTermMatrix = dtm;
        this.documents = documents;
        this.stopwords = stopwords;
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public DocumentTermMatrix getDocumentTermMatrix() {
        return documentTermMatrix;
    }
    
    public void setDocumentTermMatrix(DocumentTermMatrix documentTermMatrix) {
        this.documentTermMatrix = documentTermMatrix;
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public List<String> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<String> documents) {
        this.documents = documents;
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public Set<String> getStopwords() {
        return stopwords;
    }
    
    public void setStopwords(Set<String> stopwords) {
        this.stopwords = stopwords;
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void updateTimestamp() {
        this.lastUpdated = System.currentTimeMillis();
    }
}

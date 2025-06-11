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
    
    public GameDataContainer() {
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public GameDataContainer(DocumentTermMatrix dtm, List<String> documents, Set<String> stopwords) {
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

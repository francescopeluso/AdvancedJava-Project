package wordageddon.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import wordageddon.model.DocumentTermMatrix;

import java.io.*;

/**
 * JavaFX Service for saving and loading DocumentTermMatrix in the background.
 * This service handles serialization operations without blocking the UI thread.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class DocumentTermMatrixPersistenceService extends Service<Boolean> {

    public enum Operation {
        SAVE, LOAD
    }

    private final Operation operation;
    private final DocumentTermMatrix dtm;
    private final String filePath;
    private DocumentTermMatrix loadedDtm;

    // NOTA: faccio overload del costruttore
    // - se il servizio deve salvare, passo la DTM e il percorso del file
    // - se il servizio deve caricare, passo solo il percorso del file

    /**
     * Constructs a new DocumentTermMatrixPersistenceService for saving.
     * 
     * @param dtm the Document-Term Matrix to save
     * @param filePath the file path where to save the DTM
     */
    public DocumentTermMatrixPersistenceService(DocumentTermMatrix dtm, String filePath) {
        this.operation = Operation.SAVE;
        this.dtm = dtm;
        this.filePath = filePath;
    }

    /**
     * Constructs a new DocumentTermMatrixPersistenceService for loading.
     * 
     * @param filePath the file path from where to load the DTM
     */
    public DocumentTermMatrixPersistenceService(String filePath) {
        this.operation = Operation.LOAD;
        this.dtm = null;
        this.filePath = filePath;
    }

    /**
     * Gets the loaded DocumentTermMatrix (only valid after a successful LOAD operation).
     * 
     * @return the loaded DTM or null if not loaded
     */
    public DocumentTermMatrix getLoadedDtm() {
        return loadedDtm;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {  // uso Task per eseguire operazioni in background, restituirà un valore booleano

            @Override
            protected Boolean call() throws Exception {
                if (operation == Operation.SAVE) {
                    return performSave();
                } else {
                    return performLoad();
                }
            }

            // esegue il salvataggio della DTM su un file
            private Boolean performSave() throws IOException {
                updateMessage("Salvataggio DTM...");
                updateProgress(0, 100);

                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                    updateProgress(30, 100);
                    
                    oos.writeObject(dtm);
                    
                    updateProgress(80, 100);
                    updateMessage("Verifica integrità file...");
                    
                    // verifica che il file sia stato scritto correttamente
                    File savedFile = new File(filePath);
                    if (!savedFile.exists() || savedFile.length() == 0) {
                        throw new IOException("Il file non è stato salvato correttamente");
                    }
                    
                    updateProgress(100, 100);
                    updateMessage("Document-Term Matrix salvata con successo!");
                    
                    return true;
                }
            }

            // esegue il caricamento della DTM da un file
            private Boolean performLoad() throws IOException, ClassNotFoundException {
                updateMessage("Caricamento DTM...");
                updateProgress(0, 100);

                File file = new File(filePath);
                if (!file.exists()) {
                    throw new FileNotFoundException("File non trovato: " + filePath);
                }

                updateProgress(20, 100);

                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                    updateProgress(50, 100);
                    updateMessage("Deserializzazione dati...");
                    
                    Object loaded = ois.readObject();
                    if (!(loaded instanceof DocumentTermMatrix)) {
                        throw new IOException("Il file non contiene una Document-Term Matrix valida");
                    }
                    
                    loadedDtm = (DocumentTermMatrix) loaded;
                    
                    updateProgress(90, 100);
                    updateMessage("Validazione dati caricati...");
                    
                    // validazione di base
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
}

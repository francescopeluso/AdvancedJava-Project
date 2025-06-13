package wordageddon.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import wordageddon.model.GameEngine;
import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.TextAnalysisService;
import wordageddon.model.Question;
import wordageddon.model.Answer;
import wordageddon.model.GameSession;
import wordageddon.service.GameInitializationService;
import wordageddon.service.DocumentLoadingService;
import wordageddon.service.GameIntegrationService;
import wordageddon.service.DocumentServices;
import wordageddon.service.QuestionGeneratorService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Controller class for managing the Wordageddon game flow.
 * Handles difficulty selection, document reading, questions, and results.
 */
public class GameController {

    /** Root container for all game views */
    @FXML private StackPane rootStack;

    // vista 1: selezione difficoltà
    /** Container for the difficulty selection view */
    @FXML private VBox difficultyPane;
    /** Radio button for easy difficulty selection */
    @FXML private RadioButton easyRadio;
    /** Radio button for medium difficulty selection */
    @FXML private RadioButton mediumRadio;
    /** Radio button for hard difficulty selection */
    @FXML private RadioButton hardRadio;
    /** Toggle group for difficulty selection radio buttons */
    @FXML private ToggleGroup difficultyToggleGroup;

    // vista 2: gameplay (fase lettura documenti)
    /** Main container for the gameplay view */
    @FXML private BorderPane gameplayPane;
    /** Label displaying the reading timer countdown */
    @FXML private Label timerLabel;
    /** Container holding all document text areas */
    @FXML private HBox documentsContainer;
    /** Text area for displaying the first document */
    @FXML private TextArea doc1;
    /** Text area for displaying the second document */
    @FXML private TextArea doc2;
    /** Text area for displaying the third document */
    @FXML private TextArea doc3;
    /** Button to indicate user is ready to start questions */
    @FXML private Button readyButton;

    // vista 3: domande
    /** Container for the question view */
    @FXML private VBox questionPane;
    /** Label showing the current question number */
    @FXML private Label questionNumberLabel;
    /** Label displaying the current question text */
    @FXML private Label questionLabel;
    /** First multiple-choice option */
    @FXML private RadioButton option1;
    /** Second multiple-choice option */
    @FXML private RadioButton option2;
    /** Third multiple-choice option */
    @FXML private RadioButton option3;
    /** Fourth multiple-choice option */
    @FXML private RadioButton option4;
    /** Toggle group for multiple-choice options */
    @FXML private ToggleGroup optionsToggleGroup;
    
    // vista 4: risultati
    /** Container for the results view */
    @FXML private VBox resultsPane;
    /** Label displaying the final score percentage */
    @FXML private Label scoreLabel;
    /** Label showing the final congratulatory or encouraging message */
    @FXML private Label finalMessageLabel;
    /** Label showing detailed score breakdown */
    @FXML private Label detailedScoreLabel;
    /** Table view for question review */
    @FXML private TableView<Answer> questionReviewTable;
    /** Question number column */
    @FXML private TableColumn<Answer, Integer> questionNumberColumn;
    /** Question text column */
    @FXML private TableColumn<Answer, String> questionTextColumn;
    /** User answer column */
    @FXML private TableColumn<Answer, String> userAnswerColumn;
    /** Correct answer column */
    @FXML private TableColumn<Answer, String> correctAnswerColumn;
    /** Score column */
    @FXML private TableColumn<Answer, Double> scoreColumn;

    // componenti principali del gioco
    /** Main game engine handling game logic and state */
    private GameEngine gameEngine;
    /** Document-Term Matrix for text analysis */
    private DocumentTermMatrix dtm;
    /** Service for analyzing text documents */
    private TextAnalysisService textAnalysisService;
    /** List of document names visible in current game session */
    private List<String> visibleDocuments;
    /** List of document contents for display */
    private List<String> documentContents;
    /** List of documents selected for the current game based on difficulty */
    private List<String> currentGameDocuments;
    
    // Services for asynchronous operations
    /** Service for game initialization */
    private GameInitializationService gameInitializationService;
    /** Service for document loading */
    private DocumentLoadingService documentLoadingService;
    /** Service for database integration */
    private GameIntegrationService gameIntegrationService;
    
    // variabili di stato del gioco
    /** Current question number (0-based index) */
    private int currentQuestionNumber = 0;
    /** Total number of questions for current difficulty */
    private int totalQuestions = 0;
    /** Current game session managing questions and answers */
    private GameSession currentGameSession;
    /** Timeline for the document reading timer */
    private Timeline readingTimer;
    
    // Tracking for question uniqueness
    /** Set to track used frequency question combinations (document + word) */
    private Set<String> usedFrequencyQuestions;
    /** Set to track used most-frequent-word question documents */
    private Set<String> usedMostFrequentQuestions;
    
    /** Service for generating diverse question types */
    private QuestionGeneratorService questionGenerator;

    /**
     * Initializes the game controller by setting up services for asynchronous operations.
     * Called automatically by JavaFX after loading the FXML file.
     */
    @FXML
    public void initialize() {
        // inizializza le colonne della tableview
        initializeTableView();
        
        // inizializza i servizi
        gameIntegrationService = new GameIntegrationService();
        
        // Inizializza i Set per tracciare le domande utilizzate
        usedFrequencyQuestions = new HashSet<>();
        usedMostFrequentQuestions = new HashSet<>();
        
        // Inizializza il gioco in modo asincrono
        initializeGameAsync();
        
        // Mostra la schermata di selezione difficoltà
        showDifficultyView();
    }

    /**
     * Performs asynchronous game initialization using JavaFX Services.
     */
    private void initializeGameAsync() {
        // Crea e configura il service per l'inizializzazione del gioco
        gameInitializationService = new GameInitializationService(new File("stopwords.txt"));
        
        // Gestisce il completamento dell'inizializzazione
        gameInitializationService.setOnSucceeded(event -> {
            GameInitializationService.GameInitializationResult result = 
                gameInitializationService.getValue();
            
            if (result.isSuccess()) {
                textAnalysisService = result.getTextAnalysisService();
                
                // Ora inizializza il caricamento dei documenti
                initializeDocumentLoadingAsync();
            } else {
                handleInitializationError(result.getErrorMessage());
            }
        });
        
        // Gestisce gli errori durante l'inizializzazione
        gameInitializationService.setOnFailed(event -> {
            Throwable exception = gameInitializationService.getException();
            handleInitializationError("Errore durante l'inizializzazione: " + 
                (exception != null ? exception.getMessage() : "Errore sconosciuto"));
        });
        
        // Avvia il service
        gameInitializationService.start();
    }
    
    /**
     * Performs asynchronous document loading using JavaFX Services.
     */
    private void initializeDocumentLoadingAsync() {
        // Usa la DTM da DocumentServices invece di crearne una nuova
        DocumentServices documentServices = new DocumentServices();
        
        // Ottieni la DTM già generata con le stopwords corrette
        DocumentTermMatrix adminDtm = documentServices.getDocumentTermMatrix();
        List<String> adminDocuments = documentServices.getDocuments();
        
        if (adminDtm != null && adminDocuments != null && !adminDocuments.isEmpty()) {
            // Usa direttamente la DTM e i documenti dall'admin service
            dtm = adminDtm;
            
            // Effettua una copia profonda della lista di documenti per poter manipolarla
            documentContents = new ArrayList<>(adminDocuments);
            
            // Crea i nomi dei documenti generici
            visibleDocuments = new ArrayList<>();
            for (int i = 0; i < Math.min(adminDocuments.size(), 3); i++) {
                visibleDocuments.add("document_" + (i + 1));
            }
            
            // Inizializza il GameEngine con la DTM e i documenti visibili
            gameEngine = new GameEngine(dtm, visibleDocuments);
            
            // Inizializza il generatore di domande
            questionGenerator = new QuestionGeneratorService(dtm, visibleDocuments);
            
            
        } else {
            // Fallback al sistema precedente se l'admin service non ha dati
            initializeDocumentLoadingAsyncFallback();
        }
    }
    
    /**
     * Fallback method for document loading when admin service has no data.
     */
    private void initializeDocumentLoadingAsyncFallback() {
        // Crea e configura il service per il caricamento dei documenti
        File documentsDir = new File("data/documents/"); // Use persistent directory
        documentLoadingService = new DocumentLoadingService(textAnalysisService, documentsDir, 3);
        
        // Gestisce il completamento del caricamento
        documentLoadingService.setOnSucceeded(event -> {
            DocumentLoadingService.DocumentLoadingResult result = 
                documentLoadingService.getValue();
            
            dtm = result.getDtm();
            visibleDocuments = result.getVisibleDocuments();
            documentContents = result.getDocumentContents();
            
            // Inizializza il GameEngine con la DTM e i documenti visibili
            gameEngine = new GameEngine(dtm, visibleDocuments);
            
            // Inizializza il generatore di domande
            questionGenerator = new QuestionGeneratorService(dtm, visibleDocuments);
            
        });
        
        // Gestisce gli errori durante il caricamento
        documentLoadingService.setOnFailed(event -> {
            Throwable exception = documentLoadingService.getException();
            handleInitializationError("Errore durante il caricamento dei documenti: " + 
                (exception != null ? exception.getMessage() : "Errore sconosciuto"));
        });
        
        // Avvia il service
        documentLoadingService.start();
    }

    /**
     * Initializes the TableView columns for question review.
     * Sets up cell value factories and row coloring based on answer correctness.
     */
    private void initializeTableView() {
        // configura le factory dei valori delle colonne usando i metodi di Answer e Question
        questionNumberColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getQuestion().getQuestionNumber()).asObject());
        questionTextColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getQuestion().getQuestionText()));
        userAnswerColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSelectedAnswerText()));
        correctAnswerColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getQuestion().getCorrectAnswerText()));
        scoreColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getScoreContribution()).asObject());
        
        // aggiunge colorazione delle righe basata sulla correttezza della risposta
        questionReviewTable.setRowFactory(tv -> {
            TableRow<Answer> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else if (newItem.isCorrect()) {
                    row.setStyle("-fx-background-color: #d4edda;");
                } else {
                    row.setStyle("-fx-background-color: #f8d7da;");
                }
            });
            return row;
        });
    }

    /**
     * Handles the start game action when user clicks the start button.
     * Configures the game based on selected difficulty and initiates the game flow.
     * 
     * Difficulty settings:
     * - Easy (Facile): 3 questions, 1 document
     * - Medium (Medio): 5 questions, 2 documents  
     * - Hard (Difficile): 10 questions, 3 documents
     * 
     * @param event the action event triggered by clicking start game button
     */
    @FXML
    private void onStartGame(ActionEvent event) {
        String difficulty = getSelectedDifficulty();
        if (difficulty == null) return;
        
        // verifica che il gioco sia correttamente inizializzato
        if (!isGameInitialized()) {
            System.err.println("Errore: Gioco non inizializzato correttamente");
            return;
        }

        int numberOfDocuments;
        
        // imposta il numero di domande e documenti basato sulla difficoltà
        switch (difficulty.toLowerCase()) {
            case "facile":
                totalQuestions = 3;
                numberOfDocuments = 1;
                break;
            case "medio":
                totalQuestions = 5;
                numberOfDocuments = 2;
                break;
            case "difficile":
                totalQuestions = 10;
                numberOfDocuments = 3;
                break;
            default:
                totalQuestions = 3;
                numberOfDocuments = 1;
        }

        // inizia il gioco con la difficoltà selezionata
        gameEngine.startGame(difficulty);
        
        // prepara i documenti per la difficoltà corrente
        prepareDocumentsForDifficulty(numberOfDocuments);
        
        // genera tutte le domande in anticipo
        generateAllQuestions(difficulty);

        // reset game state
        currentQuestionNumber = 0;
        // Remove the deprecated correctAnswers counter since GameSession handles scoring

        showGameplayView();

        // avvia il timer per la lettura (30 secondi)
        startReadingTimer();
    }

    /**
     * Handles the ready button action when user indicates they are ready to start questions.
     * Stops the reading timer and immediately begins the question phase.
     * 
     * @param event the action event triggered by clicking the ready button
     */
    @FXML
    private void onReady(ActionEvent event) {
        // l'utente è pronto, ferma il timer e inizia le domande
        if (readingTimer != null) {
            readingTimer.stop();
        }
        startQuestionPhase();
    }

    /**
     * Handles navigation back to the main menu/dashboard.
     * Resets the current game state and loads the DashboardView.
     * 
     * @param event the action event triggered by clicking back to menu button
     */
    @FXML
    private void onBackToMenu(ActionEvent event) {
        try {
            // reset game state
            resetGame();
            
            // carico la vista della dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wordageddon/view/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();
            
            // ottengo la finestra corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // creo una nuova scena con la vista della dashboard
            Scene dashboardScene = new Scene(dashboardRoot);
            
            // cambio la scena dello stage
            stage.setScene(dashboardScene);
            stage.setTitle("Wordageddon - Dashboard");
            
            
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // fallback: mostra solo la schermata di selezione difficoltà
            showDifficultyView();
        }
    }

    /**
     * Handles answer submission for multiple-choice questions.
     * Evaluates the selected answer, updates the score, and progresses to the next question
     * or shows results if all questions have been answered.
     * 
     * @param event the action event triggered by submitting an answer
     */
    @FXML
    private void onSubmitAnswer(ActionEvent event) {
        RadioButton selectedOption = (RadioButton) optionsToggleGroup.getSelectedToggle();
        if (selectedOption == null) return;

        // determina quale opzione è stata selezionata (0-3)
        int selectedIndex = -1;
        if (selectedOption == option1) selectedIndex = 0;
        else if (selectedOption == option2) selectedIndex = 1;
        else if (selectedOption == option3) selectedIndex = 2;
        else if (selectedOption == option4) selectedIndex = 3;

        // sottometti la risposta alla sessione di gioco
        if (currentGameSession != null) {
            currentGameSession.submitAnswer(currentQuestionNumber, selectedIndex);
        }

        currentQuestionNumber++;

        // controlla se ci sono altre domande
        if (currentQuestionNumber < totalQuestions) {
            showNextQuestion();
        } else {
            showResultsView();
        }
    }

    /**
     * Shows the gameplay view where users read documents within the time limit.
     * Hides all other views and makes the gameplay pane visible.
     */
    private void showGameplayView() {
        difficultyPane.setVisible(false);
        gameplayPane.setVisible(true);
        questionPane.setVisible(false);
        resultsPane.setVisible(false);
    }

    /**
     * Shows the question view with the current question and multiple-choice options.
     * Updates the question number display and question text.
     * 
     * @param question the question text to display
     */
    private void showQuestionView(String question) {
        difficultyPane.setVisible(false);
        gameplayPane.setVisible(false);
        questionPane.setVisible(true);
        resultsPane.setVisible(false);
        
        questionLabel.setText(question);
        questionNumberLabel.setText(String.format("Domanda %d di %d", currentQuestionNumber + 1, totalQuestions));
    }

    /**
     * Shows the difficulty selection view.
     * Hides all other views and makes the difficulty pane visible.
     */
    private void showDifficultyView() {
        difficultyPane.setVisible(true);
        gameplayPane.setVisible(false);
        questionPane.setVisible(false);
        resultsPane.setVisible(false);
    }

    /**
     * Gets the currently selected difficulty from the radio button group.
     * 
     * @return the text of the selected difficulty radio button, or null if none selected
     */
    private String getSelectedDifficulty() {
        RadioButton selected = (RadioButton) difficultyToggleGroup.getSelectedToggle();
        return selected != null ? selected.getText() : null;
    }

    /**
     * Resets the game state to initial conditions.
     * Recreates the GameEngine with original data and clears all UI elements.
     * This method is called when starting a new game or returning to menu.
     */
    // metodo per resettare lo stato del gioco
    private void resetGame() {
        if (gameEngine != null && dtm != null && visibleDocuments != null) {
            gameEngine = new GameEngine(dtm, visibleDocuments);
        }
        
        // reset del generatore di domande
        if (questionGenerator != null && dtm != null && visibleDocuments != null) {
            questionGenerator = new QuestionGeneratorService(dtm, visibleDocuments);
            questionGenerator.resetTracking();
        }
        
        // pulisce tutti gli elementi UI
        clearDocumentViews();
        clearQuestionView();
        resetTimer();
        clearTableView();
        
        // reset game session
        currentGameSession = null;
        
        // reset question tracking sets (per retrocompatibilità)
        if (usedFrequencyQuestions != null) {
            usedFrequencyQuestions.clear();
        }
        if (usedMostFrequentQuestions != null) {
            usedMostFrequentQuestions.clear();
        }
    }
    
    /**
     * Clears all document text areas by removing their content.
     * Used when resetting the game state.
     */
    // metodo per pulire le viste dei documenti
    private void clearDocumentViews() {
        doc1.clear();
        doc2.clear();
        doc3.clear();
    }
    
    /**
     * Clears the question view by resetting question text and deselecting all options.
     * Used when resetting the game state.
     */
    // metodo per pulire la vista delle domande
    private void clearQuestionView() {
        questionLabel.setText("");
        // deseleziona tutte le opzioni
        if (optionsToggleGroup.getSelectedToggle() != null) {
            optionsToggleGroup.getSelectedToggle().setSelected(false);
        }
    }
    
    /**
     * Resets the timer display to initial state (00:00).
     * Used when resetting the game state.
     */
    // metodo per resettare il timer
    private void resetTimer() {
        timerLabel.setText("00:00");
    }
    
    /**
     * Clears the table view by removing all data.
     * Used when resetting the game state.
     */
    private void clearTableView() {
        if (questionReviewTable != null) {
            questionReviewTable.getItems().clear();
        }
    }
    
    /**
     * Populates the question review table with data from the current game session.
     * Uses Answer objects directly instead of creating separate row objects.
     */
    private void populateQuestionReviewTable() {
        if (currentGameSession == null || questionReviewTable == null) {
            return;
        }
        
        // usa direttamente la lista di Answer dal GameSession
        List<Answer> answers = currentGameSession.getAnswers();
        ObservableList<Answer> tableData = FXCollections.observableArrayList(answers);
        
        // imposta i dati nella tabella
        questionReviewTable.setItems(tableData);
    }
    
    /**
     * Verifies that all required game components are properly initialized.
     * 
     * @return true if GameEngine, DTM, visible documents, and document contents are all initialized and non-empty
     */
    // metodo per verificare se il gioco è inizializzato correttamente
    private boolean isGameInitialized() {
        return gameEngine != null && dtm != null && 
               visibleDocuments != null && !visibleDocuments.isEmpty() &&
               documentContents != null && !documentContents.isEmpty();
    }
    
    /**
     * Handles initialization errors by creating fallback dummy data.
     * This prevents application crashes when document loading fails.
     * 
     * @param message the error message to log
     */
    // metodo per gestire gli errori di inizializzazione
    private void handleInitializationError(String message) {
        System.err.println("Errore di inizializzazione: " + message);
        
        // Inizializzazione di emergenza con valori di default
        dtm = new DocumentTermMatrix();
        visibleDocuments = new ArrayList<>();
        documentContents = new ArrayList<>();
        
        // Aggiungi documenti dummy per evitare crash
        visibleDocuments.add("documento_dummy.txt");
        documentContents.add("Contenuto di esempio per test");
        
        gameEngine = new GameEngine(dtm, visibleDocuments);
        questionGenerator = new QuestionGeneratorService(dtm, visibleDocuments);
    }
    
    /**
     * Prepares and displays the appropriate number of documents based on selected difficulty.
     * Adjusts document container layout and visibility dynamically.
     * Selects documents randomly for each game session.
     * 
     * Document visibility by difficulty:
     * - Easy: 1 document
     * - Medium: 2 documents
     * - Hard: 3 documents
     * 
     * @param numberOfDocuments the number of documents to display (1-3)
     */
    // Metodo per preparare i documenti in base alla difficoltà
    private void prepareDocumentsForDifficulty(int numberOfDocuments) {
        // Pulisci tutte le TextArea prima
        doc1.clear();
        doc2.clear();
        doc3.clear();
        
        // Nascondi tutte le TextArea e i loro container
        doc1.setVisible(false);
        doc2.setVisible(false);
        doc3.setVisible(false);
        
        // Ottieni i VBox contenitori dei documenti
        VBox doc1Container = (VBox) doc1.getParent();
        VBox doc2Container = (VBox) doc2.getParent();
        VBox doc3Container = (VBox) doc3.getParent();
        
        // Nascondi tutti i container
        doc1Container.setVisible(false);
        doc1Container.setManaged(false);
        doc2Container.setVisible(false);
        doc2Container.setManaged(false);
        doc3Container.setVisible(false);
        doc3Container.setManaged(false);
        
        // Ottieni i parametri per il calcolo della larghezza
        double containerWidth = documentsContainer.getPrefWidth();
        if (containerWidth <= 0) {
            containerWidth = 850; // Larghezza di default se non specificata
        }
        
        // Configura HBox.hgrow per ogni container per permettere il ridimensionamento
        HBox.setHgrow(doc1Container, Priority.ALWAYS);
        HBox.setHgrow(doc2Container, Priority.ALWAYS);
        HBox.setHgrow(doc3Container, Priority.ALWAYS);
        
        // Creiamo una copia mescolata dei documenti disponibili per garantire selezione casuale
        List<String> shuffledDocumentContents = new ArrayList<>(documentContents);
        List<String> shuffledVisibleDocuments = new ArrayList<>(visibleDocuments);
        
        // Assicurati che gli indici corrispondano
        if (shuffledDocumentContents.size() == shuffledVisibleDocuments.size()) {
            // Crea una lista di indici da mescolare
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < shuffledDocumentContents.size(); i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);
            
            // Usa gli indici mescolati per creare le liste randomizzate
            List<String> randomizedContents = new ArrayList<>();
            List<String> randomizedNames = new ArrayList<>();
            
            for (int index : indices) {
                randomizedContents.add(shuffledDocumentContents.get(index));
                randomizedNames.add(shuffledVisibleDocuments.get(index));
            }
            
            shuffledDocumentContents = randomizedContents;
            shuffledVisibleDocuments = randomizedNames;
        } else {
            // Se le dimensioni non corrispondono, mescola solo i contenuti
            Collections.shuffle(shuffledDocumentContents);
        }
        
        // Mostra e carica solo il numero di documenti necessari
        for (int i = 0; i < numberOfDocuments && i < shuffledDocumentContents.size(); i++) {
            switch (i) {
                case 0:
                    doc1.setText(shuffledDocumentContents.get(0));
                    doc1.setVisible(true);
                    doc1Container.setVisible(true);
                    doc1Container.setManaged(true);
                    break;
                case 1:
                    doc2.setText(shuffledDocumentContents.get(1));
                    doc2.setVisible(true);
                    doc2Container.setVisible(true);
                    doc2Container.setManaged(true);
                    break;
                case 2:
                    doc3.setText(shuffledDocumentContents.get(2));
                    doc3.setVisible(true);
                    doc3Container.setVisible(true);
                    doc3Container.setManaged(true);
                    break;
            }
        }
        
        // Aggiorna i documenti per la partita corrente
        currentGameDocuments = new ArrayList<>();
        for (int i = 0; i < numberOfDocuments && i < shuffledVisibleDocuments.size(); i++) {
            currentGameDocuments.add(shuffledVisibleDocuments.get(i));
        }
        
        // Aggiorna il GameEngine con i documenti correnti
        gameEngine = new GameEngine(dtm, currentGameDocuments);
        
        // Aggiorna il generatore di domande con i documenti correnti
        if (questionGenerator != null) {
            questionGenerator = new QuestionGeneratorService(dtm, currentGameDocuments);
        }
        
    }
    
    /**
     * Starts the document reading timer with a 30-second countdown.
     * Shows the "Ready" button after 10 seconds to allow early completion.
     * Automatically starts the question phase when timer expires.
     */
    // Metodo per avviare il timer di lettura
    private void startReadingTimer() {
        final int READING_TIME_SECONDS = 30;
        final int READY_BUTTON_DELAY = 10; // Mostra il pulsante dopo 10 secondi
        
        readingTimer = new Timeline();
        readingTimer.setCycleCount(READING_TIME_SECONDS + 1);
        
        final IntegerProperty timeLeft = new SimpleIntegerProperty(READING_TIME_SECONDS);
        
        timerLabel.setText("Tempo rimasto: " + timeLeft.get() + "s");
        readyButton.setVisible(false); // Nascondi il pulsante inizialmente
        
        readingTimer.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), e -> {
                timeLeft.set(timeLeft.get() - 1);
                timerLabel.setText("Tempo rimasto: " + timeLeft.get() + "s");
                
                // Mostra il pulsante "Sono Pronto!" dopo READY_BUTTON_DELAY secondi
                if (timeLeft.get() <= READING_TIME_SECONDS - READY_BUTTON_DELAY) {
                    readyButton.setVisible(true);
                }
                
                if (timeLeft.get() <= 0) {
                    readingTimer.stop();
                    startQuestionPhase();
                }
            })
        );
        
        readingTimer.play();
    }
    
    /**
     * Initiates the question phase by resetting the question counter and showing the first question.
     */
    // Metodo per iniziare la fase delle domande
    private void startQuestionPhase() {
        currentQuestionNumber = 0;
        showFirstQuestion();
    }
    
    /**
     * Shows the first question in the question sequence.
     * Updates the question view and displays multiple-choice options.
     */
    // Metodo per mostrare la prima domanda
    private void showFirstQuestion() {
        if (currentGameSession != null && currentGameSession.getQuestions() != null && !currentGameSession.getQuestions().isEmpty()) {
            Question question = currentGameSession.getQuestions().get(currentQuestionNumber);
            showQuestionView(question.getQuestionText());
            updateQuestionOptions();
        }
    }
    
    /**
     * Advances to and shows the next question in the sequence.
     * Updates the question view and displays corresponding multiple-choice options.
     */
    // Metodo per mostrare la prossima domanda
    private void showNextQuestion() {
        if (currentGameSession != null && currentQuestionNumber < currentGameSession.getQuestions().size()) {
            Question question = currentGameSession.getQuestions().get(currentQuestionNumber);
            showQuestionView(question.getQuestionText());
            updateQuestionOptions();
        }
    }
    
    /**
     * Updates the radio button options with the choices for the current question.
     * Clears any previous selections and sets new option text.
     */
    // Metodo per aggiornare le opzioni della domanda corrente
    private void updateQuestionOptions() {
        if (currentGameSession != null && currentQuestionNumber < currentGameSession.getQuestions().size()) {
            Question question = currentGameSession.getQuestions().get(currentQuestionNumber);
            List<String> options = question.getOptions();
            
            option1.setText(options.size() > 0 ? options.get(0) : "");
            option2.setText(options.size() > 1 ? options.get(1) : "");
            option3.setText(options.size() > 2 ? options.get(2) : "");
            option4.setText(options.size() > 3 ? options.get(3) : "");
            
            // Deseleziona tutte le opzioni
            if (optionsToggleGroup.getSelectedToggle() != null) {
                optionsToggleGroup.getSelectedToggle().setSelected(false);
            }
        }
    }
    
    /**
     * Generates all questions for the current game session.
     * Creates a mix of various question types:
     * - Absolute frequency: how many times a word appears in a document
     * - Relative frequency comparison: comparing frequencies of different words
     * - Document-specific word association: which document contains a specific word
     * - Exclusion questions: which word never appears in a document
     * 
     * The number of questions generated depends on the selected difficulty level.
     * Questions are not repeated within the same session.
     */
    private void generateAllQuestions(String difficulty) {
        // Aggiorna la configurazione del generatore di domande
        if (questionGenerator == null) {
            System.err.println("Error: QuestionGenerator non inizializzato. Creazione di emergenza.");
            questionGenerator = new QuestionGeneratorService(dtm, currentGameDocuments != null ? 
                currentGameDocuments : visibleDocuments);
        } else {
            // Aggiorna i documenti del generatore per assicurarsi siano corretti
            questionGenerator = new QuestionGeneratorService(dtm, currentGameDocuments);
        }
        
        List<Question> questions = new ArrayList<>();
        questionGenerator.resetTracking();
        
        
        // Generazione domande di varie tipologie
        for (int i = 0; i < totalQuestions; i++) {
            // Genera una domanda di tipo casuale usando il service
            Question question = questionGenerator.generateRandomQuestion(i + 1);
            if (question != null) {
                questions.add(question);
            } else {
                System.err.println("Failed to generate question " + (i + 1));
            }
        }
        
        
        // Crea una nuova sessione di gioco con le domande generate
        currentGameSession = new GameSession(difficulty, questions);
    }
    
    /**
     * Shows the final results view with score, percentage, and feedback message.
     * Calculates the final score percentage and provides appropriate feedback based on performance:
     * - 80%+ : "Eccellente! Hai una ottima comprensione dei testi!"
     * - 60-79%: "Buon lavoro! Puoi ancora migliorare."
     * - <60%  : "Continua a praticare per migliorare le tue capacità di analisi testuale."
     * Also automatically populates the question review table and saves the session to database.
     */
    private void showResultsView() {
        difficultyPane.setVisible(false);
        gameplayPane.setVisible(false);
        questionPane.setVisible(false);
        resultsPane.setVisible(true);
        
        if (currentGameSession != null) {
            // calcola il punteggio finale e la percentuale
            double finalScore = currentGameSession.getTotalScore();
            int totalQuestions = currentGameSession.getQuestions().size();
            int correctCount = currentGameSession.getCorrectAnswersCount();
            double percentage = (double) correctCount / totalQuestions * 100;
            
            scoreLabel.setText(String.format("Punteggio: %.2f/%.0f (%.1f%%)", 
                finalScore, (double)totalQuestions, percentage));
            
            // determina il messaggio di feedback basato sulla percentuale
            String message;
            if (percentage >= 80) {
                message = "Eccellente! Hai una ottima comprensione dei testi!";
            } else if (percentage >= 60) {
                message = "Buon lavoro! Puoi ancora migliorare.";
            } else {
                message = "Continua a praticare per migliorare le tue capacità di analisi testuale.";
            }
            
            finalMessageLabel.setText(message);
            detailedScoreLabel.setText(String.format("Risposte corrette: %d su %d", correctCount, totalQuestions));
            
            // popola la tabella di revisione delle domande
            populateQuestionReviewTable();
            
            // salva la sessione di gioco nel database
            if (currentGameSession.isCompleted()) {
                try {
                    int sessionId = gameIntegrationService.saveCurrentUserGameSession(currentGameSession);
                    if (sessionId > 0) {
                    } else {
                        System.err.println("Errore nel salvare la sessione di gioco");
                    }
                } catch (Exception e) {
                    System.err.println("Errore nel salvare la sessione di gioco: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handles the new game action by resetting the game state and returning to difficulty selection.
     * This allows the user to start a fresh game without returning to the main menu.
     * 
     * @param event the action event triggered by clicking the new game button
     */
    @FXML
    private void onNewGame(ActionEvent event) {
        // Reset del gioco e torna alla selezione difficoltà
        resetGame();
        showDifficultyView();
    }
    
    /**
     * Handles the show question review action by displaying detailed question results.
     * Always populates the question review TableView with user answers and correct answers.
     * 
     * @param event the action event triggered by clicking the show details button
     */
    @FXML
    private void onShowQuestionReview(ActionEvent event) {
        populateQuestionReviewTable();
    }
}
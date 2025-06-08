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

import wordageddon.model.GameEngine;
import wordageddon.model.DocumentTermMatrix;
import wordageddon.model.TextAnalysisService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Controller class for managing the Wordageddon game flow and user interactions.
 * This controller handles the complete game lifecycle including difficulty selection,
 * document reading phase, question generation and display, and results presentation.
 * 
 * The game consists of four main phases:
 * 1. Difficulty Selection - User chooses between Easy (Facile), Medium (Medio), or Hard (Difficile)
 * 2. Document Reading - User reads selected documents within a time limit
 * 3. Question Phase - User answers multiple-choice questions about the documents
 * 4. Results - User sees their score and feedback
 * 
 * @author Wordageddon Team
 * @version 1.0
 * @since 2024
 */
public class GameController {

    /** Root container for all game views */
    @FXML private StackPane rootStack;

    // View 1: Difficulty Selection (Difficoltà)
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

    // View 2: Gameplay (Document Reading Phase)
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

    // View 3: Questions (Domande)
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
    
    // View 4: Results (Risultati)
    /** Container for the results view */
    @FXML private VBox resultsPane;
    /** Label displaying the final score percentage */
    @FXML private Label scoreLabel;
    /** Label showing the final congratulatory or encouraging message */
    @FXML private Label finalMessageLabel;
    /** Label showing detailed score breakdown */
    @FXML private Label detailedScoreLabel;

    // Core game components
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
    
    // Game state variables
    /** Current question number (0-based index) */
    private int currentQuestionNumber = 0;
    /** Total number of questions for current difficulty */
    private int totalQuestions = 0;
    /** Number of correctly answered questions */
    private int correctAnswers = 0;
    /** List of generated question texts */
    private List<String> questions;
    /** List of multiple-choice options for each question */
    private List<List<String>> questionOptions;
    /** List of correct answer indices for each question */
    private List<Integer> correctAnswerIndices;
    /** Timeline for the document reading timer */
    private javafx.animation.Timeline readingTimer;

    /**
     * Initializes the game controller by setting up the text analysis service,
     * loading documents, and creating the Document-Term Matrix.
     * 
     * This method is automatically called by JavaFX after loading the FXML file.
     * It performs the following operations:
     * 1. Initializes the TextAnalysisService with stopwords
     * 2. Loads and randomly selects up to 3 documents from the documents directory
     * 3. Creates a Document-Term Matrix for the selected documents
     * 4. Initializes the GameEngine with the processed data
     * 
     * If any error occurs during initialization, it creates fallback dummy data
     * to prevent application crashes.
     */
    @FXML
    public void initialize() {
        try {
            // inizializzo il servizio di analisi del testo
            textAnalysisService = new TextAnalysisService();
            textAnalysisService.loadStopwords(new File("stopwords.txt"));
            
            // prima ottengo tutti i documenti disponibili
            File documentsDir = new File("documents/");
            File[] files = documentsDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files == null || files.length == 0) {
                throw new IOException("Nessun documento trovato nella directory documents/");
            }
            
            // seleziono 3 documenti a caso
            List<File> allFiles = new ArrayList<>();
            for (File file : files) {
                allFiles.add(file);
            }
            Collections.shuffle(allFiles);
            
            int numDocuments = Math.min(3, allFiles.size());
            List<File> selectedFiles = allFiles.subList(0, numDocuments);
            
            // creo la DTM solo per i documenti selezionati
            dtm = new DocumentTermMatrix();
            for (File file : selectedFiles) {
                textAnalysisService.processDocument(dtm, file);
            }
            
            // salvo i nomi dei documenti visibili
            visibleDocuments = new ArrayList<>();
            documentContents = new ArrayList<>();
            
            for (File file : selectedFiles) {
                String fileName = file.getName();
                visibleDocuments.add(fileName);
                
                // carico il contenuto per visualizzarlo
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    documentContents.add(content);
                } catch (IOException e) {
                    documentContents.add("Errore nel caricamento del documento " + fileName);
                }
            }
            
            // inizializzo il GameEngine con la DTM e i documenti visibili
            gameEngine = new GameEngine(dtm, visibleDocuments);
            
        } catch (IOException e) {
            e.printStackTrace();
            
            // inizializzazione di fallback
            handleInitializationError(e.getMessage());
        }
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
        
        // Verifica che il gioco sia correttamente inizializzato
        if (!isGameInitialized()) {
            System.err.println("Errore: Gioco non inizializzato correttamente");
            return;
        }

        int numberOfDocuments;
        
        // Imposta il numero di domande e documenti basato sulla difficoltà
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
        
        // Prepara i documenti per la difficoltà corrente
        prepareDocumentsForDifficulty(numberOfDocuments);
        
        // Genera tutte le domande in anticipo
        generateAllQuestions();

        // Reset game state
        currentQuestionNumber = 0;
        correctAnswers = 0;

        showGameplayView();

        // Avvia il timer per la lettura (30 secondi)
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
        // L'utente è pronto, ferma il timer e inizia le domande
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
            // Reset game state
            resetGame();
            
            // Carico la vista della dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wordageddon/view/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();
            
            // Ottengo la finestra corrente
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Creo una nuova scena con la vista della dashboard
            Scene dashboardScene = new Scene(dashboardRoot);
            
            // Cambio la scena dello stage
            stage.setScene(dashboardScene);
            stage.setTitle("Wordageddon - Dashboard");
            
            System.out.println("Tornato alla dashboard");
            
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: mostra solo la schermata di selezione difficoltà
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

        // Determina quale opzione è stata selezionata (0-3)
        int selectedIndex = -1;
        if (selectedOption == option1) selectedIndex = 0;
        else if (selectedOption == option2) selectedIndex = 1;
        else if (selectedOption == option3) selectedIndex = 2;
        else if (selectedOption == option4) selectedIndex = 3;

        // Verifica se la risposta è corretta
        boolean correct = (selectedIndex == correctAnswerIndices.get(currentQuestionNumber));
        if (correct) {
            correctAnswers++;
        }

        currentQuestionNumber++;

        // Controlla se ci sono altre domande
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
    // Metodo per resettare lo stato del gioco
    private void resetGame() {
        if (gameEngine != null && dtm != null && visibleDocuments != null) {
            gameEngine = new GameEngine(dtm, visibleDocuments);
        }
        
        // Clear all UI elements
        clearDocumentViews();
        clearQuestionView();
        resetTimer();
    }
    
    /**
     * Clears all document text areas by removing their content.
     * Used when resetting the game state.
     */
    // Metodo per pulire le viste dei documenti
    private void clearDocumentViews() {
        doc1.clear();
        doc2.clear();
        doc3.clear();
    }
    
    /**
     * Clears the question view by resetting question text and deselecting all options.
     * Used when resetting the game state.
     */
    // Metodo per pulire la vista delle domande
    private void clearQuestionView() {
        questionLabel.setText("");
        // Deseleziona tutte le opzioni
        if (optionsToggleGroup.getSelectedToggle() != null) {
            optionsToggleGroup.getSelectedToggle().setSelected(false);
        }
    }
    
    /**
     * Resets the timer display to initial state (00:00).
     * Used when resetting the game state.
     */
    // Metodo per resettare il timer
    private void resetTimer() {
        timerLabel.setText("00:00");
    }
    
    /**
     * Verifies that all required game components are properly initialized.
     * 
     * @return true if GameEngine, DTM, visible documents, and document contents are all initialized and non-empty
     */
    // Metodo per verificare se il gioco è inizializzato correttamente
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
    // Metodo per gestire gli errori di inizializzazione
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
    }
    
    /**
     * Prepares and displays the appropriate number of documents based on selected difficulty.
     * Adjusts document container layout and visibility dynamically.
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
        
        // Nascondi tutte le TextArea
        doc1.setVisible(false);
        doc2.setVisible(false);
        doc3.setVisible(false);
        
        // Aggiusta la larghezza delle TextArea in base al numero di documenti
        double containerWidth = 750; // Larghezza approssimativa del container
        double spacing = 15;
        double margin = 20;
        double availableWidth = containerWidth - (numberOfDocuments - 1) * spacing - margin * 2;
        double docWidth = availableWidth / numberOfDocuments;
        
        // Mostra e carica solo il numero di documenti necessari
        for (int i = 0; i < numberOfDocuments && i < documentContents.size(); i++) {
            switch (i) {
                case 0:
                    doc1.setText(documentContents.get(0));
                    doc1.setVisible(true);
                    doc1.setPrefWidth(docWidth);
                    break;
                case 1:
                    doc2.setText(documentContents.get(1));
                    doc2.setVisible(true);
                    doc2.setPrefWidth(docWidth);
                    break;
                case 2:
                    doc3.setText(documentContents.get(2));
                    doc3.setVisible(true);
                    doc3.setPrefWidth(docWidth);
                    break;
            }
        }
        
        // Aggiorna i documenti per la partita corrente
        currentGameDocuments = new ArrayList<>();
        for (int i = 0; i < numberOfDocuments && i < visibleDocuments.size(); i++) {
            currentGameDocuments.add(visibleDocuments.get(i));
        }
        
        // Aggiorna il GameEngine con i documenti correnti
        gameEngine = new GameEngine(dtm, currentGameDocuments);
        
        System.out.println("Documenti preparati per difficoltà: " + numberOfDocuments + " documenti visibili");
        System.out.println("Documenti correnti: " + currentGameDocuments);
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
        
        readingTimer = new javafx.animation.Timeline();
        readingTimer.setCycleCount(READING_TIME_SECONDS + 1);
        
        final javafx.beans.property.IntegerProperty timeLeft = new javafx.beans.property.SimpleIntegerProperty(READING_TIME_SECONDS);
        
        timerLabel.setText("Tempo rimasto: " + timeLeft.get() + "s");
        readyButton.setVisible(false); // Nascondi il pulsante inizialmente
        
        readingTimer.getKeyFrames().add(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
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
        if (questions != null && !questions.isEmpty()) {
            showQuestionView(questions.get(currentQuestionNumber));
            updateQuestionOptions();
        }
    }
    
    /**
     * Advances to and shows the next question in the sequence.
     * Updates the question view and displays corresponding multiple-choice options.
     */
    // Metodo per mostrare la prossima domanda
    private void showNextQuestion() {
        if (currentQuestionNumber < questions.size()) {
            showQuestionView(questions.get(currentQuestionNumber));
            updateQuestionOptions();
        }
    }
    
    /**
     * Updates the radio button options with the choices for the current question.
     * Clears any previous selections and sets new option text.
     */
    // Metodo per aggiornare le opzioni della domanda corrente
    private void updateQuestionOptions() {
        if (questionOptions != null && currentQuestionNumber < questionOptions.size()) {
            List<String> options = questionOptions.get(currentQuestionNumber);
            
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
     * Creates a mix of frequency questions and most-frequent-word questions.
     * The number of questions generated depends on the selected difficulty level.
     */
    // Metodo per generare tutte le domande
    private void generateAllQuestions() {
        questions = new ArrayList<>();
        questionOptions = new ArrayList<>();
        correctAnswerIndices = new ArrayList<>();
        
        for (int i = 0; i < totalQuestions; i++) {
            // Genera una domanda casuale
            if (Math.random() < 0.5) {
                generateFrequencyQuestion();
            } else {
                generateMostFrequentWordQuestion();
            }
        }
    }
    
    /**
     * Generates a frequency-based question asking about word occurrence count.
     * Selects a random word from a random document and creates multiple-choice options
     * with the correct frequency and plausible alternatives.
     */
    // Metodo per generare una domanda sulla frequenza
    private void generateFrequencyQuestion() {
        if (currentGameDocuments == null || currentGameDocuments.isEmpty()) {
            return;
        }
        
        String doc = currentGameDocuments.get((int)(Math.random() * currentGameDocuments.size()));
        java.util.Map<String, Integer> termFreq = dtm.getTermsForDocument(doc);
        
        if (!termFreq.isEmpty()) {
            java.util.List<String> words = new java.util.ArrayList<>(termFreq.keySet());
            String word = words.get((int)(Math.random() * words.size()));
            int correctFreq = termFreq.get(word);
            
            String question = String.format("Quante volte compare la parola \"%s\" nel documento \"%s\"?", word, doc);
            questions.add(question);
            
            // Genera opzioni multiple
            java.util.Set<Integer> options = new java.util.HashSet<>();
            options.add(correctFreq);
            
            while (options.size() < 4) {
                int variation = (int)(Math.random() * 5) + 1;
                int option = Math.random() < 0.5 ? correctFreq + variation : Math.max(0, correctFreq - variation);
                options.add(option);
            }
            
            java.util.List<String> optionStrings = new java.util.ArrayList<>();
            java.util.List<Integer> optionsList = new java.util.ArrayList<>(options);
            java.util.Collections.shuffle(optionsList);
            
            int correctIndex = -1;
            for (int i = 0; i < optionsList.size(); i++) {
                optionStrings.add(String.valueOf(optionsList.get(i)));
                if (optionsList.get(i) == correctFreq) {
                    correctIndex = i;
                }
            }
            
            questionOptions.add(optionStrings);
            correctAnswerIndices.add(correctIndex);
        }
    }
    
    /**
     * Generates a most-frequent-word question asking which word appears most often.
     * Selects a random document and creates multiple-choice options with the correct
     * most frequent word and other words from the same document as alternatives.
     */
    // Metodo per generare una domanda sulla parola più frequente
    private void generateMostFrequentWordQuestion() {
        if (currentGameDocuments == null || currentGameDocuments.isEmpty()) {
            return;
        }
        
        String doc = currentGameDocuments.get((int)(Math.random() * currentGameDocuments.size()));
        java.util.Map<String, Integer> termFreq = dtm.getTermsForDocument(doc);
        
        if (!termFreq.isEmpty()) {
            String correctWord = termFreq.entrySet().stream()
                    .max(java.util.Map.Entry.comparingByValue())
                    .map(java.util.Map.Entry::getKey).orElse("");
            
            String question = String.format("Quale parola compare più spesso nel documento \"%s\"?", doc);
            questions.add(question);
            
            // Genera opzioni multiple con parole casuali dal documento
            java.util.Set<String> options = new java.util.HashSet<>();
            options.add(correctWord);
            
            java.util.List<String> allWords = new java.util.ArrayList<>(termFreq.keySet());
            java.util.Collections.shuffle(allWords);
            
            for (String word : allWords) {
                if (options.size() >= 4) break;
                options.add(word);
            }
            
            java.util.List<String> optionsList = new java.util.ArrayList<>(options);
            java.util.Collections.shuffle(optionsList);
            
            int correctIndex = optionsList.indexOf(correctWord);
            
            questionOptions.add(optionsList);
            correctAnswerIndices.add(correctIndex);
        }
    }
    
    /**
     * Shows the final results view with score, percentage, and feedback message.
     * Calculates the final score percentage and provides appropriate feedback:
     * - 80%+ : "Eccellente! Hai una ottima comprensione dei testi!"
     * - 60-79%: "Buon lavoro! Puoi ancora migliorare."
     * - <60%  : "Continua a praticare per migliorare le tue capacità di analisi testuale."
     */
    // Metodo per mostrare i risultati finali
    private void showResultsView() {
        difficultyPane.setVisible(false);
        gameplayPane.setVisible(false);
        questionPane.setVisible(false);
        resultsPane.setVisible(true);
        
        double percentage = (double) correctAnswers / totalQuestions * 100;
        scoreLabel.setText(String.format("Punteggio: %d/%d (%.1f%%)", correctAnswers, totalQuestions, percentage));
        
        String message;
        if (percentage >= 80) {
            message = "Eccellente! Hai una ottima comprensione dei testi!";
        } else if (percentage >= 60) {
            message = "Buon lavoro! Puoi ancora migliorare.";
        } else {
            message = "Continua a praticare per migliorare le tue capacità di analisi testuale.";
        }
        
        finalMessageLabel.setText(message);
        detailedScoreLabel.setText(String.format("Risposte corrette: %d su %d", correctAnswers, totalQuestions));
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
}
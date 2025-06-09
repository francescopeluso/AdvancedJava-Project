package wordageddon.model;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enumeration representing the different difficulty levels in the game.
 * Each difficulty affects the number of questions and documents used.
 */
enum Difficulty {
    /** Easy difficulty level */
    FACILE, 
    /** Medium difficulty level */
    MEDIO, 
    /** Hard difficulty level */
    DIFFICILE
}

/**
 * Core game engine that manages the Wordageddon game logic and state.
 * 
 * This class handles:
 * - Game initialization with different difficulty levels
 * - Question generation based on document term frequencies
 * - Answer validation and scoring
 * - Game state management
 * 
 * The GameEngine works with a DocumentTermMatrix to analyze text documents
 * and generate meaningful questions about word frequencies and distributions.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class GameEngine {

    /** The Document-Term Matrix containing analyzed text data */
    private final DocumentTermMatrix dtm;

    /** List of document names available for the current game session */
    private final List<String> documents;

    /** Set of all unique terms across all documents */
    private final Set<String> allTerms;

    /** Random number generator for question selection */
    private final Random random = new Random();
    
    // campi di stato del gioco
    /** Current difficulty level selected by the player */
    private String currentDifficulty;

    /** The current question text being asked */
    private String currentQuestion;

    /** The correct answer to the current question */
    private String currentAnswer;

    /** Flag indicating whether a game session is currently active */
    private boolean gameStarted = false;

    /**
     * Constructs a new GameEngine with the specified document data.
     * 
     * @param dtm the Document-Term Matrix containing processed document data
     * @param visibleDocuments list of document names that should be used in the game
     */
    public GameEngine(DocumentTermMatrix dtm, List<String> visibleDocuments) {
        this.dtm = dtm;
        this.documents = new ArrayList<>(visibleDocuments);
        this.allTerms = dtm.getAllTerms();
    }
    
    /**
     * Starts a new game session with the specified difficulty level.
     * Initializes game state and generates the first question.
     * 
     * @param difficulty the difficulty level ("facile", "medio", or "difficile")
     */
    public void startGame(String difficulty) {
        this.currentDifficulty = difficulty;
        this.gameStarted = true;
        generateNewQuestion();
    }
    
    /**
     * Retrieves the Document-Term Matrix used by this game engine.
     * 
     * @return the DocumentTermMatrix containing the analyzed document data
     */
    public DocumentTermMatrix getDocumentMatrix() {
        return this.dtm;
    }
    
    /**
     * Submits an answer for the current question and validates its correctness.
     * Automatically generates a new question after processing the answer.
     * 
     * @param answer the player's answer to the current question
     * @return true if the answer is correct, false otherwise
     */
    public boolean submitAnswer(String answer) {
        if (!gameStarted || currentAnswer == null) {
            return false;
        }
        
        boolean isCorrect = currentAnswer.equalsIgnoreCase(answer.trim());
        
        // genera la prossima domanda dopo aver risposto
        generateNewQuestion();
        
        return isCorrect;
    }
    
    /**
     * Gets the current question text.
     * 
     * @return the current question being asked, or null if no question is active
     */
    public String getCurrentQuestion() {
        return currentQuestion;
    }
    
    /**
     * Generates a new question randomly choosing between frequency and most frequent word questions.
     * This is called internally after each answer submission or game start.
     */
    private void generateNewQuestion() {
        // sceglie casualmente tra diversi tipi di domande
        if (random.nextBoolean()) {
            generateFrequencyQuestionWithAnswer();
        } else {
            generateMostFrequentWordQuestionWithAnswer();
        }
    }
    
    /**
     * Generates a word frequency question and sets the correct answer.
     * Asks how many times a specific word appears in a randomly selected document.
     */
    private void generateFrequencyQuestionWithAnswer() {
        String doc = getRandomDocument();
        String word = getRandomWordInDocument(doc);
        int freq = dtm.getTermsForDocument(doc).getOrDefault(word, 0);
        
        this.currentAnswer = String.valueOf(freq);
        this.currentQuestion = String.format("Quante volte compare la parola \"%s\" nel documento \"%s\"?", word, doc);
    }
    
    /**
     * Generates a most frequent word question and sets the correct answer.
     * Asks which word appears most frequently in a randomly selected document.
     */
    private void generateMostFrequentWordQuestionWithAnswer() {
        String doc = getRandomDocument();
        Map<String, Integer> termFreq = dtm.getTermsForDocument(doc);

        String correct = termFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("???");
        
        this.currentAnswer = correct;
        this.currentQuestion = String.format("Quale parola compare più spesso nel documento \"%s\"?", doc);
    }

    /**
     * Selects a random document from the available documents list.
     * 
     * @return a randomly selected document name
     */
    private String getRandomDocument() {
        return documents.get(random.nextInt(documents.size()));
    }

    /**
     * Selects a random word from the specified document.
     * 
     * @param document the document from which to select a word
     * @return a randomly selected word from the document
     */
    private String getRandomWordInDocument(String document) {
        List<String> words = new ArrayList<>(dtm.getTermsForDocument(document).keySet());
        return words.get(random.nextInt(words.size()));
    }

    /**
     * Generates multiple choice options for numeric answers.
     * Creates plausible alternatives around the correct answer.
     * 
     * @param correct the correct numeric answer
     * @return a list of 4 integer options including the correct answer
     */
    private List<Integer> generateNumericOptions(int correct) {
        Set<Integer> options = new HashSet<>();
        options.add(correct);
        while (options.size() < 4) {
            int variation = random.nextInt(5) + 1;
            int option = random.nextBoolean() ? correct + variation : Math.max(0, correct - variation);
            options.add(option);
        }
        return new ArrayList<>(options);
    }

    /**
     * Generates a frequency question for testing purposes.
     * This method is used in the main method for demonstration.
     * 
     * @return the generated frequency question text
     */
    public String generateFrequencyQuestion() {
        generateFrequencyQuestionWithAnswer();
        return currentQuestion;
    }
    
    /**
     * Generates a most frequent word question for testing purposes.
     * This method is used in the main method for demonstration.
     * 
     * @return the generated most frequent word question text
     */
    public String generateMostFrequentWordQuestion() {
        generateMostFrequentWordQuestionWithAnswer();
        return currentQuestion;
    }

    /**
     * Main method for testing the GameEngine functionality.
     * Demonstrates question generation using sample documents.
     * 
     * @param args command line arguments (not used)
     * @throws IOException if there are issues reading documents or stopwords
     */
    public static void main(String[] args) throws IOException {
        DocumentTermMatrix dtm = new DocumentTermMatrix();
        TextAnalysisService tas = new TextAnalysisService();

        tas.loadStopwords(new File("stopwords.txt"));
        tas.processDocuments(dtm, new File("documents/"));

        // passo solo due documenti
        List<String> visibleDocs = dtm.getDocuments().stream().limit(2).collect(Collectors.toList());
        System.out.println("Documenti presi in carico: " + visibleDocs);
        GameEngine engine = new GameEngine(dtm, visibleDocs);

        System.out.println("\n\n== TEST DOMANDE ==");
        System.out.println(engine.generateFrequencyQuestion());
        System.out.println();
        System.out.println(engine.generateMostFrequentWordQuestion());
    }
}

package wordageddon.model;

import java.util.*;

/**
 * Enumeration representing the different difficulty levels in the Wordageddon game.
 * 
 * Each difficulty level affects the number of questions generated, the complexity
 * of document analysis, and the number of documents processed during gameplay.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
enum Difficulty {
    /** Easy difficulty level - fewer questions and simpler analysis */
    FACILE, 
    /** Medium difficulty level - moderate questions and analysis complexity */
    MEDIO, 
    /** Hard difficulty level - more questions and complex analysis */
    DIFFICILE
}

/**
 * Core game engine that manages the Wordageddon game logic and state.
 * 
 * This class serves as the central coordinator for the game mechanics, handling:
 * - Game initialization with configurable difficulty levels
 * - Document processing and term-frequency analysis
 * - Question generation based on document content analysis
 * - Answer validation and real-time scoring
 * - Game session state management and progression tracking
 * 
 * The GameEngine integrates with DocumentTermMatrix for text analysis,
 * QuestionGeneratorService for creating diverse question types, and provides
 * a complete game experience from start to finish.
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
}

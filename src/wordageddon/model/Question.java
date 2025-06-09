package wordageddon.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a question in the Wordageddon game with multiple choice options.
 * 
 * This class encapsulates all the information about a single question including
 * the question text, the available multiple choice options, and the index of
 * the correct answer.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class Question {
    
    /** The question text displayed to the user */
    private final String questionText;
    
    /** List of multiple choice options (typically 4 options) */
    private final List<String> options;
    
    /** The index (0-based) of the correct answer in the options list */
    private final int correctAnswerIndex;
    
    /** Question number in the game sequence */
    private final int questionNumber;
    
    /**
     * Constructs a new Question with the specified parameters.
     * 
     * @param questionNumber the sequential number of this question in the game
     * @param questionText the text of the question to be displayed
     * @param options the list of multiple choice options
     * @param correctAnswerIndex the index of the correct answer (0-based)
     * @throws IllegalArgumentException if options is null/empty or correctAnswerIndex is invalid
     */
    public Question(int questionNumber, String questionText, List<String> options, int correctAnswerIndex) {
        
        // valudazione parametro option (lista delle risposte)
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Array vuoto o null non consentito per le opzioni di risposta");
        }

        // validazione parametro questionText (testo della domanda)
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("Il testo della domanda non può essere null o vuoto");
        }
        
        // validazione parametro correctAnswerIndex (indice della risposta corretta)
        if (correctAnswerIndex < 0 || correctAnswerIndex >= options.size()) {
            throw new IllegalArgumentException("Indice della risposta corretta non valido (out of bounds)");
        }
        
        // inizializzazione dei campi dell'istanza della classe
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));  // creo una copia immutabile per evitare modifiche esterne
        this.correctAnswerIndex = correctAnswerIndex;
    }
    
    /**
     * Gets the question number.
     * 
     * @return the sequential number of this question
     */
    public int getQuestionNumber() {
        return questionNumber;
    }
    
    /**
     * Gets the question text.
     * 
     * @return the text of the question
     */
    public String getQuestionText() {
        return questionText;
    }
    
    /**
     * Gets the list of multiple choice options.
     * 
     * @return an immutable list of option strings
     */
    public List<String> getOptions() {
        return options;
    }
    
    /**
     * Gets the index of the correct answer.
     * 
     * @return the index of the correct answer in the options list
     */
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
    
    /**
     * Gets the text of the correct answer.
     * 
     * @return the text of the correct answer option
     */
    public String getCorrectAnswerText() {
        return options.get(correctAnswerIndex);
    }
    
    /**
     * Checks if the provided answer index is correct.
     * 
     * @param answerIndex the index of the selected answer
     * @return true if the answer is correct, false otherwise
     */
    public boolean isCorrectAnswer(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }
    
    /**
     * Gets the text of the option at the specified index.
     * 
     * @param index the index of the option to retrieve
     * @return the text of the option at the given index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public String getOptionText(int index) {
        return options.get(index);
    }
    
    @Override
    public String toString() {
        return String.format("Domanda %d: %s (risposta corretta: %s)", questionNumber, questionText, getCorrectAnswerText());
    }
}

package wordageddon.model;

/**
 * Represents a user's answer to a question in the Wordageddon game.
 * 
 * This class stores the user's selected answer along with information about
 * whether it was correct and the associated question. It's used for tracking
 * game progress and calculating scores.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class Answer {
    
    /** The question this answer corresponds to */
    private final Question question;
    
    /** The index of the option selected by the user (0-based) */
    private final int selectedAnswerIndex;
    
    /** Whether this answer is correct */
    private final boolean isCorrect;
    
    /** The timestamp when this answer was submitted */
    private final long timestamp;
    
    /**
     * Constructs a new Answer with the specified parameters.
     * 
     * @param question the question being answered
     * @param selectedAnswerIndex the index of the selected answer option (0-based)
     * @throws IllegalArgumentException if question is null or selectedAnswerIndex is invalid
     */
    public Answer(Question question, int selectedAnswerIndex) {

        // se la domanda è null sollevo eccezione
        if (question == null) {
            throw new IllegalArgumentException("La domanda non può essere null");
        }

        // se l'indice della risposta selezionata è out of bounds dell'array delle risposte possibili, idem
        if (selectedAnswerIndex < 0 || selectedAnswerIndex >= question.getOptions().size()) {
            throw new IllegalArgumentException("Indice della risposta selezionata non valido (out of bounds)");
        }
        
        // inizializzazione dei campi dell'istanza della classe
        this.question = question;
        this.selectedAnswerIndex = selectedAnswerIndex;
        this.isCorrect = question.isCorrectAnswer(selectedAnswerIndex);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the question this answer corresponds to.
     * 
     * @return the associated question
     */
    public Question getQuestion() {
        return question;
    }
    
    /**
     * Gets the index of the selected answer.
     * 
     * @return the index of the selected answer option
     */
    public int getSelectedAnswerIndex() {
        return selectedAnswerIndex;
    }
    
    /**
     * Gets the text of the selected answer.
     * 
     * @return the text of the selected answer option
     */
    public String getSelectedAnswerText() {
        return question.getOptionText(selectedAnswerIndex);
    }
    
    /**
     * Checks if this answer is correct.
     * 
     * @return true if the answer is correct, false otherwise
     */
    public boolean isCorrect() {
        return isCorrect;
    }
    
    /**
     * Gets the timestamp when this answer was submitted.
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Calculates the score contribution of this answer.
     * Correct answers contribute +1.0, incorrect answers contribute -0.33.
     * 
     * @return the score contribution (1.0 for correct, -0.33 for incorrect)
     */
    public double getScoreContribution() {
        return isCorrect ? 1.0 : -0.33;
    }
    
    @Override
    public String toString() {
        return String.format("Risposta a domanda %d: %s (rispota utente: %s) - %s", 
            question.getQuestionNumber(),
            question.getQuestionText(),
            getSelectedAnswerText(),
            isCorrect ? "CORRECT" : "INCORRECT");
    }
}

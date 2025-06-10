package wordageddon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete game session in Wordageddon.
 * 
 * This class manages the entire game session including questions, answers,
 * scoring, and session metadata. It provides methods to track progress,
 * calculate scores, and generate detailed reports.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class GameSession {
    
    /** The difficulty level of this game session */
    private final String difficulty;
    
    /** The timestamp when the game session started */
    private final long startTime;
    
    /** The list of questions for this session */
    private final List<Question> questions;
    
    /** The list of answers submitted by the user */
    private final List<Answer> answers;
    
    /** The timestamp when the game session ended (null if not finished) */
    private Long endTime;
    
    /** Whether the game session is completed */
    private boolean isCompleted;
    
    /**
     * Constructs a new GameSession with the specified difficulty and questions.
     * 
     * @param difficulty the difficulty level ("Facile", "Medio", "Difficile")
     * @param questions the list of questions for this session
     * @throws IllegalArgumentException if difficulty is null/empty or questions is null/empty
     */
    public GameSession(String difficulty, List<Question> questions) {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            throw new IllegalArgumentException("Difficulty cannot be null or empty");
        }
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Questions cannot be null or empty");
        }
        
        this.difficulty = difficulty;
        this.questions = Collections.unmodifiableList(new ArrayList<>(questions)); // crea una copia immutabile
        this.answers = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.isCompleted = false;
    }
    
    /**
     * Gets the difficulty level of this session.
     * 
     * @return the difficulty level
     */
    public String getDifficulty() {
        return difficulty;
    }
    
    /**
     * Gets the start time of this session.
     * 
     * @return the start timestamp in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the end time of this session.
     * 
     * @return the end timestamp in milliseconds, or null if not finished
     */
    public Long getEndTime() {
        return endTime;
    }
    
    /**
     * Gets the duration of the session in milliseconds.
     * 
     * @return the session duration, or time elapsed so far if not finished
     */
    public long getDuration() {
        long end = endTime != null ? endTime : System.currentTimeMillis();
        return end - startTime;
    }
    
    /**
     * Gets the list of questions for this session.
     * 
     * @return an immutable list of questions
     */
    public List<Question> getQuestions() {
        return questions;
    }
    
    /**
     * Gets the list of answers submitted so far.
     * 
     * @return an immutable list of answers
     */
    public List<Answer> getAnswers() {
        return Collections.unmodifiableList(answers);
    }
    
    /**
     * Submits an answer for the current question.
     * 
     * @param questionIndex the index of the question being answered
     * @param selectedAnswerIndex the index of the selected answer option
     * @return the created Answer object
     * @throws IllegalArgumentException if indices are invalid
     * @throws IllegalStateException if session is completed or question already answered
     */
    public Answer submitAnswer(int questionIndex, int selectedAnswerIndex) {
        if (isCompleted) {
            throw new IllegalStateException("Cannot submit answers to a completed session");
        }
        if (questionIndex < 0 || questionIndex >= questions.size()) {
            throw new IllegalArgumentException("Invalid question index");
        }
        if (answers.size() != questionIndex) {
            throw new IllegalStateException("Questions must be answered in order");
        }
        
        Question question = questions.get(questionIndex);
        Answer answer = new Answer(question, selectedAnswerIndex);
        answers.add(answer);
        
        // controlla se questa era l'ultima domanda
        if (answers.size() == questions.size()) {
            completeSession();
        }
        
        return answer;
    }
    
    /**
     * Marks the session as completed and records the end time.
     */
    private void completeSession() {
        this.isCompleted = true;
        this.endTime = System.currentTimeMillis();
    }
    
    /**
     * Checks if the session is completed.
     * 
     * @return true if all questions have been answered
     */
    public boolean isCompleted() {
        return isCompleted;
    }
    
    /**
     * Gets the current question index (0-based).
     * 
     * @return the index of the next question to answer, or -1 if completed
     */
    public int getCurrentQuestionIndex() {
        return isCompleted ? -1 : answers.size();
    }
    
    /**
     * Gets the current question.
     * 
     * @return the next question to answer, or null if completed
     */
    public Question getCurrentQuestion() {
        int index = getCurrentQuestionIndex();
        return (index >= 0 && index < questions.size()) ? questions.get(index) : null;
    }
    
    /**
     * Calculates the total score for this session.
     * Score = (correct answers * 1.0) + (incorrect answers * -0.33)
     * 
     * @return the total score
     */
    public double getTotalScore() {
        return answers.stream()
                .mapToDouble(Answer::getScoreContribution)
                .sum();
    }
    
    /**
     * Gets the number of correct answers.
     * 
     * @return the count of correct answers
     */
    public int getCorrectAnswersCount() {
        return (int) answers.stream()
                .filter(Answer::isCorrect)
                .count();
    }
    
    /**
     * Gets the number of incorrect answers.
     * 
     * @return the count of incorrect answers
     */
    public int getIncorrectAnswersCount() {
        return answers.size() - getCorrectAnswersCount();
    }
    
    /**
     * Calculates the percentage of correct answers.
     * 
     * @return the percentage (0-100) of correct answers
     */
    public double getPercentageScore() {
        if (questions.isEmpty()) return 0.0;
        return (double) getCorrectAnswersCount() / questions.size() * 100.0;
    }
    
    /**
     * Gets the maximum possible score for this session.
     * 
     * @return the maximum score (number of questions * 1.0)
     */
    public double getMaxPossibleScore() {
        return questions.size() * 1.0;
    }
    
    /**
     * Gets the minimum possible score for this session.
     * 
     * @return the minimum score (number of questions * -0.33)
     */
    public double getMinPossibleScore() {
        return questions.size() * -0.33;
    }
    
    /**
     * Generates a detailed summary of the session.
     * 
     * @return a formatted string with session statistics
     */
    public String getSessionSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Sessione di gioco - Difficoltà: %s%n", difficulty));
        summary.append(String.format("Durata: %.1f secondi%n", getDuration() / 1000.0));
        summary.append(String.format("Domande totali: %d%n", questions.size()));
        summary.append(String.format("Risposte corrette: %d%n", getCorrectAnswersCount()));
        summary.append(String.format("Risposte sbagliate: %d%n", getIncorrectAnswersCount()));
        summary.append(String.format("Percentuale: %.1f%%%n", getPercentageScore()));
        summary.append(String.format("Punteggio totale: %.2f/%.2f%n", getTotalScore(), getMaxPossibleScore()));
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return String.format("GameSession[difficulty=%s, questions=%d, answers=%d, completed=%s]",
            difficulty, questions.size(), answers.size(), isCompleted);
    }
}

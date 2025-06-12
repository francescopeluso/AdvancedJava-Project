package wordageddon.model;

import java.util.Date;

/**
 * Data Transfer Object representing game session statistics for leaderboard display.
 * 
 * This lightweight class provides essential game session information without the overhead
 * of the complete GameSession object. It's optimized for displaying leaderboard entries,
 * statistics, and session summaries in the UI.
 * 
 * The class contains aggregated data including scores, completion rates, and timing
 * information that can be efficiently retrieved from the database for reporting purposes.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class GameSessionSummary {
    /** Unique identifier for the game session */
    private int sessionId;
    
    /** Unique identifier for the user who played this session */
    private int userId;
    
    /** Difficulty level of the game session (FACILE, MEDIO, DIFFICILE) */
    private String difficulty;
    
    /** Final calculated score for the session */
    private int score;
    
    /** Language used during the game session */
    private String language;
    
    /** Timestamp when the session was created */
    private Date createdAt;
    
    /** Total number of questions in the session */
    private int totalQuestions;
    
    /** Number of correctly answered questions */
    private int correctAnswers;
    
    /** Duration of the game session in milliseconds */
    private long duration;

    /**
     * Constructs a new GameSessionSummary with the specified session data.
     * 
     * // crea un riepilogo con i dati essenziali della sessione di gioco
     *
     * @param sessionId the unique identifier of the session
     * @param userId the unique identifier of the user who played
     * @param difficulty the difficulty level (FACILE, MEDIO, DIFFICILE)
     * @param score the final calculated score
     * @param language the language used during the session
     * @param createdAt the timestamp when the session was created
     * @param totalQuestions the total number of questions asked
     * @param correctAnswers the number of questions answered correctly
     * @param duration the duration of the session in milliseconds
     */
    public GameSessionSummary(int sessionId, int userId, String difficulty, int score, 
                            String language, Date createdAt, int totalQuestions, 
                            int correctAnswers, long duration) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.difficulty = difficulty;
        this.score = score;
        this.language = language;
        this.createdAt = createdAt;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.duration = duration;
    }

    // getters
    public int getSessionId() { return sessionId; }
    public int getUserId() { return userId; }
    public String getDifficulty() { return difficulty; }
    public int getScore() { return score; }
    public String getLanguage() { return language; }
    public Date getCreatedAt() { return createdAt; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public long getDuration() { return duration; }

    /**
     * Calculates the percentage score.
     *
     * @return the percentage of correct answers
     */
    public double getPercentageScore() {
        if (totalQuestions == 0) return 0.0;
        return ((double) correctAnswers / totalQuestions) * 100.0;
    }

    /**
     * Returns the start time (same as created at for compatibility).
     *
     * @return the start time as timestamp
     */
    public long getStartTime() {
        return createdAt.getTime();
    }
}

package wordageddon.model;

import java.util.Date;

/**
 * Data Transfer Object for game session statistics in the leaderboard.
 * This class represents the essential data for displaying game sessions
 * without needing the full GameSession object with questions.
 */
public class GameSessionSummary {
    private int sessionId;
    private int userId;
    private String difficulty;
    private int score;
    private String language;
    private Date createdAt;
    private int totalQuestions;
    private int correctAnswers;
    private long duration;

    /**
     * Constructor for GameSessionSummary.
     *
     * @param sessionId the ID of the session
     * @param userId the ID of the user
     * @param difficulty the difficulty level
     * @param score the final score
     * @param language the language used
     * @param createdAt the creation date
     * @param totalQuestions the total number of questions
     * @param correctAnswers the number of correct answers
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

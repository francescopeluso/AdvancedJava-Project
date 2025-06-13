package wordageddon.service;

import wordageddon.dao.DAOFactory;
import wordageddon.dao.UserDAO;
import wordageddon.dao.GameSessionDAO;
import wordageddon.dao.AnswerDAO;
import wordageddon.model.GameSession;
import wordageddon.model.GameSessionSummary;
import wordageddon.model.UserLeaderboardEntry;
import wordageddon.model.Answer;
import java.util.List;
import java.util.Collections;

/**
 * Service for integrating game data with database operations.
 * Provides high-level methods for managing users, game sessions, and answers.
 */
public class GameIntegrationService {
    
    private final UserDAO userDAO;
    private final GameSessionDAO gameSessionDAO;
    private final AnswerDAO answerDAO;
    
    public GameIntegrationService() {
        this.userDAO = DAOFactory.getUserDAO();
        this.gameSessionDAO = DAOFactory.getGameSessionDAO();
        this.answerDAO = DAOFactory.getAnswerDAO();
    }
    
    /**
     * Saves a completed game session to the database.
     * 
     * @param userId the user ID (would need to be added to User model or resolved)
     * @param gameSession the completed game session
     * @return the session ID if saved successfully, -1 otherwise
     */
    public int saveGameSession(int userId, GameSession gameSession) {
        try {
            // calcola il punteggio finale mantenendo i decimali
            double finalScore = gameSession.getTotalScore();
            
            // salva la sessione di gioco
            int sessionId = gameSessionDAO.addGameSession(
                userId, 
                finalScore, 
                gameSession.getDifficulty(), 
                "italian" // lingua predefinita
            );
            
            if (sessionId > 0) {
                // salva tutte le risposte
                for (Answer answer : gameSession.getAnswers()) {
                    answerDAO.addAnswer(
                        sessionId,
                        answer.getQuestion().getQuestionText(),
                        answer.getSelectedAnswerText(),
                        answer.getQuestion().getCorrectAnswerText(),
                        answer.isCorrect()
                    );
                }
            }
            
            return sessionId;
        } catch (Exception e) {
            System.err.println("Error saving game session: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Updates user information.
     * 
     * @param username the username
     * @param firstName the new first name
     * @param lastName the new last name
     * @param email the new email
     * @param isAdmin whether the user is an admin
     * @return true if update is successful, false otherwise
     */
    public boolean updateUser(String username, String firstName, String lastName, String email, boolean isAdmin) {
        try {
            userDAO.updateUser(username, firstName, lastName, null, email, isAdmin);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves a completed game session for a specific user.
     * 
     * @param userId the user ID
     * @param gameSession the completed game session
     * @return the session ID if saved successfully, -1 otherwise
     */
    public int saveUserGameSession(int userId, GameSession gameSession) {
        return saveGameSession(userId, gameSession);
    }
    
    /**
     * Gets all game sessions for a specific user.
     * 
     * @param userId the user ID
     * @return list of game sessions, empty list if none found
     */
    public List<GameSession> getUserGameSessions(int userId) {
        try {
            return gameSessionDAO.getGameSessionsByUser(userId);
        } catch (Exception e) {
            System.err.println("Error retrieving user game sessions: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Gets game session summaries for a specific user.
     * This method returns lightweight summary objects instead of full GameSession objects.
     * 
     * @param userId the user ID
     * @return list of game session summaries, empty list if none found
     */
    public List<GameSessionSummary> getUserGameSessionSummaries(int userId) {
        try {
            return gameSessionDAO.getGameSessionSummariesByUser(userId);
        } catch (Exception e) {
            System.err.println("Error retrieving user game session summaries: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets the global leaderboard showing all users with their total points.
     * 
     * @return list of user leaderboard entries ordered by total points
     */    public List<UserLeaderboardEntry> getGlobalLeaderboard() {
        try {
            return gameSessionDAO.getGlobalLeaderboard();
        } catch (Exception e) {
            System.err.println("Error retrieving global leaderboard: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}

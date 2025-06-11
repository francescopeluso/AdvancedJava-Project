package wordageddon.service;

import wordageddon.dao.DAOFactory;
import wordageddon.dao.UserDAO;
import wordageddon.dao.GameSessionDAO;
import wordageddon.dao.AnswerDAO;
import wordageddon.dao.Database;
import wordageddon.model.User;
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
        // inizializza il database alla creazione del servizio
        Database.initializeDatabase();
        
        this.userDAO = DAOFactory.getUserDAO();
        this.gameSessionDAO = DAOFactory.getGameSessionDAO();
        this.answerDAO = DAOFactory.getAnswerDAO();
    }
    
    /**
     * Registers a new user.
     * 
     * @param username the username
     * @param firstName the first name
     * @param lastName the last name
     * @param password the password
     * @param email the email
     * @param isAdmin whether the user is an admin
     * @return true if registration is successful, false if user already exists
     */
    public boolean registerUser(String username, String firstName, String lastName, String password, String email, boolean isAdmin) {
        try {
            if (userDAO.userExists(username)) {
                return false;
            }
            
            userDAO.addUser(username, firstName, lastName, password, email, isAdmin);
            return true;
        } catch (Exception e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticates a user login using email and password.
     * 
     * @param email the user's email
     * @param password the password
     * @return the User object if authentication is successful, null otherwise
     */
    public User authenticateUser(String email, String password) {
        try {
            User user = userDAO.getUserByEmail(email);
            if (user != null && user.getPassword().equals(password)) {
                // imposta la sessione utente
                UserSession.getInstance().setCurrentUser(user);
                return user;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return null;
        }
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
            // calcola il punteggio finale
            int finalScore = (int) Math.round(gameSession.getTotalScore());
            
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
     * Checks if a username is available.
     * 
     * @param username the username to check
     * @return true if available, false if already taken
     */
    public boolean isUsernameAvailable(String username) {
        try {
            return !userDAO.userExists(username);
        } catch (Exception e) {
            System.err.println("Error checking username availability: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates user information.
     * 
     * @param username the username
     * @param firstName the new first name
     * @param lastName the new last name
     * @param password the new password
     * @param email the new email
     * @param isAdmin whether the user is an admin
     * @return true if update is successful, false otherwise
     */
    public boolean updateUser(String username, String firstName, String lastName, String password, String email, boolean isAdmin) {
        try {
            userDAO.updateUser(username, firstName, lastName, password, email, isAdmin);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Saves a completed game session for the current logged-in user.
     * 
     * @param gameSession the completed game session
     * @return the session ID if saved successfully, -1 otherwise
     */
    public int saveCurrentUserGameSession(GameSession gameSession) {
        UserSession userSession = UserSession.getInstance();
        if (!userSession.isLoggedIn()) {
            System.err.println("Cannot save game session: No user logged in");
            return -1;
        }
        
        return saveGameSession(userSession.getCurrentUserId(), gameSession);
    }
    
    /**
     * Gets all game sessions for the current logged-in user.
     * 
     * @return list of game sessions, empty list if none found or no user logged in
     */
    public List<GameSession> getCurrentUserGameSessions() {
        UserSession userSession = UserSession.getInstance();
        if (!userSession.isLoggedIn()) {
            return Collections.emptyList(); // lista vuota
        }
        
        try {
            return gameSessionDAO.getGameSessionsByUser(userSession.getCurrentUserId());
        } catch (Exception e) {
            System.err.println("Error retrieving user game sessions: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Gets game session summaries for the current logged-in user.
     * This method returns lightweight summary objects instead of full GameSession objects,
     * avoiding the "Questions cannot be null or empty" constructor issue.
     * 
     * @return list of game session summaries, empty list if none found or no user logged in
     */
    public List<GameSessionSummary> getCurrentUserGameSessionSummaries() {
        UserSession userSession = UserSession.getInstance();
        if (!userSession.isLoggedIn()) {
            return Collections.emptyList();
        }
        
        try {
            return gameSessionDAO.getGameSessionSummariesByUser(userSession.getCurrentUserId());
        } catch (Exception e) {
            System.err.println("Error retrieving user game session summaries: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets all game sessions for a specific user.
     * 
     * @param userId the user ID
     * @return list of game sessions for the user
     */
    public List<GameSession> getUserGameSessions(int userId) {
        try {
            return gameSessionDAO.getGameSessionsByUser(userId);
        } catch (Exception e) {
            System.err.println("Error retrieving game sessions for user " + userId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Gets the global leaderboard showing all users with their total points.
     * 
     * @return list of user leaderboard entries ordered by total points
     */
    public List<UserLeaderboardEntry> getGlobalLeaderboard() {
        try {
            return gameSessionDAO.getGlobalLeaderboard();
        } catch (Exception e) {
            System.err.println("Error retrieving global leaderboard: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}

package wordageddon.dao;

import wordageddon.model.GameSession;
import wordageddon.model.GameSessionSummary;
import java.util.List;

/**
 * GameSessionDAO interface for managing game session data in the database.
 * Provides basic CRUD operations for GameSession entities.
 */
public interface GameSessionDAO {

    /**
     * Adds a new game session to the database.
     *
     * @param userId the ID of the user playing
     * @param score the final score of the session
     * @param difficulty the difficulty level of the session
     * @param language the language used in the session
     * @return the ID of the created session
     */
    int addGameSession(int userId, int score, String difficulty, String language);

    /**
     * Retrieves a game session by its ID.
     *
     * @param sessionId the ID of the session
     * @return the GameSession object if found, null otherwise
     */
    GameSession getGameSession(int sessionId);

    /**
     * Retrieves all game sessions for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of game sessions for the user
     */
    List<GameSession> getGameSessionsByUser(int userId);

    /**
     * Updates the score of an existing game session.
     *
     * @param sessionId the ID of the session
     * @param score the new score
     */
    void updateSessionScore(int sessionId, int score);

    /**
     * Deletes a game session from the database.
     *
     * @param sessionId the ID of the session to delete
     */
    void deleteGameSession(int sessionId);

    /**
     * Retrieves all game sessions from the database.
     *
     * @return a list of all game sessions
     */
    List<GameSession> getAllGameSessions();

    /**
     * Counts the total number of game sessions in the database.
     *
     * @return the total number of game sessions
     */
    int countGameSessions();

    /**
     * Retrieves game session summaries for a specific user.
     * This method returns lightweight summary objects instead of full GameSession objects.
     *
     * @param userId the ID of the user
     * @return a list of game session summaries for the user
     */
    List<GameSessionSummary> getGameSessionSummariesByUser(int userId);

}

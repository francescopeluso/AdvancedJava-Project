package wordageddon.dao;

import wordageddon.dao.implementation.UserDAOSQLite;
import wordageddon.dao.implementation.AnswerDAOSQLite;
import wordageddon.dao.implementation.GameSessionDAOSQLite;

/**
 * Factory class for creating DAO instances.
 * Provides centralized access to all DAO implementations.
 * 
 * It is a good practice to use a factory pattern for creating DAO instances,
 * as it allows for better separation of concerns and easier testing.
 * 
 * This class follows the Singleton pattern to ensure that only one instance
 * of each DAO implementation is created throughout the application lifecycle.
 */
public class DAOFactory {
    
    // Singleton instances
    private static UserDAO userDAO;
    private static AnswerDAO answerDAO;
    private static GameSessionDAO gameSessionDAO;
    
    /**
     * Gets the UserDAO implementation.
     * 
     * @return the UserDAO instance
     */
    public static UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAOSQLite();
        }
        return userDAO;
    }
    
    /**
     * Gets the AnswerDAO implementation.
     * 
     * @return the AnswerDAO instance
     */
    public static AnswerDAO getAnswerDAO() {
        if (answerDAO == null) {
            answerDAO = new AnswerDAOSQLite();
        }
        return answerDAO;
    }
    
    /**
     * Gets the GameSessionDAO implementation.
     * 
     * @return the GameSessionDAO instance
     */
    public static GameSessionDAO getGameSessionDAO() {
        if (gameSessionDAO == null) {
            gameSessionDAO = new GameSessionDAOSQLite();
        }
        return gameSessionDAO;
    }
}

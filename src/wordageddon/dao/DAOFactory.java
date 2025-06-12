package wordageddon.dao;

import wordageddon.dao.implementation.UserDAOSQLite;
import wordageddon.dao.implementation.AnswerDAOSQLite;
import wordageddon.dao.implementation.GameSessionDAOSQLite;

/**
 * Factory class for creating and managing DAO instances.
 * 
 * This factory provides centralized access to all Data Access Object implementations
 * using the Factory and Singleton patterns. It ensures that only one instance of each
 * DAO implementation exists throughout the application lifecycle, providing better
 * resource management and consistency.
 * 
 * The factory pattern allows for better separation of concerns, easier testing,
 * and flexibility in changing DAO implementations without affecting client code.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class DAOFactory {
    
    // Singleton instances - garantisce una sola istanza per ogni DAO
    private static UserDAO userDAO;
    private static AnswerDAO answerDAO;
    private static GameSessionDAO gameSessionDAO;
    
    /**
     * Gets the UserDAO implementation instance.
     * 
     * // restituisce l'istanza singleton del dao per gli utenti
     * 
     * @return the singleton UserDAO instance for user data operations
     */
    public static UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAOSQLite(); // crea una nuova istanza solo se necessario
        }
        return userDAO;
    }
    
    /**
     * Gets the AnswerDAO implementation instance.
     * 
     * // restituisce l'istanza singleton del dao per le risposte
     * 
     * @return the singleton AnswerDAO instance for answer data operations
     */
    public static AnswerDAO getAnswerDAO() {
        if (answerDAO == null) {
            answerDAO = new AnswerDAOSQLite(); // crea una nuova istanza solo se necessario
        }
        return answerDAO;
    }
    
    /**
     * Gets the GameSessionDAO implementation instance.
     * 
     * // restituisce l'istanza singleton del dao per le sessioni di gioco
     * 
     * @return the singleton GameSessionDAO instance for game session data operations
     */
    public static GameSessionDAO getGameSessionDAO() {
        if (gameSessionDAO == null) {
            gameSessionDAO = new GameSessionDAOSQLite();
        }
        return gameSessionDAO;
    }
}

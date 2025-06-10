package wordageddon.dao;

import wordageddon.model.Answer;
import java.util.List;

/**
 * AnswerDAO interface for managing answer data in the database.
 * Provides basic CRUD operations for Answer entities.
 */
public interface AnswerDAO {

    /**
     * Adds a new answer to the database.
     *
     * @param sessionId the ID of the game session
     * @param questionText the text of the question
     * @param chosenAnswer the answer chosen by the user
     * @param correctAnswer the correct answer
     * @param isCorrect whether the answer is correct
     */
    void addAnswer(int sessionId, String questionText, String chosenAnswer, String correctAnswer, boolean isCorrect);

    /**
     * Retrieves all answers for a specific game session.
     *
     * @param sessionId the ID of the game session
     * @return a list of answers for the session
     */
    List<Answer> getAnswersBySession(int sessionId);

    /**
     * Deletes all answers for a specific game session.
     *
     * @param sessionId the ID of the game session
     */
    void deleteAnswersBySession(int sessionId);

}

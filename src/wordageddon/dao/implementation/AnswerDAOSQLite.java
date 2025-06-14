package wordageddon.dao.implementation;

import wordageddon.dao.AnswerDAO;
import wordageddon.dao.Database;
import wordageddon.model.Answer;
import wordageddon.model.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of the AnswerDAO interface for answer data persistence.
 * 
 * This class provides concrete implementation of all answer-related database operations
 * using SQLite as the underlying database. It handles:
 * - Storage of user answers for each game session
 * - Answer validation and correctness tracking
 * - Question-answer relationship management
 * - Answer retrieval for session analysis and review
 * 
 * Answers are stored with complete context including the original question,
 * user's chosen response, correct answer, and validation status.
 * This enables detailed game analysis and performance tracking.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class AnswerDAOSQLite implements AnswerDAO {

    @Override
    public void addAnswer(int sessionId, String questionText, String chosenAnswer, String correctAnswer, boolean isCorrect) {
        String sql = "INSERT INTO answers (session_id, question_text, chosen_answer, correct_answer, is_correct) VALUES (?, ?, ?, ?, ?)";
        
        // inserisce una nuova risposta nel database con tutti i dettagli
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sessionId);
            pstmt.setString(2, questionText);
            pstmt.setString(3, chosenAnswer);
            pstmt.setString(4, correctAnswer);
            pstmt.setInt(5, isCorrect ? 1 : 0); // converte boolean in int per sqlite
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding answer: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Answer> getAnswersBySession(int sessionId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT question_text, chosen_answer, correct_answer, is_correct FROM answers WHERE session_id = ? ORDER BY id";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                int questionNumber = 1;
                while (rs.next()) {
                    String questionText = rs.getString("question_text");
                    String chosenAnswer = rs.getString("chosen_answer");
                    String correctAnswer = rs.getString("correct_answer");
                    boolean isCorrect = rs.getInt("is_correct") == 1;
                    
                    // Create a simplified Question object for Answer construction
                    // Note: This is a simplified version since Answer requires a Question object
                    // In a real implementation, you might need to adjust this based on your Question class structure
                    List<String> options = new ArrayList<>();
                    options.add(chosenAnswer);
                    if (!chosenAnswer.equals(correctAnswer)) {
                        options.add(correctAnswer);
                    }
                    
                    Question question = new Question(questionNumber++, questionText, options, 
                        options.indexOf(correctAnswer));
                    Answer answer = new Answer(question, options.indexOf(chosenAnswer));
                    answers.add(answer);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving answers: " + e.getMessage(), e);
        }
        return answers;
    }

    @Override
    public void deleteAnswersBySession(int sessionId) {
        String sql = "DELETE FROM answers WHERE session_id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting answers: " + e.getMessage(), e);
        }
    }
}

package wordageddon.dao.implementation;

import wordageddon.dao.GameSessionDAO;
import wordageddon.dao.Database;
import wordageddon.model.GameSession;
import wordageddon.model.GameSessionSummary;
import wordageddon.model.UserLeaderboardEntry;
import wordageddon.model.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * SQLite implementation of the GameSessionDAO interface for game session data persistence.
 * 
 * This class provides concrete implementation of all game session-related database operations
 * using SQLite as the underlying database. It handles:
 * - Game session creation and storage
 * - Session statistics and scoring data retrieval
 * - Leaderboard generation and user ranking
 * - Game history tracking and analysis
 * - Associated answer and question data management
 * 
 * The implementation supports complex queries for leaderboard generation,
 * user statistics calculation, and comprehensive game session reporting.
 * All database operations use prepared statements for security and performance.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class GameSessionDAOSQLite implements GameSessionDAO {

    @Override
    public int addGameSession(int userId, int score, String difficulty, String language) {
        String sql = "INSERT INTO game_sessions (user_id, score, difficulty, language) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, score);
            pstmt.setString(3, difficulty);
            pstmt.setString(4, language);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding game session: " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public GameSession getGameSession(int sessionId) {
        String sql = "SELECT user_id, score, difficulty, language, created_at FROM game_sessions WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sessionId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String difficulty = rs.getString("difficulty");
                    
                    // nota: questa è un'implementazione semplificata
                    // in uno scenario reale, dovrai ricostruire le domande
                    // e le risposte dal database
                    List<Question> questions = new ArrayList<>();
                    
                    return new GameSession(difficulty, questions);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving game session: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<GameSession> getGameSessionsByUser(int userId) {
        List<GameSession> sessions = new ArrayList<>();
        String sql = "SELECT id, score, difficulty, language, created_at FROM game_sessions WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String difficulty = rs.getString("difficulty");
                    
                    // nota: questa è un'implementazione semplificata
                    // in uno scenario reale, dovrai ricostruire le domande
                    // e le risposte dal database
                    List<Question> questions = new ArrayList<>();
                    
                    sessions.add(new GameSession(difficulty, questions));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving game sessions: " + e.getMessage(), e);
        }
        return sessions;
    }

    @Override
    public void updateSessionScore(int sessionId, int score) {
        String sql = "UPDATE game_sessions SET score = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, score);
            pstmt.setInt(2, sessionId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating session score: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteGameSession(int sessionId) {
        String sql = "DELETE FROM game_sessions WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting game session: " + e.getMessage(), e);
        }
    }

    @Override
    public List<GameSession> getAllGameSessions() {
        List<GameSession> sessions = new ArrayList<>();
        String sql = "SELECT id, user_id, score, difficulty, language, created_at FROM game_sessions ORDER BY created_at DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String difficulty = rs.getString("difficulty");
                
                // nota: questa è un'implementazione semplificata
                // in uno scenario reale, dovrai ricostruire le domande
                // e le risposte dal database
                List<Question> questions = new ArrayList<>();
                
                sessions.add(new GameSession(difficulty, questions));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all game sessions: " + e.getMessage(), e);
        }
        return sessions;
    }

    @Override
    public int countGameSessions() {
        String sql = "SELECT COUNT(*) as total FROM game_sessions";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting game sessions: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<GameSessionSummary> getGameSessionSummariesByUser(int userId) {
        List<GameSessionSummary> summaries = new ArrayList<>();
        String sql = "SELECT gs.id, gs.score, gs.difficulty, gs.language, gs.created_at, " +
                     "COUNT(a.id) as total_questions, " +
                     "SUM(CASE WHEN a.is_correct = 1 THEN 1 ELSE 0 END) as correct_answers " +
                     "FROM game_sessions gs " +
                     "LEFT JOIN answers a ON gs.id = a.session_id " +
                     "WHERE gs.user_id = ? " +
                     "GROUP BY gs.id, gs.score, gs.difficulty, gs.language, gs.created_at " +
                     "ORDER BY gs.created_at DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int sessionId = rs.getInt("id");
                    String difficulty = rs.getString("difficulty");
                    int score = rs.getInt("score");
                    String language = rs.getString("language");
                    Date createdAt = new Date(rs.getTimestamp("created_at").getTime());
                    
                    // ottieni il numero effettivo di domande e risposte dal database
                    int totalQuestions = rs.getInt("total_questions");
                    int correctAnswers = rs.getInt("correct_answers");
                    
                    // per la durata, useremo un valore predefinito dato che non è memorizzato nel database
                    // in un'implementazione completa, aggiungeresti i campi start_time e end_time
                    long duration = totalQuestions > 0 ? totalQuestions * 30000L : 300000L; // 30 secondi per domanda
                    
                    GameSessionSummary summary = new GameSessionSummary(
                        sessionId, userId, difficulty, score, language, 
                        createdAt, totalQuestions, correctAnswers, duration
                    );
                    summaries.add(summary);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving game session summaries: " + e.getMessage(), e);
        }
        return summaries;
    }

    @Override
    public List<UserLeaderboardEntry> getGlobalLeaderboard() {
        List<UserLeaderboardEntry> leaderboard = new ArrayList<>();
        String sql = "SELECT u.username, SUM(gs.score) as total_points " +
                     "FROM users u " +
                     "INNER JOIN game_sessions gs ON u.id = gs.user_id " +
                     "GROUP BY u.id, u.username " +
                     "ORDER BY total_points DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                int totalPoints = rs.getInt("total_points");
                leaderboard.add(new UserLeaderboardEntry(username, totalPoints));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving global leaderboard: " + e.getMessage(), e);
        }
        return leaderboard;
    }
}

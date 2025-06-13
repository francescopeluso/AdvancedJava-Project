package wordageddon.model;

/**
 * Represents a user entry in the global leaderboard system.
 * 
 * This Data Transfer Object contains aggregated user statistics for leaderboard
 * display, including username and cumulative points from all game sessions.
 * It provides a lightweight representation optimized for leaderboard queries
 * and ranking calculations.
 * 
 * @author Gregorio Barberio, Francesco Peluso, Davide Quaranta, Ciro Ronca
 * @version 1.0
 * @since 2025
 */
public class UserLeaderboardEntry {
    /** The username displayed in the leaderboard */
    private String username;
    
    /** Total accumulated points across all game sessions */
    private double totalPoints;
    
    /**
     * Default constructor for UserLeaderboardEntry.
     * 
     * // costruttore vuoto per l'inizializzazione
     */
    public UserLeaderboardEntry() {}
    
    /**
     * Constructs a new UserLeaderboardEntry with specified username and total points.
     * 
     * // crea una voce della classifica con username e punteggio
     * 
     * @param username the username to display in the leaderboard
     * @param totalPoints the total accumulated points for this user
     */
    public UserLeaderboardEntry(String username, double totalPoints) {
        this.username = username;
        this.totalPoints = totalPoints;
    }
    
    /**
     * Gets the username for this leaderboard entry.
     * 
     * // restituisce il nome utente di questa voce della classifica
     * 
     * @return the username displayed in the leaderboard
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username for this leaderboard entry.
     * 
     * // imposta il nome utente per questa voce della classifica
     * 
     * @param username the username to display in the leaderboard
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets the total accumulated points for this user.
     * 
     * // restituisce il punteggio totale accumulato dall'utente
     * 
     * @return the total points across all game sessions
     */
    public double getTotalPoints() {
        return totalPoints;
    }
    
    /**
     * Sets the total accumulated points for this user.
     * 
     * // imposta il punteggio totale accumulato dall'utente
     * 
     * @param totalPoints the total points to set
     */
    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;
    }
}

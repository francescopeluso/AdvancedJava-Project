package wordageddon.model;

/**
 * Represents a user entry in the global leaderboard.
 * Contains username and total points for leaderboard display.
 */
public class UserLeaderboardEntry {
    private String username;
    private int totalPoints;
    
    public UserLeaderboardEntry() {}
    
    public UserLeaderboardEntry(String username, int totalPoints) {
        this.username = username;
        this.totalPoints = totalPoints;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}

package it.unisa.diem.ja.g10.model;

/**
 *
 * @author david
 */
public class User {
    private String username;
    private String password;
    private int score;

    public User(String username, String password, int score) {
        this.username = username;
        this.password = password;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getScore() {
        return score;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    
}

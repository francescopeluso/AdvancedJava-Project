package it.unisa.diem.ja.g10.model;

/**
 *
 * @author david
 */
public class Game {
    private String readingTime;
    private String level;
    private int gameScore;

    public Game(String readingTime, String level, int gameScore) {
        this.readingTime = readingTime;
        this.level = level;
        this.gameScore = gameScore;
    }

    public String getReadingTime() {
        return readingTime;
    }

    public String getLevel() {
        return level;
    }

    public int getGameScore() {
        return gameScore;
    }

    public void setReadingTime(String readingTime) {
        this.readingTime = readingTime;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
    }
    
    
}

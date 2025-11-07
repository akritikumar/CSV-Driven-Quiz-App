package quiz;

import java.time.LocalDateTime;

public class LeaderboardEntry {
    private final String username;
    private final int score;
    private final LocalDateTime quizDate;

    public LeaderboardEntry(String username, int score, LocalDateTime quizDate) {
        this.username = username;
        this.score = score;
        this.quizDate = quizDate;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public LocalDateTime getQuizDate() {
        return quizDate;
    }
}

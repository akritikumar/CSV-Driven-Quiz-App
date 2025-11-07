package quiz;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    // ✅ Update these values for your setup:
    private static final String DB_URL = "jdbc:sqlite:identifier.sqlite";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    public static void saveResult(String username, int score) {
        String sql = "INSERT INTO results (username, score, quiz_date) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, score);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

            System.out.println("✅ Saved result for " + username + " | Score: " + score);
        } catch (SQLException e) {
            System.err.println("❌ Error saving result:");
            e.printStackTrace();
        }
    }

    public static List<LeaderboardEntry> getLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = "SELECT username, score, quiz_date FROM results ORDER BY score DESC, quiz_date ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entries.add(new LeaderboardEntry(
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getTimestamp("quiz_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading leaderboard:");
            e.printStackTrace();
        }

        return entries;
    }
}

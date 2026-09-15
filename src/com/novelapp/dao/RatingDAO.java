package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;

public class RatingDAO {

    public boolean hasRated(int userId, int storyId) {
        String sql = "SELECT 1 FROM ratings WHERE user_id = ? AND story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addOrUpdateRating(int userId, int storyId, int score, String review) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            if (hasRated(userId, storyId)) {
                // Cập nhật
                String sql = "UPDATE ratings SET score = ?, review = ?, updated_at = GETDATE() WHERE user_id = ? AND story_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, score);
                    ps.setString(2, review);
                    ps.setInt(3, userId);
                    ps.setInt(4, storyId);
                    ps.executeUpdate();
                }
            } else {
                // Thêm mới
                String sql = "INSERT INTO ratings (user_id, story_id, score, review) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, storyId);
                    ps.setInt(3, score);
                    ps.setString(4, review);
                    ps.executeUpdate();
                }
            }

            // Cập nhật rating_avg và rating_count của truyện
            updateStoryRating(conn, storyId);

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {}
        }
    }

    private void updateStoryRating(Connection conn, int storyId) throws SQLException {
        String sql = "UPDATE stories SET "
                   + "rating_avg = (SELECT AVG(CAST(score AS FLOAT)) FROM ratings WHERE story_id = ?), "
                   + "rating_count = (SELECT COUNT(*) FROM ratings WHERE story_id = ?) "
                   + "WHERE story_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, storyId);
            ps.setInt(3, storyId);
            ps.executeUpdate();
        }
    }

    public int getUserRating(int userId, int storyId) {
        String sql = "SELECT score FROM ratings WHERE user_id = ? AND story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("score");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
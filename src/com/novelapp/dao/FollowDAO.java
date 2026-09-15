package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;

public class FollowDAO {

    public boolean isFollowing(int userId, int storyId) {
        String sql = "SELECT 1 FROM follows WHERE user_id = ? AND story_id = ?";
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

    public boolean follow(int userId, int storyId) {
        if (isFollowing(userId, storyId)) return false;
        
        String sql = "INSERT INTO follows (user_id, story_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                // Tăng follow_count
                updateFollowCount(storyId, 1);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean unfollow(int userId, int storyId) {
        String sql = "DELETE FROM follows WHERE user_id = ? AND story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                updateFollowCount(storyId, -1);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateFollowCount(int storyId, int delta) {
        String sql = "UPDATE stories SET follow_count = follow_count + ? WHERE story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, storyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
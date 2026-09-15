package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;

public class FavoriteDAO {

    public boolean isFavorited(int userId, int storyId) {
        String sql = "SELECT 1 FROM favorites WHERE user_id = ? AND story_id = ?";
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

    public boolean addFavorite(int userId, int storyId) {
        if (isFavorited(userId, storyId)) return false;
        
        String sql = "INSERT INTO favorites (user_id, story_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                updateFavoriteCount(storyId, 1);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFavorite(int userId, int storyId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                updateFavoriteCount(storyId, -1);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateFavoriteCount(int storyId, int delta) {
        String sql = "UPDATE stories SET favorite_count = favorite_count + ? WHERE story_id = ?";
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
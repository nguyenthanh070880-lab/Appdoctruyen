package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.Story;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoryDAO {

    public List<Story> getApprovedStories(int limit) {
        List<Story> list = new ArrayList<>();

        String sql = "SELECT TOP (?) s.*, u.full_name AS author_name "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 "
                   + "ORDER BY s.updated_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Story findById(int storyId) {
        String sql = "SELECT s.*, u.full_name AS author_name "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.story_id = ? AND s.is_deleted = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, storyId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Story> searchStories(String keyword) {
        List<Story> list = new ArrayList<>();

        String sql = "SELECT TOP 50 s.*, u.full_name AS author_name "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 "
                   + "AND (s.title LIKE ? OR u.full_name LIKE ? OR s.description LIKE ?) "
                   + "ORDER BY s.view_count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword + "%";

            ps.setString(1, key);
            ps.setString(2, key);
            ps.setString(3, key);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Story> getTopStoriesByView(int limit) {
        List<Story> list = new ArrayList<>();
        String sql = "SELECT TOP (?) s.*, u.full_name AS author_name "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 "
                   + "ORDER BY s.view_count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Story> getTopStoriesByRating(int limit) {
        List<Story> list = new ArrayList<>();
        String sql = "SELECT TOP (?) s.*, u.full_name AS author_name "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND s.rating_count > 0 "
                   + "ORDER BY s.rating_avg DESC, s.rating_count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Story> getRecommendedStories(int limit) {
        List<Story> list = new ArrayList<>();
        String sql = "SELECT TOP (?) s.*, u.full_name AS author_name "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 "
                   + "ORDER BY (s.view_count * 0.4 + s.follow_count * 0.3 + s.rating_avg * 100 * 0.3) DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateCoverUrl(int storyId, String coverUrl) {
        String sql = "UPDATE stories SET cover_url = ?, updated_at = GETDATE() WHERE story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, coverUrl);
            ps.setInt(2, storyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Story mapResultSetToStory(ResultSet rs) throws SQLException {
        Story story = new Story();

        story.setStoryId(rs.getInt("story_id"));
        story.setAuthorId(rs.getInt("author_id"));
        story.setAuthorName(rs.getString("author_name"));
        story.setTitle(rs.getString("title"));
        story.setSlug(rs.getString("slug"));
        story.setDescription(rs.getString("description"));
        story.setCoverUrl(rs.getString("cover_url"));
        story.setStatus(rs.getString("status"));
        story.setModerationStatus(rs.getString("moderation_status"));
        story.setPaid(rs.getBoolean("is_paid"));
        story.setViewCount(rs.getLong("view_count"));
        story.setFavoriteCount(rs.getInt("favorite_count"));
        story.setFollowCount(rs.getInt("follow_count"));
        story.setRatingAvg(rs.getDouble("rating_avg"));
        story.setRatingCount(rs.getInt("rating_count"));
        story.setDeleted(rs.getBoolean("is_deleted"));

        Timestamp published = rs.getTimestamp("published_at");
        if (published != null) {
            story.setPublishedAt(published.toLocalDateTime());
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            story.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            story.setUpdatedAt(updated.toLocalDateTime());
        }

        return story;
    }
}
package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.Story;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryDAO {

    public List<Story> getApprovedStories(int limit) {
        List<Story> list = new ArrayList<>();

        String sql = "SELECT TOP (?) s.*, u.full_name AS author_name "
                + "FROM stories s "
                + "JOIN users u ON s.author_id = u.user_id "
                + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND ISNULL(s.is_hidden, 0) = 0 "
                + "ORDER BY s.updated_at DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
                + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND ISNULL(s.is_hidden, 0) = 0 "
                + "AND (s.title LIKE ? OR u.full_name LIKE ? OR s.description LIKE ?) "
                + "ORDER BY s.view_count DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
                + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND ISNULL(s.is_hidden, 0) = 0 "
                + "ORDER BY s.view_count DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
                + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND ISNULL(s.is_hidden, 0) = 0 AND s.rating_count > 0 "
                + "ORDER BY s.rating_avg DESC, s.rating_count DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
                + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND ISNULL(s.is_hidden, 0) = 0 "
                + "ORDER BY (s.view_count * 0.4 + s.follow_count * 0.3 + s.rating_avg * 100 * 0.3) DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, coverUrl);
            ps.setInt(2, storyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ẩn hoặc hiện một bộ truyện theo storyId
     */
    public boolean setStoryHidden(int storyId, boolean hidden) {
        String sql = "UPDATE stories SET is_hidden = ?, updated_at = GETDATE() WHERE story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, hidden);
            ps.setInt(2, storyId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Thêm liên kết Thể loại - Truyện vào bảng story_genres
     */
    public void addStoryGenre(int storyId, int genreId) {
        String sql = "IF NOT EXISTS (SELECT 1 FROM story_genres WHERE story_id = ? AND genre_id = ?) "
                   + "INSERT INTO story_genres (story_id, genre_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, genreId);
            ps.setInt(3, storyId);
            ps.setInt(4, genreId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa toàn bộ thể loại của 1 truyện (Dùng khi cập nhật/sửa thể loại truyện)
     */
    public void clearStoryGenres(int storyId) {
        String sql = "DELETE FROM story_genres WHERE story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeAllGenresByStoryId(int storyId) {
        String sql = "DELETE FROM story_genres WHERE story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
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

    public String getGenresByStoryId(int storyId) {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT g.genre_name FROM story_genres sg "
                + "JOIN genres g ON sg.genre_id = g.genre_id "
                + "WHERE sg.story_id = ? AND g.is_active = 1 "
                + "ORDER BY g.genre_name";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(rs.getString("genre_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.length() > 0 ? sb.toString() : "Chưa phân loại";
    }

    public List<Map<String, Object>> getGenreListByStoryId(int storyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT g.genre_id, g.genre_name FROM story_genres sg "
                + "JOIN genres g ON sg.genre_id = g.genre_id "
                + "WHERE sg.story_id = ? AND g.is_active = 1 "
                + "ORDER BY g.genre_name";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("genreId", rs.getInt("genre_id"));
                    map.put("genreName", rs.getString("genre_name"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Story> searchWithFilter(String keyword, String status, String paid, String sort, Integer genreId) {
        List<Story> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT TOP 50 s.*, u.full_name AS author_name FROM stories s "
                + "JOIN users u ON s.author_id = u.user_id "
                + "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 AND ISNULL(s.is_hidden, 0) = 0 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (s.title LIKE ? OR u.full_name LIKE ? OR s.description LIKE ?) ");
            String key = "%" + keyword.trim() + "%";
            params.add(key);
            params.add(key);
            params.add(key);
        }

        if (status != null && !"Tất cả".equals(status)) {
            sql.append("AND s.status = ? ");
            params.add(status);
        }

        if ("Miễn phí".equals(paid)) {
            sql.append("AND s.is_paid = 0 ");
        } else if ("Trả phí".equals(paid)) {
            sql.append("AND s.is_paid = 1 ");
        }

        if (genreId != null && genreId > 0) {
            sql.append("AND EXISTS (SELECT 1 FROM story_genres sg WHERE sg.story_id = s.story_id AND sg.genre_id = ?) ");
            params.add(genreId);
        }

        if ("Lượt xem cao".equals(sort)) {
            sql.append("ORDER BY s.view_count DESC");
        } else if ("Đánh giá cao".equals(sort)) {
            sql.append("ORDER BY s.rating_avg DESC");
        } else {
            sql.append("ORDER BY s.updated_at DESC");
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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

    public Map<String, Integer> getAllActiveGenres() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT genre_id, genre_name FROM genres WHERE is_active = 1 ORDER BY genre_name";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("genre_name"), rs.getInt("genre_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
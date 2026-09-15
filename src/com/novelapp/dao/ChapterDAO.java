package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.Chapter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChapterDAO {
    // Lấy danh sách chương của 1 truyện (đã duyệt)
    public List<Chapter> getChaptersByStoryId(int storyId) {
        List<Chapter> list = new ArrayList<>();
        String sql = "SELECT * FROM chapters " + "WHERE story_id = ? AND moderation_status = 'APPROVED' AND is_deleted = 0 " + "ORDER BY chapter_number ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToChapter(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy 1 chương theo ID
    public Chapter findById(int chapterId) {
        String sql = "SELECT * FROM chapters WHERE chapter_id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChapter(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Tăng lượt xem chương
    public void increaseViewCount(int chapterId) {
        String sql = "UPDATE chapters SET view_count = view_count + 1 WHERE chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lấy chương trước
    public Chapter getPreviousChapter(int storyId, double currentNumber) {
        String sql = "SELECT TOP 1 * FROM chapters "
                   + "WHERE story_id = ? AND chapter_number < ? "
                   + "AND moderation_status = 'APPROVED' AND is_deleted = 0 "
                   + "ORDER BY chapter_number DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, storyId);
            ps.setDouble(2, currentNumber);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChapter(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy chương sau
    public Chapter getNextChapter(int storyId, double currentNumber) {
        String sql = "SELECT TOP 1 * FROM chapters "
                   + "WHERE story_id = ? AND chapter_number > ? "
                   + "AND moderation_status = 'APPROVED' AND is_deleted = 0 "
                   + "ORDER BY chapter_number ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, storyId);
            ps.setDouble(2, currentNumber);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToChapter(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Chapter mapResultSetToChapter(ResultSet rs) throws SQLException {
        Chapter chapter = new Chapter();
        chapter.setChapterId(rs.getInt("chapter_id"));
        chapter.setStoryId(rs.getInt("story_id"));
        chapter.setChapterNumber(rs.getDouble("chapter_number"));
        chapter.setTitle(rs.getString("title"));
        chapter.setContent(rs.getString("content"));
        chapter.setFree(rs.getBoolean("is_free"));
        chapter.setPriceCoin(rs.getInt("price_coin"));
        chapter.setWordCount(rs.getInt("word_count"));
        chapter.setViewCount(rs.getLong("view_count"));
        chapter.setModerationStatus(rs.getString("moderation_status"));
        chapter.setDeleted(rs.getBoolean("is_deleted"));
        Timestamp published = rs.getTimestamp("published_at");
        if (published != null) chapter.setPublishedAt(published.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) chapter.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) chapter.setUpdatedAt(updated.toLocalDateTime());
        return chapter;
    }
}

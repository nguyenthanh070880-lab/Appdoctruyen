package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CommentDAO {

    public boolean addComment(int userId, Integer storyId, Integer chapterId, String content) {
        String sql = "INSERT INTO comments (user_id, story_id, chapter_id, content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (storyId != null) ps.setInt(2, storyId); else ps.setNull(2, Types.INTEGER);
            if (chapterId != null) ps.setInt(3, chapterId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Map<String, Object>> getCommentsByStory(int storyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.content, c.created_at, c.like_count, u.full_name, u.username "
                   + "FROM comments c "
                   + "JOIN users u ON c.user_id = u.user_id "
                   + "WHERE c.story_id = ? AND c.is_deleted = 0 AND c.is_hidden = 0 "
                   + "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("commentId", rs.getInt("comment_id"));
                    row.put("content", rs.getString("content"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("likeCount", rs.getInt("like_count"));
                    row.put("fullName", rs.getString("full_name"));
                    row.put("username", rs.getString("username"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentDAO {

    public boolean addComment(int userId, Integer storyId, Integer chapterId, String content) {
        String sql = "INSERT INTO comments (user_id, story_id, chapter_id, content, parent_id, is_hidden, is_deleted, like_count) "
                   + "VALUES (?, ?, ?, ?, NULL, 0, 0, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (storyId != null) ps.setInt(2, storyId); else ps.setNull(2, Types.INTEGER);
            if (chapterId != null) ps.setInt(3, chapterId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return addCommentSimple(userId, storyId, chapterId, content);
        }
    }

    private boolean addCommentSimple(int userId, Integer storyId, Integer chapterId, String content) {
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

    public boolean replyComment(int userId, int storyId, int parentCommentId, String content) {
        String sql = "INSERT INTO comments (user_id, story_id, parent_id, content, is_hidden, is_deleted, like_count) "
                   + "VALUES (?, ?, ?, ?, 0, 0, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            ps.setInt(3, parentCommentId);
            ps.setString(4, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Map<String, Object>> getCommentsByStory(int storyId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.parent_id, c.user_id, c.content, c.created_at, "
                   + "ISNULL(c.like_count, 0) AS like_count, u.username, u.full_name "
                   + "FROM comments c "
                   + "JOIN users u ON c.user_id = u.user_id "
                   + "WHERE c.story_id = ? "
                   + "AND ISNULL(c.is_hidden, 0) = 0 "
                   + "AND ISNULL(c.is_deleted, 0) = 0 "
                   + "ORDER BY c.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("commentId", rs.getInt("comment_id"));
                    row.put("parentId", rs.getObject("parent_id"));
                    row.put("userId", rs.getInt("user_id"));
                    row.put("content", rs.getString("content"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("likeCount", rs.getInt("like_count"));
                    row.put("username", rs.getString("username"));
                    row.put("fullName", rs.getString("full_name"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean toggleLike(int userId, int commentId) {
        String check = "SELECT 1 FROM comment_likes WHERE user_id = ? AND comment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, userId);
            ps.setInt(2, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement del = conn.prepareStatement(
                            "DELETE FROM comment_likes WHERE user_id = ? AND comment_id = ?")) {
                        del.setInt(1, userId);
                        del.setInt(2, commentId);
                        del.executeUpdate();
                    }
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE comments SET like_count = CASE WHEN ISNULL(like_count,1) > 0 THEN like_count - 1 ELSE 0 END WHERE comment_id = ?")) {
                        upd.setInt(1, commentId);
                        upd.executeUpdate();
                    }
                    return false;
                } else {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO comment_likes (user_id, comment_id) VALUES (?, ?)")) {
                        ins.setInt(1, userId);
                        ins.setInt(2, commentId);
                        ins.executeUpdate();
                    }
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE comments SET like_count = ISNULL(like_count,0) + 1 WHERE comment_id = ?")) {
                        upd.setInt(1, commentId);
                        upd.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteOwnComment(int userId, int commentId) {
        String sql = "UPDATE comments SET is_deleted = 1 WHERE comment_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
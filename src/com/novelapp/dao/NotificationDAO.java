package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDAO {

    /**
     * Lấy danh sách 50 thông báo mới nhất của user
     */
    public List<Map<String, Object>> getNotifications(int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 50 notification_id, title, content, type, is_read, created_at "
                   + "FROM notifications WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("notification_id"));
                    row.put("title", rs.getString("title"));
                    row.put("content", rs.getString("content"));
                    row.put("type", rs.getString("type"));
                    row.put("isRead", rs.getBoolean("is_read"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đánh dấu 1 thông báo là đã đọc
     */
    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    public void markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Tạo thông báo mới (phiên bản chuẩn)
     */
    public void createNotification(int userId, String title, String content, String type) {
        send(userId, title, content, type);
    }

    /**
     * Helper gửi thông báo sự kiện / hệ thống ngắn gọn (gắn sẵn is_read = 0 và GETDATE())
     */
    public void send(int userId, String title, String content, String type) {
        String sql = "INSERT INTO notifications (user_id, title, content, type, is_read, created_at) "
                   + "VALUES (?, ?, ?, ?, 0, GETDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Support gửi thông báo bằng kết nối Transaction có sẵn (cho phép rollback cùng vụ giao dịch)
     */
    public void send(Connection conn, int userId, String title, String content, String type) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, content, type, is_read, created_at) "
                   + "VALUES (?, ?, ?, ?, 0, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, content);
            ps.setString(4, type);
            ps.executeUpdate();
        }
    }
}
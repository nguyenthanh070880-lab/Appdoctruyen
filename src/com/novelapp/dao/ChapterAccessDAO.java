package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;

public class ChapterAccessDAO {

    // Kiểm tra user đã mở khóa chương chưa
    public boolean hasAccess(int userId, int chapterId) {
        String sql = "SELECT 1 FROM chapter_access WHERE user_id = ? AND chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cấp quyền mở khóa
    public boolean grantAccess(int userId, int chapterId, String unlockType) {
        String sql = "INSERT INTO chapter_access (user_id, chapter_id, unlock_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            ps.setString(3, unlockType);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
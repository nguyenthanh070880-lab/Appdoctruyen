package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;

public class ReportDAO {

    public boolean createReport(int reporterId, String targetType, int targetId, String reason, String description) {
        String sql = "INSERT INTO reports (reporter_id, target_type, target_id, reason, description, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reporterId);
            ps.setString(2, targetType); // STORY hoặc CHAPTER
            ps.setInt(3, targetId);
            ps.setString(4, reason);
            ps.setString(5, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
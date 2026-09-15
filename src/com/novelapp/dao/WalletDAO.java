package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import java.sql.*;

public class WalletDAO {

    public long getBalance(int userId) {
        String sql = "SELECT balance FROM wallets WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean addCoin(int userId, long amount, String type, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Cộng tiền
            String updateSql = "UPDATE wallets SET balance = balance + ?, updated_at = GETDATE() WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setLong(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            // Lấy số dư mới
            long newBalance = getBalanceWithConnection(conn, userId);

            // Ghi lịch sử
            String logSql = "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, userId);
                ps.setLong(2, amount);
                ps.setLong(3, newBalance);
                ps.setString(4, type);
                ps.setString(5, description);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception e) {}
        }
    }

    // Đã thêm chính xác hàm deductCoin nguyên bản của bạn vào class
    public boolean deductCoin(int userId, long amount, String type, String description) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra số dư
            long currentBalance = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM wallets WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) currentBalance = rs.getLong("balance");
                }
            }

            if (currentBalance < amount) {
                conn.rollback();
                return false; // không đủ tiền
            }

            // Trừ tiền
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE wallets SET balance = balance - ?, updated_at = GETDATE() WHERE user_id = ?")) {
                ps.setLong(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            long newBalance = currentBalance - amount;

            // Ghi lịch sử
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setLong(2, -amount); // số âm vì là trừ
                ps.setLong(3, newBalance);
                ps.setString(4, type);
                ps.setString(5, description);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {}
        }
    }

    private long getBalanceWithConnection(Connection conn, int userId) throws SQLException {
        String sql = "SELECT balance FROM wallets WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("balance");
            }
        }
        return 0;
    }
}

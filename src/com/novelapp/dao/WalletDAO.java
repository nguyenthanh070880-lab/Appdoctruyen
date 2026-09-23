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
        if (amount <= 0) return false;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Khóa dòng ví và lấy số dư hiện tại để đảm bảo nhất quán
            long currentBalance = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM wallets WITH (UPDLOCK, ROWLOCK) WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Ví không tồn tại
                    }
                    currentBalance = rs.getLong("balance");
                }
            }

            long newBalance = currentBalance + amount;

            // Cập nhật số dư ví
            String updateSql = "UPDATE wallets SET balance = ?, updated_at = GETDATE() WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setLong(1, newBalance);
                ps.setInt(2, userId);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Ghi lịch sử giao dịch
            String logSql = "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                ps.setInt(1, userId);
                ps.setLong(2, amount); // Số dương vì là cộng tiền
                ps.setLong(3, newBalance);
                ps.setString(4, type);
                ps.setString(5, description);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public boolean deductCoin(int userId, long amount, String type, String description) {
        if (amount <= 0) return false;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Khóa dòng ví + đọc số dư trực tiếp từ DB
            long balance = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM wallets WITH (UPDLOCK, ROWLOCK) WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false; // Ví không tồn tại
                    }
                    balance = rs.getLong("balance");
                }
            }

            // 2. Kiểm tra điều kiện không âm
            if (balance < amount) {
                conn.rollback();
                return false; // Không đủ tiền
            }

            long newBalance = balance - amount;

            // 3. Trừ tiền kèm điều kiện chặn race condition
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE wallets SET balance = ?, updated_at = GETDATE() WHERE user_id = ? AND balance >= ?")) {
                ps.setLong(1, newBalance);
                ps.setInt(2, userId);
                ps.setLong(3, amount);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 4. Ghi lịch sử giao dịch
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) "
                  + "VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setLong(2, -amount); // Trừ -> lưu số âm
                ps.setLong(3, newBalance);
                ps.setString(4, type);
                ps.setString(5, description);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception ignored) {}
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
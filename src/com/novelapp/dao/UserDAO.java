package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ==================== ĐĂNG KÝ ====================
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, birth_date, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            
            if (user.getBirthDate() != null && !user.getBirthDate().isEmpty()) {
                ps.setDate(5, java.sql.Date.valueOf(user.getBirthDate())); // yyyy-MM-dd
            } else {
                ps.setNull(5, Types.DATE);
            }
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                // Lấy user_id vừa tạo
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        user.setUserId(userId);
                        
                        // Gán role USER mặc định
                        assignDefaultRole(userId);
                        
                        // Tạo ví coin
                        createWallet(userId);
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Gán role USER mặc định
    private void assignDefaultRole(int userId) {
        String sql = "INSERT INTO user_roles (user_id, role_id) "
                   + "SELECT ?, role_id FROM roles WHERE role_name = 'USER'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tạo ví coin cho user mới
    private void createWallet(int userId) {
        String sql = "INSERT INTO wallets (user_id, balance) VALUES (?, 0)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== ĐĂNG NHẬP ====================
    public User findByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND is_deleted = 0";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Lấy danh sách role của user
    public List<String> getRolesByUserId(int userId) {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT r.role_name FROM user_roles ur "
                   + "JOIN roles r ON ur.role_id = r.role_id "
                   + "WHERE ur.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("role_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    // Kiểm tra username đã tồn tại chưa
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật last_login
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = GETDATE() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Map ResultSet → User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            user.setDateOfBirth(dob.toLocalDate());
        }
        
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setPhone(rs.getString("phone"));
        user.setStatus(rs.getString("status"));
        user.setDeleted(rs.getBoolean("is_deleted"));
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) user.setCreatedAt(created.toLocalDateTime());
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) user.setUpdatedAt(updated.toLocalDateTime());
        
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) user.setLastLogin(lastLogin.toLocalDateTime());
        
        return user;
    }
}
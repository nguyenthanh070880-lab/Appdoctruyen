package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminUserFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminUserFrame() {
        this.currentUser = SessionManager.getCurrentUser();

        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }

        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setTitle("Quản lý người dùng - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Quản lý người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        String[] columns = {
            "ID", "Username", "Họ tên", "Email",
            "Trạng thái", "Role", "Ngày tạo"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnLock = new JButton("Khóa tài khoản");
        btnLock.setBackground(new Color(220, 53, 69));
        btnLock.setForeground(Color.WHITE);
        btnLock.setFocusPainted(false);
        btnLock.addActionListener(e -> changeStatus("LOCKED"));

        JButton btnUnlock = new JButton("Mở khóa");
        btnUnlock.setBackground(new Color(0, 153, 76));
        btnUnlock.setForeground(Color.WHITE);
        btnUnlock.setFocusPainted(false);
        btnUnlock.addActionListener(e -> changeStatus("ACTIVE"));

        JButton btnSetAuthor = new JButton("Cấp quyền Author");
        btnSetAuthor.setFocusPainted(false);
        btnSetAuthor.addActionListener(e -> assignRole("AUTHOR"));

        JButton btnRemoveAuthor = new JButton("Thu hồi Author");
        btnRemoveAuthor.setFocusPainted(false);
        btnRemoveAuthor.addActionListener(e -> removeRole("AUTHOR"));

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadUsers());

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnLock);
        buttonPanel.add(btnUnlock);
        buttonPanel.add(btnSetAuthor);
        buttonPanel.add(btnRemoveAuthor);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadUsers() {
        model.setRowCount(0);

        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, u.status, u.created_at, "
                   + "STUFF((SELECT ', ' + r.role_name FROM user_roles ur "
                   + "JOIN roles r ON ur.role_id = r.role_id "
                   + "WHERE ur.user_id = u.user_id FOR XML PATH('')), 1, 2, '') AS roles "
                   + "FROM users u "
                   + "WHERE u.is_deleted = 0 "
                   + "ORDER BY u.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("status"),
                    rs.getString("roles"),
                    rs.getTimestamp("created_at")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void changeStatus(String status) {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản!");
            return;
        }

        int userId = (int) table.getValueAt(row, 0);
        String username = (String) table.getValueAt(row, 1);

        if (userId == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "Bạn không thể khóa chính mình!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn "
                        + (status.equals("LOCKED") ? "KHÓA" : "MỞ KHÓA")
                        + " tài khoản \"" + username + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE users SET status = ?, updated_at = GETDATE() "
                   + "WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, userId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadUsers();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void assignRole(String roleName) {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản!");
            return;
        }

        int userId = (int) table.getValueAt(row, 0);
        String username = (String) table.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cấp quyền " + roleName
                        + " cho tài khoản \"" + username + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "IF NOT EXISTS (SELECT 1 FROM user_roles ur "
                   + "JOIN roles r ON ur.role_id = r.role_id "
                   + "WHERE ur.user_id = ? AND r.role_name = ?) "
                   + "INSERT INTO user_roles (user_id, role_id) "
                   + "SELECT ?, role_id FROM roles WHERE role_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, roleName);
            ps.setInt(3, userId);
            ps.setString(4, roleName);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cấp quyền thành công!");
            loadUsers();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void removeRole(String roleName) {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản!");
            return;
        }

        int userId = (int) table.getValueAt(row, 0);
        String username = (String) table.getValueAt(row, 1);

        if (userId == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn không thể tự thu hồi quyền của chính mình!"
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Thu hồi quyền " + roleName
                        + " của tài khoản \"" + username + "\"?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE ur FROM user_roles ur "
                   + "JOIN roles r ON ur.role_id = r.role_id "
                   + "WHERE ur.user_id = ? AND r.role_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, roleName);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã thu hồi quyền " + roleName + "!"
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Tài khoản này không có quyền " + roleName + "."
                );
            }

            loadUsers();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: " + e.getMessage()
            );
        }
    }
}
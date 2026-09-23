package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.service.AuthService;
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
    private JTextField txtSearch;

    public AdminUserFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadUsers("");
    }

    private void initComponents() {
        setTitle("Quản lý người dùng - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 680);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 1. Header + Search Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lbl = new JLabel("👥 Quản lý người dùng & Phân quyền");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setForeground(Color.WHITE);
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtSearch = new JTextField(16);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.addActionListener(e -> loadUsers(txtSearch.getText().trim()));

        JButton btnSearch = createBtn("Tìm", new Color(0, 153, 76));
        btnSearch.addActionListener(e -> loadUsers(txtSearch.getText().trim()));

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        header.add(lbl, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);

        // 2. Table
        String[] columns = {"ID", "Username", "Họ tên", "Email", "Trạng thái", "Role", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // 3. Bottom Action Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnAdd = createBtn("+ Thêm user", new Color(0, 153, 76));
        JButton btnEdit = createBtn("Sửa user", new Color(255, 140, 0));
        JButton btnLogs = createBtn("Lịch sử HĐ", null);
        JButton btnLock = createBtn("Khóa", new Color(220, 53, 69));
        JButton btnUnlock = createBtn("Mở khóa", new Color(40, 167, 69));
        JButton btnSetAuthor = createBtn("Cấp Author", null);
        JButton btnRemoveAuthor = createBtn("Thu hồi Author", null);
        JButton btnSetStaff = createBtn("Cấp Staff", null);
        JButton btnRemoveStaff = createBtn("Thu hồi Staff", null);
        JButton btnSetAdmin = createBtn("Cấp Admin", new Color(153, 51, 204));
        JButton btnRefresh = createBtn("Làm mới", null);
        JButton btnBack = createBtn("← Dashboard", null);

        btnAdd.addActionListener(e -> showAddUserDialog());
        btnEdit.addActionListener(e -> onEditUserClicked());
        btnLogs.addActionListener(e -> showActivityLogs());
        btnLock.addActionListener(e -> changeStatus("LOCKED"));
        btnUnlock.addActionListener(e -> changeStatus("ACTIVE"));
        btnSetAuthor.addActionListener(e -> assignRole("AUTHOR"));
        btnRemoveAuthor.addActionListener(e -> removeRole("AUTHOR"));
        btnSetStaff.addActionListener(e -> assignRole("STAFF"));
        btnRemoveStaff.addActionListener(e -> removeRole("STAFF"));
        btnSetAdmin.addActionListener(e -> assignRole("ADMIN"));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadUsers("");
        });
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnLogs);
        bottom.add(btnLock);
        bottom.add(btnUnlock);
        bottom.add(btnSetAuthor);
        bottom.add(btnRemoveAuthor);
        bottom.add(btnSetStaff);
        bottom.add(btnRemoveStaff);
        bottom.add(btnSetAdmin);
        bottom.add(btnRefresh);
        bottom.add(btnBack);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (bg != null) {
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
        }
        return btn;
    }

    private void loadUsers(String keyword) {
        model.setRowCount(0);
        StringBuilder sql = new StringBuilder(
                "SELECT u.user_id, u.username, u.full_name, u.email, u.status, u.created_at, "
                + "STUFF((SELECT ', ' + r.role_name FROM user_roles ur "
                + "JOIN roles r ON ur.role_id = r.role_id WHERE ur.user_id = u.user_id FOR XML PATH('')), 1, 2, '') AS roles "
                + "FROM users u WHERE u.is_deleted = 0 "
        );

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            sql.append("AND (u.username LIKE ? OR u.email LIKE ? OR u.full_name LIKE ?) ");
        }
        sql.append("ORDER BY u.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (hasKeyword) {
                String searchPattern = "%" + keyword.trim() + "%";
                ps.setString(1, searchPattern);
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách user: " + e.getMessage());
        }
    }

    private void showAddUserDialog() {
        JTextField txtUser = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtName = new JTextField();
        JPasswordField txtPass = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Username:"));
        p.add(txtUser);
        p.add(new JLabel("Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Họ tên:"));
        p.add(txtName);
        p.add(new JLabel("Mật khẩu:"));
        p.add(txtPass);

        if (JOptionPane.showConfirmDialog(this, p, "Thêm user mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        String username = txtUser.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPass.getPassword());
        String fullName = txtName.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Username, Email và Mật khẩu!");
            return;
        }

        String err = new AuthService().register(
                username, email, password, password, fullName, "2000-01-01");
        if (err == null) {
            logActivity(currentUser.getUserId(), "ADMIN_ADD_USER", "Tạo user " + username);
            JOptionPane.showMessageDialog(this, "Thêm người dùng thành công!");
            loadUsers(txtSearch.getText().trim());
        } else {
            JOptionPane.showMessageDialog(this, err);
        }
    }

    private void onEditUserClicked() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần sửa!");
            return;
        }
        int userId = (int) table.getValueAt(row, 0);
        String username = String.valueOf(table.getValueAt(row, 1));
        String fullName = String.valueOf(table.getValueAt(row, 2));
        String email = String.valueOf(table.getValueAt(row, 3));
        showEditUserDialog(userId, username, email, fullName);
    }

    private void showEditUserDialog(int userId, String username, String email, String fullName) {
        JTextField txtEmail = new JTextField(email);
        JTextField txtName = new JTextField(fullName);

        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.add(new JLabel("Email:"));
        p.add(txtEmail);
        p.add(new JLabel("Họ tên:"));
        p.add(txtName);

        if (JOptionPane.showConfirmDialog(this, p, "Sửa user: " + username,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        String sql = "UPDATE users SET email = ?, full_name = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtEmail.getText().trim());
            ps.setString(2, txtName.getText().trim());
            ps.setInt(3, userId);
            if (ps.executeUpdate() > 0) {
                logActivity(currentUser.getUserId(), "ADMIN_EDIT_USER", "Sửa user_id=" + userId);
                JOptionPane.showMessageDialog(this, "Đã cập nhật thành công!");
                loadUsers(txtSearch.getText().trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + e.getMessage());
        }
    }

    private void showActivityLogs() {
        String[] cols = {"Thời gian", "User", "Hành động", "Mô tả"};
        DefaultTableModel logModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = "SELECT TOP 100 l.created_at, u.username, l.action_type, l.description "
                   + "FROM activity_logs l "
                   + "JOIN users u ON l.user_id = u.user_id "
                   + "ORDER BY l.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                logModel.addRow(new Object[]{
                    rs.getTimestamp("created_at"),
                    rs.getString("username"),
                    rs.getString("action_type"),
                    rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi đọc log (kiểm tra bảng activity_logs / cột action_type):\n" + e.getMessage());
            return;
        }

        JTable t = new JTable(logModel);
        t.setRowHeight(28);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(750, 360));
        JOptionPane.showMessageDialog(this, sp, "Lịch sử hoạt động", JOptionPane.PLAIN_MESSAGE);
    }

    private void changeStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản!");
            return;
        }
        int userId = (int) table.getValueAt(row, 0);
        if (userId == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "Không thể đổi trạng thái của chính mình!");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET status = ?, updated_at = GETDATE() WHERE user_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
            logActivity(currentUser.getUserId(), "ADMIN_CHANGE_STATUS",
                    "Đổi status user_id=" + userId + " -> " + status);
            loadUsers(txtSearch.getText().trim());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void assignRole(String roleName) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản!");
            return;
        }
        int userId = (int) table.getValueAt(row, 0);
        String sql = "IF NOT EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON ur.role_id = r.role_id "
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
            logActivity(currentUser.getUserId(), "ADMIN_ASSIGN_ROLE",
                    "Cấp " + roleName + " cho user_id=" + userId);
            JOptionPane.showMessageDialog(this, "Cấp quyền " + roleName + " thành công!");
            loadUsers(txtSearch.getText().trim());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeRole(String roleName) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản!");
            return;
        }
        int userId = (int) table.getValueAt(row, 0);
        if (userId == currentUser.getUserId() && "ADMIN".equalsIgnoreCase(roleName)) {
            JOptionPane.showMessageDialog(this, "Không thể tự thu hồi Admin của chính mình!");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE ur FROM user_roles ur JOIN roles r ON ur.role_id = r.role_id "
                   + "WHERE ur.user_id = ? AND r.role_name = ?")) {
            ps.setInt(1, userId);
            ps.setString(2, roleName);
            ps.executeUpdate();
            logActivity(currentUser.getUserId(), "ADMIN_REMOVE_ROLE",
                    "Thu hồi " + roleName + " của user_id=" + userId);
            JOptionPane.showMessageDialog(this, "Đã thu hồi quyền " + roleName + "!");
            loadUsers(txtSearch.getText().trim());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logActivity(int userId, String action, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description, created_at) "
                   + "VALUES (?, ?, ?, GETDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
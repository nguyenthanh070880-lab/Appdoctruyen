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
        setSize(1200, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        JLabel lbl = new JLabel("👥  Quản lý người dùng & Phân quyền");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);

        String[] columns = {"ID", "Username", "Họ tên", "Email", "Trạng thái", "Role", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnLock = createBtn("Khóa", new Color(220, 53, 69));
        JButton btnUnlock = createBtn("Mở khóa", new Color(0, 153, 76));
        JButton btnSetAuthor = createBtn("Cấp Author", null);
        JButton btnRemoveAuthor = createBtn("Thu hồi Author", null);
        JButton btnSetStaff = createBtn("Cấp Staff", null);
        JButton btnRemoveStaff = createBtn("Thu hồi Staff", null);
        JButton btnSetAdmin = createBtn("Cấp Admin", new Color(255, 140, 0));
        JButton btnRefresh = createBtn("Làm mới", null);
        JButton btnBack = createBtn("← Dashboard", null);

        btnLock.addActionListener(e -> changeStatus("LOCKED"));
        btnUnlock.addActionListener(e -> changeStatus("ACTIVE"));
        btnSetAuthor.addActionListener(e -> assignRole("AUTHOR"));
        btnRemoveAuthor.addActionListener(e -> removeRole("AUTHOR"));
        btnSetStaff.addActionListener(e -> assignRole("STAFF"));
        btnRemoveStaff.addActionListener(e -> removeRole("STAFF"));
        btnSetAdmin.addActionListener(e -> assignRole("ADMIN"));
        btnRefresh.addActionListener(e -> loadUsers());
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

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
        if (bg != null) {
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
        }
        return btn;
    }

    private void loadUsers() {
        model.setRowCount(0);
        String sql = "SELECT u.user_id, u.username, u.full_name, u.email, u.status, u.created_at, "
                   + "STUFF((SELECT ', ' + r.role_name FROM user_roles ur "
                   + "JOIN roles r ON ur.role_id = r.role_id WHERE ur.user_id = u.user_id FOR XML PATH('')), 1, 2, '') AS roles "
                   + "FROM users u WHERE u.is_deleted = 0 ORDER BY u.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"), rs.getString("username"), rs.getString("full_name"),
                    rs.getString("email"), rs.getString("status"), rs.getString("roles"),
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản!");
            return;
        }
        int userId = (int) table.getValueAt(row, 0);
        if (userId == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "Không thể khóa chính mình!");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET status = ?, updated_at = GETDATE() WHERE user_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
            loadUsers();
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
                   + "INSERT INTO user_roles (user_id, role_id) SELECT ?, role_id FROM roles WHERE role_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, roleName);
            ps.setInt(3, userId);
            ps.setString(4, roleName);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cấp quyền " + roleName + " thành công!");
            loadUsers();
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
            JOptionPane.showMessageDialog(this, "Không thể tự thu hồi quyền Admin của chính mình!");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE ur FROM user_roles ur JOIN roles r ON ur.role_id = r.role_id "
                   + "WHERE ur.user_id = ? AND r.role_name = ?")) {
            ps.setInt(1, userId);
            ps.setString(2, roleName);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Đã thu hồi quyền " + roleName + "!");
            loadUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.NotificationDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminNotificationFrame extends JFrame {

    private final User currentUser;
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private DefaultTableModel model;

    public AdminNotificationFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        setTitle("Quản lý Thông báo - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🔔  Quản lý Thông báo hệ thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Dashboard");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "User ID", "Tiêu đề", "Nội dung", "Loại", "Đã đọc", "Thời gian"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnSendOne = new JButton("Gửi cho 1 user");
        btnSendOne.setBackground(new Color(0, 102, 204));
        btnSendOne.setForeground(Color.WHITE);
        btnSendOne.setFocusPainted(false);
        btnSendOne.setBorderPainted(false);
        btnSendOne.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSendOne.addActionListener(e -> sendToOneUser());

        JButton btnSendAll = new JButton("Gửi cho tất cả");
        btnSendAll.setBackground(new Color(0, 153, 76));
        btnSendAll.setForeground(Color.WHITE);
        btnSendAll.setFocusPainted(false);
        btnSendAll.setBorderPainted(false);
        btnSendAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSendAll.addActionListener(e -> sendToAllUsers());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadNotifications());

        bottom.add(btnSendOne);
        bottom.add(btnSendAll);
        bottom.add(btnRefresh);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadNotifications() {
        model.setRowCount(0);
        String sql = "SELECT TOP 100 notification_id, user_id, title, content, type, is_read, created_at "
                   + "FROM notifications ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("notification_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("type"),
                    rs.getBoolean("is_read") ? "Đã đọc" : "Chưa đọc",
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendToOneUser() {
        JTextField txtUsername = new JTextField(15);
        JTextField txtTitle = new JTextField(20);
        JTextArea txtContent = new JTextArea(4, 25);
        txtContent.setLineWrap(true);
        txtContent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel panel = new JPanel(new BorderLayout(5, 8));
        JPanel top = new JPanel(new GridLayout(2, 2, 8, 8));
        top.add(new JLabel("Username:"));
        top.add(txtUsername);
        top.add(new JLabel("Tiêu đề:"));
        top.add(txtTitle);
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JLabel("Nội dung:"), BorderLayout.CENTER);
        panel.add(new JScrollPane(txtContent), BorderLayout.SOUTH);

        // Fix layout
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("Username:"));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Tiêu đề:"));
        form.add(txtTitle);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Nội dung:"));
        form.add(new JScrollPane(txtContent));

        int result = JOptionPane.showConfirmDialog(this, form, "Gửi thông báo cho 1 user",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String username = txtUsername.getText().trim();
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();

        if (username.isEmpty() || title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ!");
            return;
        }

        int userId = findUserId(username);
        if (userId < 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy user!");
            return;
        }

        notificationDAO.createNotification(userId, title, content, "SYSTEM");
        JOptionPane.showMessageDialog(this, "Đã gửi thông báo!");
        loadNotifications();
    }

    private void sendToAllUsers() {
        JTextField txtTitle = new JTextField(20);
        JTextArea txtContent = new JTextArea(4, 25);
        txtContent.setLineWrap(true);
        txtContent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(new JLabel("Tiêu đề:"));
        form.add(txtTitle);
        form.add(Box.createVerticalStrut(8));
        form.add(new JLabel("Nội dung:"));
        form.add(new JScrollPane(txtContent));

        int result = JOptionPane.showConfirmDialog(this, form, "Gửi thông báo cho TẤT CẢ user",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ!");
            return;
        }

        int count = 0;
        String sql = "SELECT user_id FROM users WHERE is_deleted = 0 AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                notificationDAO.createNotification(rs.getInt("user_id"), title, content, "SYSTEM");
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(this, "Đã gửi thông báo cho " + count + " user!");
        loadNotifications();
    }

    private int findUserId(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
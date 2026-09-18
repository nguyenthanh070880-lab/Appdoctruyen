package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminTransactionFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;

    public AdminTransactionFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadTransactions();
    }

    private void initComponents() {
        setTitle("Quản lý Giao dịch & Coin - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 620);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("💳  Quản lý Giao dịch & Coin");
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
        String[] columns = {"ID", "User", "Loại", "Số lượng", "Số dư sau", "Mô tả", "Thời gian"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnAdjust = new JButton("Cộng / Trừ Coin thủ công");
        btnAdjust.setBackground(new Color(0, 153, 76));
        btnAdjust.setForeground(Color.WHITE);
        btnAdjust.setFocusPainted(false);
        btnAdjust.setBorderPainted(false);
        btnAdjust.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdjust.addActionListener(e -> adjustCoin());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadTransactions());

        bottom.add(btnAdjust);
        bottom.add(btnRefresh);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadTransactions() {
        model.setRowCount(0);
        String sql = "SELECT TOP 200 ct.transaction_id, u.username, ct.type, ct.amount, "
                   + "ct.balance_after, ct.description, ct.created_at "
                   + "FROM coin_transactions ct "
                   + "JOIN users u ON ct.user_id = u.user_id "
                   + "ORDER BY ct.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getLong(4),
                    rs.getLong(5),
                    rs.getString(6),
                    rs.getTimestamp(7)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void adjustCoin() {
        JTextField txtUsername = new JTextField(15);
        JTextField txtAmount = new JTextField(10);
        JTextField txtReason = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Username:"));
        panel.add(txtUsername);
        panel.add(new JLabel("Số Coin (+/-):"));
        panel.add(txtAmount);
        panel.add(new JLabel("Lý do:"));
        panel.add(txtReason);

        int result = JOptionPane.showConfirmDialog(this, panel, "Điều chỉnh Coin",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String username = txtUsername.getText().trim();
        String amountStr = txtAmount.getText().trim();
        String reason = txtReason.getText().trim();

        if (username.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số Coin không hợp lệ!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int userId = -1;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id FROM users WHERE username = ? AND is_deleted = 0")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) userId = rs.getInt(1);
                }
            }
            if (userId < 0) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy user!");
                return;
            }

            // Cập nhật ví
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE wallets SET balance = balance + ?, updated_at = GETDATE() WHERE user_id = ?")) {
                ps.setLong(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }

            long newBalance = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM wallets WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) newBalance = rs.getLong(1);
                }
            }

            // Ghi log
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) "
                  + "VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setLong(2, amount);
                ps.setLong(3, newBalance);
                ps.setString(4, amount >= 0 ? "ADMIN_ADD" : "ADMIN_DEDUCT");
                ps.setString(5, reason.isEmpty() ? "Admin điều chỉnh" : reason);
                ps.executeUpdate();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Điều chỉnh thành công!\nSố dư mới: " + newBalance + " Coin");
            loadTransactions();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
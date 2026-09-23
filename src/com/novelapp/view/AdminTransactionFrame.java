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
            @Override
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
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

        JButton btnAdjust = createBtn("Cộng / Trừ Coin thủ công", new Color(0, 153, 76));
        btnAdjust.addActionListener(e -> adjustCoin());

        JButton btnRefund = createBtn("Hoàn tiền (cộng Coin)", new Color(255, 152, 0));
        btnRefund.addActionListener(e -> refundCoin());

        JButton btnRefresh = createBtn("Làm mới", new Color(100, 100, 100));
        btnRefresh.addActionListener(e -> loadTransactions());

        bottom.add(btnAdjust);
        bottom.add(btnRefund);
        bottom.add(btnRefresh);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
            JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử giao dịch: " + e.getMessage());
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

        String type = amount >= 0 ? "ADMIN_ADD" : "ADMIN_DEDUCT";
        String defaultReason = reason.isEmpty() ? "Admin điều chỉnh" : reason;

        processCoinTransaction(username, amount, type, defaultReason);
    }

    private void refundCoin() {
        JTextField txtUser = new JTextField(12);
        JTextField txtAmount = new JTextField(10);
        JTextField txtReason = new JTextField(20);

        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(new JLabel("Username:")); 
        p.add(txtUser);
        p.add(new JLabel("Số Coin hoàn:")); 
        p.add(txtAmount);
        p.add(new JLabel("Lý do hoàn:")); 
        p.add(txtReason);

        if (JOptionPane.showConfirmDialog(this, p, "Hoàn tiền cho User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        String username = txtUser.getText().trim();
        String amountStr = txtAmount.getText().trim();
        String reason = txtReason.getText().trim();

        if (username.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Số Coin hoàn trả phải lớn hơn 0!");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số Coin không hợp lệ!");
            return;
        }

        String defaultReason = reason.isEmpty() ? "Hoàn tiền từ Admin" : reason;

        processCoinTransaction(username, amount, "REFUND", defaultReason);
    }

    private void processCoinTransaction(String username, long amount, String type, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Kiểm tra User có tồn tại hay không
                int userId = -1;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT user_id FROM users WHERE username = ? AND is_deleted = 0")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) userId = rs.getInt(1);
                    }
                }

                if (userId < 0) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng!");
                    conn.rollback();
                    return;
                }

                // 2. Cập nhật Ví (Wallets)
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE wallets SET balance = balance + ?, updated_at = GETDATE() WHERE user_id = ?")) {
                    ps.setLong(1, amount);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }

                // 3. Lấy số dư mới sau khi cập nhật
                long newBalance = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT balance FROM wallets WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) newBalance = rs.getLong(1);
                    }
                }

                // Kiểm tra nếu số dư bị âm sau khi trừ
                if (newBalance < 0) {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại: Số dư tài khoản không đủ để trừ!");
                    conn.rollback();
                    return;
                }

                // 4. Lưu Nhật ký Giao dịch (coin_transactions)
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) "
                      + "VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setLong(2, amount);
                    ps.setLong(3, newBalance);
                    ps.setString(4, type);
                    ps.setString(5, description);
                    ps.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Thực hiện thành công!\nSố dư mới của " + username + ": " + newBalance + " Coin");
                loadTransactions();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + e.getMessage());
        }
    }
}
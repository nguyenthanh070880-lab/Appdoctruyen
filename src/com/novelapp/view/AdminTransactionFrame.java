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
    
    // Tab 1: Biến động Coin (coin_transactions)
    private DefaultTableModel modelTransactions;
    
    // Tab 2: Lịch sử Đơn nạp (payment_orders)
    private DefaultTableModel modelOrders;
    private JComboBox<String> cboStatus;
    private JTextField txtFromDate;
    private JTextField txtToDate;

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
        loadPaymentOrders();
    }

    private void initComponents() {
        setTitle("Quản lý Giao dịch & Coin - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 680);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("💳 Quản lý Giao dịch & Nạp Coin");
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

        // --- TABBED PANE ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Lịch sử Biến động Coin", createTransactionsPanel());
        tabbedPane.addTab("Quản lý Đơn nạp (payment_orders)", createPaymentOrdersPanel());

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    // ==========================================
    // TAB 1: COIN TRANSACTIONS
    // ==========================================
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = {"ID", "User", "Loại", "Số lượng", "Số dư sau", "Mô tả", "Thời gian"};
        modelTransactions = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(modelTransactions);
        styleTable(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnAdjust = createBtn("Cộng / Trừ Coin thủ công", new Color(0, 153, 76));
        btnAdjust.addActionListener(e -> adjustCoin());

        JButton btnRefund = createBtn("Hoàn tiền (cộng Coin)", new Color(255, 152, 0));
        btnRefund.addActionListener(e -> refundCoin());

        JButton btnReconcile = createBtn("Đối soát tổng quan", new Color(0, 102, 204));
        btnReconcile.addActionListener(e -> showReconcile());

        JButton btnRefresh = createBtn("Làm mới", new Color(100, 100, 100));
        btnRefresh.addActionListener(e -> loadTransactions());

        bottom.add(btnAdjust);
        bottom.add(btnRefund);
        bottom.add(btnReconcile);
        bottom.add(btnRefresh);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // TAB 2: PAYMENT ORDERS & FILTER
    // ==========================================
    private JPanel createPaymentOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterPanel.setBackground(new Color(245, 247, 250));
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        cboStatus = new JComboBox<>(new String[]{"Tất cả", "PENDING", "SUCCESS", "FAILED", "CANCELLED"});
        txtFromDate = new JTextField(9);
        txtToDate = new JTextField(9);

        // Tooltip hướng dẫn định dạng ngày
        txtFromDate.setToolTipText("Định dạng: yyyy-MM-dd");
        txtToDate.setToolTipText("Định dạng: yyyy-MM-dd");

        JButton btnFilter = createBtn("🔍 Lọc đơn", new Color(0, 102, 204));
        btnFilter.addActionListener(e -> loadPaymentOrders());

        JButton btnResetFilter = createBtn("Đặt lại", new Color(120, 120, 120));
        btnResetFilter.addActionListener(e -> {
            cboStatus.setSelectedIndex(0);
            txtFromDate.setText("");
            txtToDate.setText("");
            loadPaymentOrders();
        });

        filterPanel.add(new JLabel("Trạng thái:"));
        filterPanel.add(cboStatus);
        filterPanel.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        filterPanel.add(txtFromDate);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(txtToDate);
        filterPanel.add(btnFilter);
        filterPanel.add(btnResetFilter);

        // Table
        String[] columns = {"Mã Đơn", "User", "Số Coin", "Số tiền (VNĐ)", "Trạng thái", "Thời gian"};
        modelOrders = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tableOrders = new JTable(modelOrders);
        styleTable(tableOrders);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableOrders), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // SQL DATA LOADERS
    // ==========================================
    private void loadTransactions() {
        modelTransactions.setRowCount(0);
        String sql = "SELECT TOP 200 ct.transaction_id, u.username, ct.type, ct.amount, "
                   + "ct.balance_after, ct.description, ct.created_at "
                   + "FROM coin_transactions ct "
                   + "JOIN users u ON ct.user_id = u.user_id "
                   + "ORDER BY ct.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelTransactions.addRow(new Object[]{
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
            JOptionPane.showMessageDialog(this, "Lỗi tải giao dịch: " + e.getMessage());
        }
    }

    private void loadPaymentOrders() {
        modelOrders.setRowCount(0);

        String status = (String) cboStatus.getSelectedItem();
        String fromDate = txtFromDate.getText().trim();
        String toDate = txtToDate.getText().trim();

        String sql = "SELECT po.order_code, u.username, po.coin_amount, po.amount_vnd, po.status, po.created_at "
                   + "FROM payment_orders po "
                   + "JOIN users u ON po.user_id = u.user_id "
                   + "WHERE (? = 'Tất cả' OR po.status = ?) "
                   + "  AND (? = '' OR CAST(po.created_at AS DATE) >= ?) "
                   + "  AND (? = '' OR CAST(po.created_at AS DATE) <= ?) "
                   + "ORDER BY po.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, fromDate);
            ps.setString(4, fromDate);
            ps.setString(5, toDate);
            ps.setString(6, toDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelOrders.addRow(new Object[]{
                        rs.getString("order_code"),
                        rs.getString("username"),
                        rs.getLong("coin_amount"),
                        String.format("%,d", rs.getLong("amount_vnd")),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi lọc đơn nạp: " + e.getMessage());
        }
    }

    // ==========================================
    // HELPER ACTIONS & METHODS
    // ==========================================
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

        if (JOptionPane.showConfirmDialog(this, panel, "Điều chỉnh Coin",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

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
        String desc = reason.isEmpty() ? "Admin điều chỉnh" : reason;
        processCoinTransaction(username, amount, type, desc);
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
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

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
                JOptionPane.showMessageDialog(this, "Số Coin hoàn phải > 0!");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số Coin không hợp lệ!");
            return;
        }

        String desc = reason.isEmpty() ? "Hoàn tiền từ Admin" : reason;
        processCoinTransaction(username, amount, "REFUND", desc);
    }

    private void processCoinTransaction(String username, long amount, String type, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
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

                if (newBalance < 0) {
                    JOptionPane.showMessageDialog(this, "Thất bại: số dư không đủ!");
                    conn.rollback();
                    return;
                }

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
                JOptionPane.showMessageDialog(this,
                        "Thành công!\nSố dư mới của " + username + ": " + newBalance + " Coin");
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

    private void showReconcile() {
        String sql = "SELECT "
                   + "(SELECT COUNT(*) FROM payment_orders WHERE status = 'SUCCESS') AS ok_orders, "
                   + "(SELECT COUNT(*) FROM payment_orders WHERE status = 'FAILED') AS fail_orders, "
                   + "(SELECT COUNT(*) FROM payment_orders WHERE status = 'PENDING') AS pending_orders, "
                   + "(SELECT COUNT(*) FROM payment_orders WHERE status = 'CANCELLED') AS cancel_orders, "
                   + "(SELECT ISNULL(SUM(amount_vnd),0) FROM payment_orders WHERE status = 'SUCCESS') AS total_vnd, "
                   + "(SELECT ISNULL(SUM(coin_amount),0) FROM payment_orders WHERE status = 'SUCCESS') AS total_coin";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String msg = String.format(
                    "ĐỐI SOÁT NẠP COIN (payment_orders)\n\n"
                  + "SUCCESS:   %d\n"
                  + "FAILED:    %d\n"
                  + "PENDING:   %d\n"
                  + "CANCELLED: %d\n\n"
                  + "Tổng tiền SUCCESS: %,d VNĐ\n"
                  + "Tổng Coin đã cấp:  %,d",
                    rs.getInt("ok_orders"),
                    rs.getInt("fail_orders"),
                    rs.getInt("pending_orders"),
                    rs.getInt("cancel_orders"),
                    rs.getLong("total_vnd"),
                    rs.getLong("total_coin")
                );
                JOptionPane.showMessageDialog(this, msg, "Đối soát", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi đối soát (kiểm tra bảng payment_orders):\n" + e.getMessage());
        }
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

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
    }
}
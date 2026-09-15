package com.novelapp.view;

import com.novelapp.dao.WalletDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import com.novelapp.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WalletFrame extends JFrame {

    private final User currentUser;
    private final WalletDAO walletDAO = new WalletDAO();
    private JLabel lblBalance;

    public WalletFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadTransactions();
    }

    private void initComponents() {
        setTitle("Ví Coin - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // ===== Số dư =====
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        balancePanel.setBackground(new Color(0, 102, 204));
        balancePanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Số dư hiện tại:");
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTitle.setForeground(Color.WHITE);

        lblBalance = new JLabel(walletDAO.getBalance(currentUser.getUserId()) + " Coin");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBalance.setForeground(Color.WHITE);

        JButton btnNap = new JButton("Nạp Coin");
        btnNap.setFocusPainted(false);
        
        // Đã cập nhật sự kiện mở DepositFrame theo yêu cầu của bạn
        btnNap.addActionListener(e -> {
            new DepositFrame().setVisible(true);
            this.dispose();
        });

        balancePanel.add(lblTitle);
        balancePanel.add(lblBalance);
        balancePanel.add(Box.createHorizontalStrut(30));
        balancePanel.add(btnNap);

        // ===== Lịch sử giao dịch =====
        JLabel lblHistory = new JLabel("Lịch sử giao dịch Coin");
        lblHistory.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String[] columns = {"Thời gian", "Loại", "Số lượng", "Số dư sau", "Mô tả"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);

        // Nút quay lại
        JButton btnBack = new JButton("Quay lại trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        // Ghép layout
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(lblHistory, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(balancePanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Load dữ liệu vào bảng
        loadDataToTable(model);
    }

    private void loadTransactions() {
        // Đã gọi trong constructor
    }

    private void loadDataToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT TOP 50 created_at, type, amount, balance_after, description "
                   + "FROM coin_transactions WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentUser.getUserId());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getTimestamp("created_at"),
                        rs.getString("type"),
                        rs.getLong("amount"),
                        rs.getLong("balance_after"),
                        rs.getString("description")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

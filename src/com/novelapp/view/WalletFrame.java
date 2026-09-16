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
    }

    private void initComponents() {
        setTitle("Ví Coin - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("💰  Ví Coin");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnBack, BorderLayout.EAST);

        // Balance card
        JPanel balancePanel = new JPanel(new BorderLayout());
        balancePanel.setBackground(new Color(0, 102, 204));
        balancePanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblBalanceTitle = new JLabel("Số dư hiện tại");
        lblBalanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblBalanceTitle.setForeground(new Color(200, 220, 255));

        lblBalance = new JLabel(walletDAO.getBalance(currentUser.getUserId()) + " Coin");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblBalance.setForeground(Color.WHITE);

        JButton btnNap = new JButton("Nạp Coin");
        btnNap.setPreferredSize(new Dimension(130, 42));
        btnNap.setBackground(Color.WHITE);
        btnNap.setForeground(new Color(0, 102, 204));
        btnNap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNap.setFocusPainted(false);
        btnNap.setBorderPainted(false);
        btnNap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNap.addActionListener(e -> {
            new DepositFrame().setVisible(true);
            this.dispose();
        });

        JPanel leftBal = new JPanel();
        leftBal.setLayout(new BoxLayout(leftBal, BoxLayout.Y_AXIS));
        leftBal.setOpaque(false);
        leftBal.add(lblBalanceTitle);
        leftBal.add(Box.createVerticalStrut(6));
        leftBal.add(lblBalance);

        balancePanel.add(leftBal, BorderLayout.WEST);
        balancePanel.add(btnNap, BorderLayout.EAST);

        // History
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblHistory = new JLabel("Lịch sử giao dịch");
        lblHistory.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String[] columns = {"Thời gian", "Loại", "Số lượng", "Số dư sau", "Mô tả"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        loadTransactions(model);

        historyPanel.add(lblHistory, BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.add(balancePanel, BorderLayout.NORTH);
        center.add(historyPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadTransactions(DefaultTableModel model) {
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
package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class AdminStatsFrame extends JFrame {

    private final User currentUser;

    public AdminStatsFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Thống kê hệ thống - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Thống kê tổng quan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // ===== Grid thống kê =====
        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        statsPanel.add(createStatCard("Tổng người dùng", getCount("SELECT COUNT(*) FROM users WHERE is_deleted = 0"), new Color(0, 102, 204)));
        statsPanel.add(createStatCard("Tổng truyện", getCount("SELECT COUNT(*) FROM stories WHERE is_deleted = 0"), new Color(40, 167, 69)));
        statsPanel.add(createStatCard("Tổng chương", getCount("SELECT COUNT(*) FROM chapters WHERE is_deleted = 0"), new Color(255, 193, 7)));
        statsPanel.add(createStatCard("Truyện chờ duyệt", getCount("SELECT COUNT(*) FROM stories WHERE moderation_status = 'PENDING' AND is_deleted = 0"), new Color(220, 53, 69)));
        statsPanel.add(createStatCard("Chương chờ duyệt", getCount("SELECT COUNT(*) FROM chapters WHERE moderation_status = 'PENDING' AND is_deleted = 0"), new Color(108, 117, 125)));
        statsPanel.add(createStatCard("Tổng lượt xem", getCount("SELECT ISNULL(SUM(view_count), 0) FROM stories"), new Color(111, 66, 193)));

        // ===== Doanh thu Coin =====
        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        revenuePanel.setBackground(new Color(248, 249, 250));
        revenuePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        long totalDeposit = getLong("SELECT ISNULL(SUM(amount), 0) FROM coin_transactions WHERE type = 'DEPOSIT'");
        long totalUnlock = getLong("SELECT ISNULL(SUM(ABS(amount)), 0) FROM coin_transactions WHERE type = 'UNLOCK'");

        JLabel lblRevenue = new JLabel(String.format(
                "<html><b>Doanh thu nạp Coin:</b> %,d Coin &nbsp;&nbsp;|&nbsp;&nbsp; <b>Coin đã tiêu (mở khóa):</b> %,d Coin</html>",
                totalDeposit, totalUnlock
        ));
        lblRevenue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        revenuePanel.add(lblRevenue);

        // ===== Nút quay lại =====
        JButton btnBack = new JButton("Quay lại Dashboard");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        // Ghép
        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.CENTER);
        centerPanel.add(revenuePanel, BorderLayout.SOUTH);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createStatCard(String title, long value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(String.format("%,d", value));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(color);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private long getCount(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getLong(String sql) {
        return getCount(sql);
    }
}
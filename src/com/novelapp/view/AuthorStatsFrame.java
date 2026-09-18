package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AuthorStatsFrame extends JFrame {

    private final User currentUser;

    public AuthorStatsFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("AUTHOR") && !currentUser.hasRole("ADMIN"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Author!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Thống kê & Doanh thu - Tác giả");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("📊  Thống kê & Doanh thu của tôi");
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

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        // Tổng quan
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        long totalViews = getLong("SELECT ISNULL(SUM(view_count), 0) FROM stories WHERE author_id = ? AND is_deleted = 0");
        long totalStories = getLong("SELECT COUNT(*) FROM stories WHERE author_id = ? AND is_deleted = 0");
        long totalChapters = getLong(
            "SELECT COUNT(*) FROM chapters c JOIN stories s ON c.story_id = s.story_id " +
            "WHERE s.author_id = ? AND c.is_deleted = 0 AND s.is_deleted = 0");
        long totalRevenue = getLong(
            "SELECT ISNULL(SUM(ABS(ct.amount)), 0) FROM coin_transactions ct " +
            "JOIN chapter_access ca ON ct.description LIKE '%' + CAST(ca.chapter_id AS VARCHAR) + '%' " +
            "JOIN chapters c ON ca.chapter_id = c.chapter_id " +
            "JOIN stories s ON c.story_id = s.story_id " +
            "WHERE s.author_id = ? AND ct.type = 'UNLOCK'");

        // Doanh thu đơn giản hơn: tính từ chapter_access + price
        totalRevenue = getRevenue();

        summaryPanel.add(createStatCard("Tổng truyện", totalStories, new Color(0, 102, 204)));
        summaryPanel.add(createStatCard("Tổng chương", totalChapters, new Color(40, 167, 69)));
        summaryPanel.add(createStatCard("Tổng lượt xem", totalViews, new Color(255, 193, 7)));
        summaryPanel.add(createStatCard("Doanh thu (Coin)", totalRevenue, new Color(220, 53, 69)));

        // Bảng chi tiết theo truyện
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        JLabel lblDetail = new JLabel("Chi tiết theo từng truyện");
        lblDetail.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String[] columns = {"ID", "Tên truyện", "Lượt xem", "Số chương", "Lượt mở khóa", "Doanh thu (Coin)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        loadStoryStats(model);

        tablePanel.add(lblDetail, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(summaryPanel, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createStatCard(String title, long value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(String.format("%,d", value));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(color);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private long getLong(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getRevenue() {
        // Tính doanh thu = số lần mở khóa * giá coin của chương
        String sql = "SELECT ISNULL(SUM(c.price_coin), 0) "
                   + "FROM chapter_access ca "
                   + "JOIN chapters c ON ca.chapter_id = c.chapter_id "
                   + "JOIN stories s ON c.story_id = s.story_id "
                   + "WHERE s.author_id = ? AND ca.access_type = 'PURCHASE' AND c.is_free = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void loadStoryStats(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT s.story_id, s.title, s.view_count, "
                   + "(SELECT COUNT(*) FROM chapters c WHERE c.story_id = s.story_id AND c.is_deleted = 0) AS chapter_count, "
                   + "(SELECT COUNT(*) FROM chapter_access ca "
                   + " JOIN chapters c2 ON ca.chapter_id = c2.chapter_id "
                   + " WHERE c2.story_id = s.story_id AND ca.access_type = 'PURCHASE') AS unlock_count, "
                   + "(SELECT ISNULL(SUM(c3.price_coin), 0) FROM chapter_access ca2 "
                   + " JOIN chapters c3 ON ca2.chapter_id = c3.chapter_id "
                   + " WHERE c3.story_id = s.story_id AND ca2.access_type = 'PURCHASE' AND c3.is_free = 0) AS revenue "
                   + "FROM stories s "
                   + "WHERE s.author_id = ? AND s.is_deleted = 0 "
                   + "ORDER BY s.view_count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("story_id"),
                        rs.getString("title"),
                        rs.getLong("view_count"),
                        rs.getInt("chapter_count"),
                        rs.getInt("unlock_count"),
                        rs.getLong("revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
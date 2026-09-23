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
        setSize(1200, 750);
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

        // --- 1. TỔNG QUAN (5 Thẻ Thống Kê) ---
        JPanel summaryPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        long totalViews = getLong("SELECT ISNULL(SUM(view_count), 0) FROM stories WHERE author_id = ? AND is_deleted = 0");
        long totalStories = getLong("SELECT COUNT(*) FROM stories WHERE author_id = ? AND is_deleted = 0");
        long totalChapters = getLong(
            "SELECT COUNT(*) FROM chapters c JOIN stories s ON c.story_id = s.story_id " +
            "WHERE s.author_id = ? AND c.is_deleted = 0 AND s.is_deleted = 0");
        long totalRevenue = getRevenue();
        long totalFollows = getTotalFollows();

        summaryPanel.add(createStatCard("Tổng truyện", totalStories, new Color(0, 102, 204)));
        summaryPanel.add(createStatCard("Tổng chương", totalChapters, new Color(40, 167, 69)));
        summaryPanel.add(createStatCard("Tổng lượt xem", totalViews, new Color(255, 193, 7)));
        summaryPanel.add(createStatCard("Lượt theo dõi", totalFollows, new Color(102, 16, 242)));
        summaryPanel.add(createStatCard("Doanh thu (Coin)", totalRevenue, new Color(220, 53, 69)));

        // --- 2. BẢNG CHI TIẾT THEO TRUYỆN ---
        JPanel storyTablePanel = new JPanel(new BorderLayout(0, 8));
        storyTablePanel.setBackground(Color.WHITE);
        storyTablePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblDetail = new JLabel("Chi tiết theo từng truyện");
        lblDetail.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] columns = {"ID", "Tên truyện", "Lượt xem", "Số chương", "Lượt mở khóa", "Doanh thu (Coin)"};
        DefaultTableModel storyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable storyTable = new JTable(storyModel);
        setupTableStyle(storyTable);
        storyTable.getColumnModel().getColumn(0).setMinWidth(0);
        storyTable.getColumnModel().getColumn(0).setMaxWidth(0);

        loadStoryStats(storyModel);

        storyTablePanel.add(lblDetail, BorderLayout.NORTH);
        storyTablePanel.add(new JScrollPane(storyTable), BorderLayout.CENTER);

        // --- 3. BẢNG DOANH THU THEO THÁNG ---
        JPanel monthTablePanel = new JPanel(new BorderLayout(0, 8));
        monthTablePanel.setBackground(Color.WHITE);
        monthTablePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblMonth = new JLabel("Doanh thu theo tháng");
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] monthCols = {"Năm", "Tháng", "Doanh thu (Coin)"};
        DefaultTableModel monthModel = new DefaultTableModel(monthCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable monthTable = new JTable(monthModel);
        setupTableStyle(monthTable);

        loadMonthlyRevenue(monthModel);

        monthTablePanel.add(lblMonth, BorderLayout.NORTH);
        monthTablePanel.add(new JScrollPane(monthTable), BorderLayout.CENTER);

        // --- 4. BẢNG DOANH THU THEO NGÀY ---
        JPanel dayTablePanel = new JPanel(new BorderLayout(0, 8));
        dayTablePanel.setBackground(Color.WHITE);
        dayTablePanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblDay = new JLabel("Doanh thu theo ngày");
        lblDay.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] dayCols = {"Ngày", "Doanh thu (Coin)"};
        DefaultTableModel dayModel = new DefaultTableModel(dayCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable dayTable = new JTable(dayModel);
        setupTableStyle(dayTable);

        loadDailyRevenue(dayModel);

        dayTablePanel.add(lblDay, BorderLayout.NORTH);
        dayTablePanel.add(new JScrollPane(dayTable), BorderLayout.CENTER);

        // --- BỐ CỤC CHIA 3 BẢNG ---
        JPanel tablesContainer = new JPanel(new GridLayout(1, 3, 15, 0));
        tablesContainer.setOpaque(false);
        tablesContainer.setBorder(new EmptyBorder(0, 25, 20, 25));
        tablesContainer.add(storyTablePanel);
        tablesContainer.add(monthTablePanel);
        tablesContainer.add(dayTablePanel);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(summaryPanel, BorderLayout.NORTH);
        center.add(tablesContainer, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void setupTableStyle(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
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
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
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

    private long getTotalFollows() {
        String sql = "SELECT COUNT(*) FROM follows f "
                   + "JOIN stories s ON f.story_id = s.story_id "
                   + "WHERE s.author_id = ? AND ISNULL(s.is_deleted, 0) = 0";
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
        String sql = "SELECT ISNULL(SUM(c.price_coin), 0) "
                   + "FROM chapter_access ca "
                   + "JOIN chapters c ON ca.chapter_id = c.chapter_id "
                   + "JOIN stories s ON c.story_id = s.story_id "
                   + "WHERE s.author_id = ? AND ca.access_type = 'PURCHASE' AND ISNULL(c.is_free, 0) = 0";
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
                   + "(SELECT COUNT(*) FROM chapters c WHERE c.story_id = s.story_id AND ISNULL(c.is_deleted, 0) = 0) AS chapter_count, "
                   + "(SELECT COUNT(*) FROM chapter_access ca "
                   + " JOIN chapters c2 ON ca.chapter_id = c2.chapter_id "
                   + " WHERE c2.story_id = s.story_id AND ca.access_type = 'PURCHASE') AS unlock_count, "
                   + "(SELECT ISNULL(SUM(c3.price_coin), 0) FROM chapter_access ca2 "
                   + " JOIN chapters c3 ON ca2.chapter_id = c3.chapter_id "
                   + " WHERE c3.story_id = s.story_id AND ca2.access_type = 'PURCHASE' AND ISNULL(c3.is_free, 0) = 0) AS revenue "
                   + "FROM stories s "
                   + "WHERE s.author_id = ? AND ISNULL(s.is_deleted, 0) = 0 "
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

    private void loadMonthlyRevenue(DefaultTableModel monthModel) {
        monthModel.setRowCount(0);
        String sqlMonth = "SELECT YEAR(ca.created_at) AS y, MONTH(ca.created_at) AS m, "
                        + "ISNULL(SUM(c.price_coin), 0) AS revenue "
                        + "FROM chapter_access ca "
                        + "JOIN chapters c ON ca.chapter_id = c.chapter_id "
                        + "JOIN stories s ON c.story_id = s.story_id "
                        + "WHERE s.author_id = ? AND ca.access_type = 'PURCHASE' AND ISNULL(c.is_free, 0) = 0 "
                        + "GROUP BY YEAR(ca.created_at), MONTH(ca.created_at) "
                        + "ORDER BY y DESC, m DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlMonth)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    monthModel.addRow(new Object[]{
                        rs.getInt("y"),
                        rs.getInt("m"),
                        rs.getLong("revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDailyRevenue(DefaultTableModel dayModel) {
        dayModel.setRowCount(0);
        String sqlDay = "SELECT CAST(ca.created_at AS DATE) AS d, ISNULL(SUM(c.price_coin), 0) AS revenue "
                      + "FROM chapter_access ca "
                      + "JOIN chapters c ON ca.chapter_id = c.chapter_id "
                      + "JOIN stories s ON c.story_id = s.story_id "
                      + "WHERE s.author_id = ? AND ca.access_type = 'PURCHASE' AND ISNULL(c.is_free, 0) = 0 "
                      + "GROUP BY CAST(ca.created_at AS DATE) "
                      + "ORDER BY d DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDay)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dayModel.addRow(new Object[]{
                        rs.getDate("d"),
                        rs.getLong("revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
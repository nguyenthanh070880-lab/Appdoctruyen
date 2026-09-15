package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LibraryFrame extends JFrame {

    private final User currentUser;
    private JTabbedPane tabbedPane;

    public LibraryFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Thư viện của tôi - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Thư viện của tôi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Tab 1: Đang theo dõi
        tabbedPane.addTab("Đang theo dõi", createFollowedPanel());

        // Tab 2: Yêu thích
        tabbedPane.addTab("Yêu thích", createFavoritePanel());

        // Tab 3: Lịch sử đọc
        tabbedPane.addTab("Lịch sử đọc", createHistoryPanel());

        JButton btnBack = new JButton("Quay lại trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // ==================== TAB ĐANG THEO DÕI ====================
    private JPanel createFollowedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = {"ID", "Tên truyện", "Tác giả", "Trạng thái", "Ngày theo dõi"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Ẩn cột ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        loadFollowedData(model);

        // Double click để mở chi tiết
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int storyId = (int) table.getValueAt(row, 0);
                        new StoryDetailFrame(storyId).setVisible(true);
                        LibraryFrame.this.dispose();
                    }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadFollowedData(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT s.story_id, s.title, u.full_name, s.status, f.created_at "
                   + "FROM follows f "
                   + "JOIN stories s ON f.story_id = s.story_id "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE f.user_id = ? AND s.is_deleted = 0 "
                   + "ORDER BY f.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("story_id"),
                        rs.getString("title"),
                        rs.getString("full_name"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== TAB YÊU THÍCH ====================
    private JPanel createFavoritePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = {"ID", "Tên truyện", "Tác giả", "Trạng thái", "Ngày thêm"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        loadFavoriteData(model);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int storyId = (int) table.getValueAt(row, 0);
                        new StoryDetailFrame(storyId).setVisible(true);
                        LibraryFrame.this.dispose();
                    }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadFavoriteData(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT s.story_id, s.title, u.full_name, s.status, fav.created_at "
                   + "FROM favorites fav "
                   + "JOIN stories s ON fav.story_id = s.story_id "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE fav.user_id = ? AND s.is_deleted = 0 "
                   + "ORDER BY fav.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("story_id"),
                        rs.getString("title"),
                        rs.getString("full_name"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== TAB LỊCH SỬ ĐỌC ====================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columns = {"ID Truyện", "Tên truyện", "Chương đang đọc", "Lần đọc cuối"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        loadHistoryData(model);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int storyId = (int) table.getValueAt(row, 0);
                        new StoryDetailFrame(storyId).setVisible(true);
                        LibraryFrame.this.dispose();
                    }
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadHistoryData(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT s.story_id, s.title, c.title AS chapter_title, rh.last_read_at "
                   + "FROM reading_history rh "
                   + "JOIN stories s ON rh.story_id = s.story_id "
                   + "JOIN chapters c ON rh.chapter_id = c.chapter_id "
                   + "WHERE rh.user_id = ? AND s.is_deleted = 0 "
                   + "ORDER BY rh.last_read_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("story_id"),
                        rs.getString("title"),
                        rs.getString("chapter_title"),
                        rs.getTimestamp("last_read_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
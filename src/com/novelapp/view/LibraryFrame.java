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
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("📚  Thư viện của tôi");
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

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Đang theo dõi", createFollowedPanel());
        tabbedPane.addTab("Yêu thích", createFavoritePanel());
        tabbedPane.addTab("Lịch sử đọc", createHistoryPanel());
        tabbedPane.addTab("Đã mở khóa", createUnlockedPanel());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createFollowedPanel() {
        return createTablePanel(
            new String[]{"ID", "Tên truyện", "Tác giả", "Trạng thái", "Ngày theo dõi"},
            "SELECT s.story_id, s.title, u.full_name, s.status, f.created_at "
          + "FROM follows f JOIN stories s ON f.story_id = s.story_id "
          + "JOIN users u ON s.author_id = u.user_id "
          + "WHERE f.user_id = ? AND s.is_deleted = 0 ORDER BY f.created_at DESC"
        );
    }

    private JPanel createFavoritePanel() {
        return createTablePanel(
            new String[]{"ID", "Tên truyện", "Tác giả", "Trạng thái", "Ngày thêm"},
            "SELECT s.story_id, s.title, u.full_name, s.status, fav.created_at "
          + "FROM favorites fav JOIN stories s ON fav.story_id = s.story_id "
          + "JOIN users u ON s.author_id = u.user_id "
          + "WHERE fav.user_id = ? AND s.is_deleted = 0 ORDER BY fav.created_at DESC"
        );
    }

    private JPanel createHistoryPanel() {
        return createTablePanel(
            new String[]{"ID", "Tên truyện", "Chương đang đọc", "Lần đọc cuối"},
            "SELECT s.story_id, s.title, c.title AS chapter_title, rh.last_read_at "
          + "FROM reading_history rh JOIN stories s ON rh.story_id = s.story_id "
          + "JOIN chapters c ON rh.chapter_id = c.chapter_id "
          + "WHERE rh.user_id = ? AND s.is_deleted = 0 ORDER BY rh.last_read_at DESC"
        );
    }

    private JPanel createUnlockedPanel() {
        // Đổi unlocked_at / created_at cho khớp DB của bạn
        return createTablePanel(
            new String[]{"ID", "Tên truyện", "Chương", "Ngày mở khóa"},
            "SELECT s.story_id, s.title, c.title AS chapter_title, ca.created_at "
          + "FROM chapter_access ca "
          + "JOIN chapters c ON ca.chapter_id = c.chapter_id "
          + "JOIN stories s ON c.story_id = s.story_id "
          + "WHERE ca.user_id = ? AND ca.access_type = 'PURCHASE' AND s.is_deleted = 0 "
          + "ORDER BY ca.created_at DESC"
        );
    }

    private JPanel createTablePanel(String[] columns, String sql) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[columns.length];
                    for (int i = 0; i < columns.length; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
}
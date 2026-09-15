package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AuthorChapterFrame extends JFrame {

    private final User currentUser;
    private final int storyId;
    private final String storyTitle;
    private DefaultTableModel model;
    private JTable table;

    public AuthorChapterFrame(int storyId, String storyTitle) {
        this.currentUser = SessionManager.getCurrentUser();
        this.storyId = storyId;
        this.storyTitle = storyTitle;

        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadChapters();
    }

    private void initComponents() {
        setTitle("Quản lý chương - " + storyTitle);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Chương của truyện: " + storyTitle);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btnAdd = new JButton("+ Thêm chương");
        btnAdd.setFocusPainted(false);
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            new CreateChapterFrame(storyId, storyTitle).setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Số chương", "Tiêu đề", "Miễn phí", "Giá Coin", "Trạng thái", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        JButton btnEdit = new JButton("Sửa chương");
        JButton btnDelete = new JButton("Xóa chương");
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnBack = new JButton("Quay lại");

        btnEdit.setFocusPainted(false);
        btnDelete.setFocusPainted(false);
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnBack.setFocusPainted(false);

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một chương!");
                return;
            }
            int chapterId = (int) table.getValueAt(row, 0);
            new EditChapterFrame(chapterId, storyId, storyTitle).setVisible(true);
            this.dispose();
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một chương!");
                return;
            }
            int chapterId = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 2);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn XÓA chương \"" + title + "\"?\n(Chỉ ẩn, không xóa vĩnh viễn)",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String sql = "UPDATE chapters SET is_deleted = 1, updated_at = GETDATE() WHERE chapter_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, chapterId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Đã xóa chương!");
                loadChapters();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        btnRefresh.addActionListener(e -> loadChapters());
        btnBack.addActionListener(e -> {
            new AuthorStoryFrame().setVisible(true);
            this.dispose();
        });

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnBack);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadChapters() {
        model.setRowCount(0);
        String sql = "SELECT chapter_id, chapter_number, title, is_free, price_coin, moderation_status, created_at "
                   + "FROM chapters WHERE story_id = ? AND is_deleted = 0 ORDER BY chapter_number ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, storyId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("chapter_id"),
                        rs.getDouble("chapter_number"),
                        rs.getString("title"),
                        rs.getBoolean("is_free") ? "Có" : "Không",
                        rs.getInt("price_coin"),
                        rs.getString("moderation_status"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
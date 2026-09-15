package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AuthorStoryFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AuthorStoryFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("AUTHOR") && !currentUser.hasRole("ADMIN"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền truy cập!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadStories();
    }

    private void initComponents() {
        setTitle("Quản lý truyện - Tác giả");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        // ===== Header =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý truyện của tôi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton btnAdd = new JButton("+ Thêm truyện mới");
        btnAdd.setFocusPainted(false);
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            new CreateStoryFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        // ===== Table =====
        String[] columns = {"ID", "Tên truyện", "Trạng thái", "Kiểm duyệt", "Lượt xem", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Ẩn cột ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);

        // ===== Nút dưới =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        JButton btnEdit = new JButton("Sửa truyện");
        JButton btnDelete = new JButton("Xóa truyện");
        JButton btnManageChapter = new JButton("Quản lý chương");
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnBack = new JButton("Quay lại");

        btnEdit.setFocusPainted(false);
        btnDelete.setFocusPainted(false);
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnManageChapter.setFocusPainted(false);
        btnRefresh.setFocusPainted(false);
        btnBack.setFocusPainted(false);

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
                return;
            }
            int storyId = (int) table.getValueAt(row, 0);
            new EditStoryFrame(storyId).setVisible(true);
            this.dispose();
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
                return;
            }
            int storyId = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn XÓA truyện \"" + title + "\"?\n(Chỉ ẩn, không xóa vĩnh viễn)",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String sql = "UPDATE stories SET is_deleted = 1, updated_at = GETDATE() WHERE story_id = ? AND author_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, storyId);
                ps.setInt(2, currentUser.getUserId());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Đã xóa truyện!");
                loadStories();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        btnManageChapter.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
                return;
            }
            int storyId = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 1);
            new AuthorChapterFrame(storyId, title).setVisible(true);
            this.dispose();
        });

        btnRefresh.addActionListener(e -> loadStories());
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnManageChapter);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnBack);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadStories() {
        model.setRowCount(0);
        String sql = "SELECT story_id, title, status, moderation_status, view_count, created_at "
                   + "FROM stories WHERE author_id = ? AND is_deleted = 0 ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentUser.getUserId());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("story_id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("moderation_status"),
                        rs.getLong("view_count"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
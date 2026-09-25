package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.StoryDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AuthorStoryFrame extends JFrame {

    private final User currentUser;
    private final StoryDAO storyDAO = new StoryDAO();
    private DefaultTableModel model;
    private JTable table;
    private JButton btnToggleHide;

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
        setTitle(currentUser.hasRole("ADMIN") ? "Quản lý toàn bộ truyện - Admin" : "Quản lý truyện - Tác giả");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 620);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel(currentUser.hasRole("ADMIN") ? "📚 Quản lý tất cả truyện" : "✍️ Quản lý truyện của tôi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnAdd = new JButton("+ Thêm truyện mới");
        btnAdd.setFocusPainted(false);
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            new CreateStoryFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnAdd, BorderLayout.EAST);

        // 1. Khai báo cột linh hoạt theo Role
        String[] columns;
        if (currentUser.hasRole("ADMIN")) {
            columns = new String[]{
                "ID", "Tên truyện", "Tác giả", "Trạng thái", "Kiểm duyệt",
                "Người duyệt", "Hiển thị", "Lượt xem", "Ngày tạo"
            };
        } else {
            columns = new String[]{
                "ID", "Tên truyện", "Tác giả", "Trạng thái", "Kiểm duyệt",
                "Hiển thị", "Lượt xem", "Ngày tạo"
            };
        }

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        // Ẩn cột ID (index 0)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Lắng nghe sự kiện chọn dòng để đổi tên nút Ẩn/Hiện
        table.getSelectionModel().addListSelectionListener(e -> updateToggleHideButtonLabel());

        // Panel nút bấm chức năng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnEdit = new JButton("Sửa truyện");
        JButton btnDelete = new JButton("Xóa truyện");
        btnToggleHide = new JButton("Ẩn/Hiện");
        JButton btnManageChapter = new JButton("Quản lý chương");
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnBack = new JButton("← Trang chủ");

        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setBorderPainted(false);

        btnToggleHide.setBackground(new Color(108, 117, 125));
        btnToggleHide.setForeground(Color.WHITE);
        btnToggleHide.setBorderPainted(false);

        btnEdit.setFocusPainted(false);
        btnDelete.setFocusPainted(false);
        btnToggleHide.setFocusPainted(false);
        btnManageChapter.setFocusPainted(false);
        btnRefresh.setFocusPainted(false);
        btnBack.setFocusPainted(false);

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
                return;
            }
            new EditStoryFrame((int) table.getValueAt(row, 0)).setVisible(true);
            this.dispose();
        });

        // Nút Ẩn/Hiện truyện
        btnToggleHide.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện để Ẩn/Hiện!");
                return;
            }

            int storyId = (int) table.getValueAt(row, 0);
            String displayStatus = (String) table.getValueAt(row, getDisplayColumnIndex());
            boolean isCurrentlyHidden = "Đã ẩn".equals(displayStatus);

            boolean targetHiddenState = !isCurrentlyHidden;
            String actionName = targetHiddenState ? "ẩn" : "hiện";

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn " + actionName + " truyện này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean success = storyDAO.setStoryHidden(storyId, targetHiddenState);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã " + actionName + " truyện thành công!");
                loadStories();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái Ẩn/Hiện!");
            }
        });

        // Nút Xóa truyện (Soft delete)
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
                return;
            }
            int storyId = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa truyện \"" + title + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            String deleteSql;
            if (currentUser.hasRole("ADMIN")) {
                deleteSql = "UPDATE stories SET is_deleted = 1, updated_at = GETDATE() WHERE story_id = ?";
            } else {
                deleteSql = "UPDATE stories SET is_deleted = 1, updated_at = GETDATE() WHERE story_id = ? AND author_id = ?";
            }

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, storyId);
                if (!currentUser.hasRole("ADMIN")) {
                    ps.setInt(2, currentUser.getUserId());
                }
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Đã xóa truyện!");
                loadStories();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa truyện: " + ex.getMessage());
            }
        });

        btnManageChapter.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
                return;
            }
            new AuthorChapterFrame((int) table.getValueAt(row, 0), (String) table.getValueAt(row, 1)).setVisible(true);
            this.dispose();
        });

        btnRefresh.addActionListener(e -> loadStories());
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnToggleHide);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnManageChapter);
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnBack);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    // Helper xác định vị trí cột "Hiển thị" dựa vào Role
    private int getDisplayColumnIndex() {
        return currentUser.hasRole("ADMIN") ? 6 : 5;
    }

    // 2. Nạp danh sách truyện theo Role
    private void loadStories() {
        model.setRowCount(0);
        boolean isAdmin = currentUser.hasRole("ADMIN");
        String sql;

        if (isAdmin) {
            sql = "SELECT s.story_id, s.title, s.status, s.moderation_status, "
                + "ISNULL(s.is_hidden, 0) AS is_hidden, s.view_count, s.created_at, "
                + "ISNULL(s.author_display_name, ISNULL(u.full_name, u.username)) AS author_name, "
                + "ISNULL(app.full_name, app.username) AS approver_name "
                + "FROM stories s "
                + "LEFT JOIN users u ON s.author_id = u.user_id "
                + "LEFT JOIN users app ON s.approved_by = app.user_id "
                + "WHERE ISNULL(s.is_deleted, 0) = 0 "
                + "ORDER BY s.created_at DESC";
        } else {
            sql = "SELECT s.story_id, s.title, s.status, s.moderation_status, "
                + "ISNULL(s.is_hidden, 0) AS is_hidden, s.view_count, s.created_at, "
                + "ISNULL(s.author_display_name, ISNULL(u.full_name, u.username)) AS author_name "
                + "FROM stories s "
                + "LEFT JOIN users u ON s.author_id = u.user_id "
                + "WHERE s.author_id = ? AND ISNULL(s.is_deleted, 0) = 0 "
                + "ORDER BY s.created_at DESC";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!isAdmin) {
                ps.setInt(1, currentUser.getUserId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean isHidden = rs.getBoolean("is_hidden");
                    if (isAdmin) {
                        model.addRow(new Object[]{
                            rs.getInt("story_id"),
                            rs.getString("title"),
                            rs.getString("author_name"),
                            rs.getString("status"),
                            rs.getString("moderation_status"),
                            rs.getString("approver_name"),
                            isHidden ? "Đã ẩn" : "Đang hiện",
                            rs.getLong("view_count"),
                            rs.getTimestamp("created_at")
                        });
                    } else {
                        model.addRow(new Object[]{
                            rs.getInt("story_id"),
                            rs.getString("title"),
                            rs.getString("author_name"),
                            rs.getString("status"),
                            rs.getString("moderation_status"),
                            isHidden ? "Đã ẩn" : "Đang hiện",
                            rs.getLong("view_count"),
                            rs.getTimestamp("created_at")
                        });
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách truyện: " + e.getMessage());
        }
        updateToggleHideButtonLabel();
    }

    private void updateToggleHideButtonLabel() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String displayStatus = (String) table.getValueAt(row, getDisplayColumnIndex());
            if ("Đã ẩn".equals(displayStatus)) {
                btnToggleHide.setText("Hiện truyện");
            } else {
                btnToggleHide.setText("Ẩn truyện");
            }
        } else {
            btnToggleHide.setText("Ẩn/Hiện");
        }
    }
}
package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminCommentModerationFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminCommentModerationFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("STAFF"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadComments();
    }

    private void initComponents() {
        setTitle("Kiểm duyệt Bình luận - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("💬  Kiểm duyệt Bình luận");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        // Table
        String[] columns = {"ID", "Người viết", "Nội dung", "Truyện ID", "Trạng thái", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(350);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnHide = createBtn("Ẩn bình luận", new Color(220, 53, 69));
        JButton btnShow = createBtn("Hiện lại", new Color(0, 153, 76));
        JButton btnDelete = createBtn("Xóa vĩnh viễn", new Color(108, 117, 125));
        JButton btnRefresh = createBtn("Làm mới", null);
        JButton btnBack = createBtn("← Dashboard", null);

        btnHide.addActionListener(e -> updateComment(true, false));
        btnShow.addActionListener(e -> updateComment(false, false));
        btnDelete.addActionListener(e -> updateComment(true, true));
        btnRefresh.addActionListener(e -> loadComments());
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        bottom.add(btnHide);
        bottom.add(btnShow);
        bottom.add(btnDelete);
        bottom.add(btnRefresh);
        bottom.add(btnBack);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (bg != null) {
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
        }
        return btn;
    }

    private void loadComments() {
        model.setRowCount(0);
        String sql = "SELECT c.comment_id, u.username, c.content, c.story_id, "
                   + "CASE WHEN c.is_hidden = 1 THEN N'Đã ẩn' WHEN c.is_deleted = 1 THEN N'Đã xóa' ELSE N'Hiển thị' END AS status, "
                   + "c.created_at "
                   + "FROM comments c "
                   + "JOIN users u ON c.user_id = u.user_id "
                   + "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("comment_id"),
                    rs.getString("username"),
                    rs.getString("content"),
                    rs.getObject("story_id"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateComment(boolean hide, boolean delete) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bình luận!");
            return;
        }
        int commentId = (int) table.getValueAt(row, 0);

        String msg = delete ? "Xóa vĩnh viễn bình luận này?" : (hide ? "Ẩn bình luận này?" : "Hiện lại bình luận này?");
        int confirm = JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql;
        if (delete) {
            sql = "UPDATE comments SET is_deleted = 1, is_hidden = 1 WHERE comment_id = ?";
        } else if (hide) {
            sql = "UPDATE comments SET is_hidden = 1 WHERE comment_id = ?";
        } else {
            sql = "UPDATE comments SET is_hidden = 0, is_deleted = 0 WHERE comment_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadComments();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
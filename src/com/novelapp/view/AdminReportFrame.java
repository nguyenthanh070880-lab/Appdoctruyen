package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminReportFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminReportFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("STAFF"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadReports();
    }

    private void initComponents() {
        setTitle("Xử lý Báo cáo - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🚩  Xử lý Báo cáo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.WEST);

        // Table
        String[] columns = {"ID", "Loại", "Đối tượng ID", "Người báo cáo", "Lý do", "Mô tả", "Trạng thái", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnResolve = createBtn("Đã xử lý", new Color(0, 153, 76));
        JButton btnReject = createBtn("Từ chối báo cáo", new Color(108, 117, 125));
        JButton btnHide = createBtn("Ẩn nội dung", new Color(220, 53, 69));
        JButton btnRefresh = createBtn("Làm mới", null);
        JButton btnBack = createBtn("← Dashboard", null);

        btnResolve.addActionListener(e -> updateReportStatus("RESOLVED"));
        btnReject.addActionListener(e -> updateReportStatus("REJECTED"));
        btnHide.addActionListener(e -> hideReportedContent());
        btnRefresh.addActionListener(e -> loadReports());
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        bottom.add(btnResolve);
        bottom.add(btnReject);
        bottom.add(btnHide);
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

    private void loadReports() {
        model.setRowCount(0);
        String sql = "SELECT r.report_id, r.target_type, r.target_id, u.username, r.reason, "
                   + "r.description, r.status, r.created_at "
                   + "FROM reports r "
                   + "JOIN users u ON r.reporter_id = u.user_id "
                   + "ORDER BY CASE r.status WHEN 'PENDING' THEN 0 ELSE 1 END, r.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("report_id"),
                    rs.getString("target_type"),
                    rs.getInt("target_id"),
                    rs.getString("username"),
                    rs.getString("reason"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateReportStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một báo cáo!");
            return;
        }
        int reportId = (int) table.getValueAt(row, 0);

        String sql = "UPDATE reports SET status = ?, reviewed_by = ?, reviewed_at = GETDATE() WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, currentUser.getUserId());
            ps.setInt(3, reportId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadReports();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void hideReportedContent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một báo cáo!");
            return;
        }

        String targetType = (String) table.getValueAt(row, 1);
        int targetId = (int) table.getValueAt(row, 2);
        int reportId = (int) table.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ẩn nội dung này (" + targetType + " #" + targetId + ")?",
                "Xác nhận ẩn", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = null;
        switch (targetType) {
            case "STORY":
                sql = "UPDATE stories SET is_deleted = 1, updated_at = GETDATE() WHERE story_id = ?";
                break;
            case "CHAPTER":
                sql = "UPDATE chapters SET is_deleted = 1, updated_at = GETDATE() WHERE chapter_id = ?";
                break;
            case "COMMENT":
                sql = "UPDATE comments SET is_hidden = 1, is_deleted = 1 WHERE comment_id = ?";
                break;
            default:
                JOptionPane.showMessageDialog(this, "Loại đối tượng không hỗ trợ!");
                return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, targetId);
            ps.executeUpdate();

            // Đánh dấu báo cáo đã xử lý
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE reports SET status = 'RESOLVED', reviewed_by = ?, reviewed_at = GETDATE() WHERE report_id = ?")) {
                ps2.setInt(1, currentUser.getUserId());
                ps2.setInt(2, reportId);
                ps2.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Đã ẩn nội dung và đánh dấu báo cáo đã xử lý!");
            loadReports();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
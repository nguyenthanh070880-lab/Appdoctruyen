package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminStoryModerationFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminStoryModerationFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("STAFF"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadPendingStories();
    }

    private void initComponents() {
        setTitle("Duyệt Truyện - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Danh sách truyện chờ duyệt");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        String[] columns = {"ID", "Tên truyện", "Tác giả", "Trạng thái", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnApprove = new JButton("Duyệt");
        btnApprove.setBackground(new Color(0, 153, 76));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setFocusPainted(false);
        btnApprove.addActionListener(e -> updateStatus("APPROVED"));

        JButton btnReject = new JButton("Từ chối");
        btnReject.setBackground(new Color(220, 53, 69));
        btnReject.setForeground(Color.WHITE);
        btnReject.setFocusPainted(false);
        btnReject.addActionListener(e -> updateStatus("REJECTED"));

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadPendingStories());

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadPendingStories() {
        model.setRowCount(0);
        String sql = "SELECT s.story_id, s.title, u.full_name, s.status, s.created_at "
                   + "FROM stories s "
                   + "JOIN users u ON s.author_id = u.user_id "
                   + "WHERE s.moderation_status = 'PENDING' AND s.is_deleted = 0 "
                   + "ORDER BY s.created_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("story_id"),
                    rs.getString("title"),
                    rs.getString("full_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một truyện!");
            return;
        }

        int storyId = (int) table.getValueAt(row, 0);
        String title = (String) table.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + (status.equals("APPROVED") ? "DUYỆT" : "TỪ CHỐI") 
                + " truyện \"" + title + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE stories SET moderation_status = ?, updated_at = GETDATE() WHERE story_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, storyId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadPendingStories();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
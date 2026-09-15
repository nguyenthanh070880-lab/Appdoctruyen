package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminChapterModerationFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminChapterModerationFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("STAFF"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadPendingChapters();
    }

    private void initComponents() {
        setTitle("Duyệt Chương - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Danh sách chương chờ duyệt");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        String[] columns = {"ID", "Truyện", "Số chương", "Tiêu đề", "Miễn phí", "Giá", "Ngày tạo"};
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
        btnRefresh.addActionListener(e -> loadPendingChapters());

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

    private void loadPendingChapters() {
        model.setRowCount(0);
        String sql = "SELECT c.chapter_id, s.title AS story_title, c.chapter_number, c.title, "
                   + "c.is_free, c.price_coin, c.created_at "
                   + "FROM chapters c "
                   + "JOIN stories s ON c.story_id = s.story_id "
                   + "WHERE c.moderation_status = 'PENDING' AND c.is_deleted = 0 "
                   + "ORDER BY c.created_at ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("chapter_id"),
                    rs.getString("story_title"),
                    rs.getDouble("chapter_number"),
                    rs.getString("title"),
                    rs.getBoolean("is_free") ? "Có" : "Không",
                    rs.getInt("price_coin"),
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chương!");
            return;
        }

        int chapterId = (int) table.getValueAt(row, 0);
        String title = (String) table.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + (status.equals("APPROVED") ? "DUYỆT" : "TỪ CHỐI") 
                + " chương \"" + title + "\"?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE chapters SET moderation_status = ?, updated_at = GETDATE() WHERE chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, chapterId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadPendingChapters();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
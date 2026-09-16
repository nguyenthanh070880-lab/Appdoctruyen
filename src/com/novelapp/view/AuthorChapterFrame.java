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
        setSize(1000, 580);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lbl = new JLabel("📖  Chương của: " + storyTitle);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);

        JButton btnAdd = new JButton("+ Thêm chương");
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            new CreateChapterFrame(storyId, storyTitle).setVisible(true);
            this.dispose();
        });

        header.add(lbl, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);

        String[] columns = {"ID", "Số chương", "Tiêu đề", "Miễn phí", "Giá Coin", "Trạng thái", "Ngày tạo"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnEdit = new JButton("Sửa chương");
        JButton btnDelete = new JButton("Xóa chương");
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnBack = new JButton("← Quay lại");

        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setBorderPainted(false);

        btnEdit.setFocusPainted(false);
        btnDelete.setFocusPainted(false);
        btnRefresh.setFocusPainted(false);
        btnBack.setFocusPainted(false);

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn chương!");
                return;
            }
            new EditChapterFrame((int) table.getValueAt(row, 0), storyId, storyTitle).setVisible(true);
            this.dispose();
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn chương!");
                return;
            }
            int chapterId = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa chương \"" + title + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE chapters SET is_deleted = 1, updated_at = GETDATE() WHERE chapter_id = ?")) {
                ps.setInt(1, chapterId);
                ps.executeUpdate();
                loadChapters();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnRefresh.addActionListener(e -> loadChapters());
        btnBack.addActionListener(e -> {
            new AuthorStoryFrame().setVisible(true);
            this.dispose();
        });

        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnRefresh);
        bottom.add(btnBack);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadChapters() {
        model.setRowCount(0);
        String sql = "SELECT chapter_id, chapter_number, title, is_free, price_coin, moderation_status, created_at "
                   + "FROM chapters WHERE story_id = ? AND is_deleted = 0 ORDER BY chapter_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt(1), rs.getDouble(2), rs.getString(3),
                        rs.getBoolean(4) ? "Có" : "Không",
                        rs.getInt(5), rs.getString(6), rs.getTimestamp(7)
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
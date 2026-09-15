package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminGenreFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminGenreFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadGenres();
    }

    private void initComponents() {
        setTitle("Quản lý Thể loại - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Quản lý Thể loại / Hashtag");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        String[] columns = {"ID", "Tên thể loại", "Mô tả", "Trạng thái"};
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

        JButton btnAdd = new JButton("+ Thêm thể loại");
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addGenre());

        JButton btnEdit = new JButton("Sửa");
        btnEdit.setFocusPainted(false);
        btnEdit.addActionListener(e -> editGenre());

        JButton btnToggle = new JButton("Ẩn / Hiện");
        btnToggle.setFocusPainted(false);
        btnToggle.addActionListener(e -> toggleActive());

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadGenres());

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnToggle);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadGenres() {
        model.setRowCount(0);
        String sql = "SELECT genre_id, genre_name, description, is_active FROM genres ORDER BY genre_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("genre_id"),
                    rs.getString("genre_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_active") ? "Hiện" : "Ẩn"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addGenre() {
        JTextField txtName = new JTextField();
        JTextField txtDesc = new JTextField();
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Tên thể loại:"));
        panel.add(txtName);
        panel.add(new JLabel("Mô tả:"));
        panel.add(txtDesc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm thể loại mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên thể loại không được để trống!");
            return;
        }

        String sql = "INSERT INTO genres (genre_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, txtDesc.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadGenres();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void editGenre() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thể loại!");
            return;
        }
        int genreId = (int) table.getValueAt(row, 0);
        String oldName = (String) table.getValueAt(row, 1);
        String oldDesc = (String) table.getValueAt(row, 2);

        JTextField txtName = new JTextField(oldName);
        JTextField txtDesc = new JTextField(oldDesc != null ? oldDesc : "");
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Tên thể loại:"));
        panel.add(txtName);
        panel.add(new JLabel("Mô tả:"));
        panel.add(txtDesc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa thể loại",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String sql = "UPDATE genres SET genre_name = ?, description = ?, updated_at = GETDATE() WHERE genre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtDesc.getText().trim());
            ps.setInt(3, genreId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadGenres();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void toggleActive() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thể loại!");
            return;
        }
        int genreId = (int) table.getValueAt(row, 0);
        String status = (String) table.getValueAt(row, 3);
        boolean newActive = status.equals("Ẩn");

        String sql = "UPDATE genres SET is_active = ?, updated_at = GETDATE() WHERE genre_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, newActive);
            ps.setInt(2, genreId);
            ps.executeUpdate();
            loadGenres();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
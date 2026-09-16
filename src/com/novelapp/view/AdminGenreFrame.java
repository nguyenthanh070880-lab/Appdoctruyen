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
            JOptionPane.showMessageDialog(null, "Bạn không có quyền!");
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
        setSize(750, 520);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        JLabel lbl = new JLabel("🏷️  Quản lý Thể loại");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(Color.WHITE);
        header.add(lbl, BorderLayout.WEST);

        String[] columns = {"ID", "Tên thể loại", "Mô tả", "Trạng thái"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("+ Thêm");
        btnAdd.setBackground(new Color(0, 153, 76));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setBorderPainted(false);
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

        JButton btnBack = new JButton("← Dashboard");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnToggle);
        bottom.add(btnRefresh);
        bottom.add(btnBack);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadGenres() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT genre_id, genre_name, description, is_active FROM genres ORDER BY genre_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getBoolean(4) ? "Hiện" : "Ẩn"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addGenre() {
        JTextField txtName = new JTextField();
        JTextField txtDesc = new JTextField();
        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.add(new JLabel("Tên:")); p.add(txtName);
        p.add(new JLabel("Mô tả:")); p.add(txtDesc);
        if (JOptionPane.showConfirmDialog(this, p, "Thêm thể loại", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        if (txtName.getText().trim().isEmpty()) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO genres (genre_name, description) VALUES (?, ?)")) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtDesc.getText().trim());
            ps.executeUpdate();
            loadGenres();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editGenre() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        JTextField txtName = new JTextField((String) table.getValueAt(row, 1));
        JTextField txtDesc = new JTextField((String) table.getValueAt(row, 2));
        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.add(new JLabel("Tên:")); p.add(txtName);
        p.add(new JLabel("Mô tả:")); p.add(txtDesc);
        if (JOptionPane.showConfirmDialog(this, p, "Sửa thể loại", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE genres SET genre_name=?, description=?, updated_at=GETDATE() WHERE genre_id=?")) {
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtDesc.getText().trim());
            ps.setInt(3, id);
            ps.executeUpdate();
            loadGenres();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toggleActive() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) table.getValueAt(row, 0);
        boolean newActive = "Ẩn".equals(table.getValueAt(row, 3));
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE genres SET is_active=?, updated_at=GETDATE() WHERE genre_id=?")) {
            ps.setBoolean(1, newActive);
            ps.setInt(2, id);
            ps.executeUpdate();
            loadGenres();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminPromotionFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;
    private JTable table;

    public AdminPromotionFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền Admin!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadPackages();
    }

    private void initComponents() {
        setTitle("Quản lý Khuyến mãi (Gói Coin) - Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🎁  Quản lý Khuyến mãi - Gói Coin");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Dashboard");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Tên gói", "Coin", "Bonus", "Tổng nhận", "Giá (VNĐ)", "Trạng thái"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(Color.WHITE);

        JButton btnAdd = createBtn("+ Thêm gói", new Color(0, 153, 76));
        JButton btnEdit = createBtn("Sửa", null);
        JButton btnToggle = createBtn("Bật / Tắt", new Color(255, 193, 7));
        JButton btnRefresh = createBtn("Làm mới", null);

        btnAdd.addActionListener(e -> addPackage());
        btnEdit.addActionListener(e -> editPackage());
        btnToggle.addActionListener(e -> togglePackage());
        btnRefresh.addActionListener(e -> loadPackages());

        bottom.add(btnAdd);
        bottom.add(btnEdit);
        bottom.add(btnToggle);
        bottom.add(btnRefresh);

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
            btn.setForeground(bg.equals(new Color(255, 193, 7)) ? Color.BLACK : Color.WHITE);
            btn.setBorderPainted(false);
        }
        return btn;
    }

    private void loadPackages() {
        model.setRowCount(0);
        String sql = "SELECT package_id, package_name, coin_amount, bonus_coin, "
                   + "(coin_amount + bonus_coin) AS total, price_vnd, is_active "
                   + "FROM coin_packages ORDER BY price_vnd";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("package_id"),
                    rs.getString("package_name"),
                    rs.getInt("coin_amount"),
                    rs.getInt("bonus_coin"),
                    rs.getInt("total"),
                    String.format("%,d", rs.getLong("price_vnd")),
                    rs.getBoolean("is_active") ? "Đang bán" : "Đã tắt"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addPackage() {
        JTextField txtName = new JTextField();
        JTextField txtCoin = new JTextField();
        JTextField txtBonus = new JTextField("0");
        JTextField txtPrice = new JTextField();

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Tên gói:")); p.add(txtName);
        p.add(new JLabel("Số Coin:")); p.add(txtCoin);
        p.add(new JLabel("Bonus Coin:")); p.add(txtBonus);
        p.add(new JLabel("Giá (VNĐ):")); p.add(txtPrice);

        if (JOptionPane.showConfirmDialog(this, p, "Thêm gói khuyến mãi",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            String name = txtName.getText().trim();
            int coin = Integer.parseInt(txtCoin.getText().trim());
            int bonus = Integer.parseInt(txtBonus.getText().trim());
            long price = Long.parseLong(txtPrice.getText().trim());

            if (name.isEmpty() || coin <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!");
                return;
            }

            String sql = "INSERT INTO coin_packages (package_name, coin_amount, bonus_coin, price_vnd, is_active) "
                       + "VALUES (?, ?, ?, ?, 1)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt(2, coin);
                ps.setInt(3, bonus);
                ps.setLong(4, price);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Thêm gói thành công!");
            loadPackages();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số Coin / Giá phải là số!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void editPackage() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một gói!");
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        JTextField txtName = new JTextField((String) table.getValueAt(row, 1));
        JTextField txtCoin = new JTextField(table.getValueAt(row, 2).toString());
        JTextField txtBonus = new JTextField(table.getValueAt(row, 3).toString());
        JTextField txtPrice = new JTextField(table.getValueAt(row, 5).toString().replace(",", ""));

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.add(new JLabel("Tên gói:")); p.add(txtName);
        p.add(new JLabel("Số Coin:")); p.add(txtCoin);
        p.add(new JLabel("Bonus Coin:")); p.add(txtBonus);
        p.add(new JLabel("Giá (VNĐ):")); p.add(txtPrice);

        if (JOptionPane.showConfirmDialog(this, p, "Sửa gói",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {
            String sql = "UPDATE coin_packages SET package_name=?, coin_amount=?, bonus_coin=?, price_vnd=? "
                       + "WHERE package_id=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, txtName.getText().trim());
                ps.setInt(2, Integer.parseInt(txtCoin.getText().trim()));
                ps.setInt(3, Integer.parseInt(txtBonus.getText().trim()));
                ps.setLong(4, Long.parseLong(txtPrice.getText().trim()));
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadPackages();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void togglePackage() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một gói!");
            return;
        }
        int id = (int) table.getValueAt(row, 0);
        boolean currentlyActive = "Đang bán".equals(table.getValueAt(row, 6));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE coin_packages SET is_active = ? WHERE package_id = ?")) {
            ps.setBoolean(1, !currentlyActive);
            ps.setInt(2, id);
            ps.executeUpdate();
            loadPackages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
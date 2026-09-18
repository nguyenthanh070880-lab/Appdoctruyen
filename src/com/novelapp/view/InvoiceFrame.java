package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InvoiceFrame extends JFrame {

    private final User currentUser;
    private DefaultTableModel model;

    public InvoiceFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadInvoices();
    }

    private void initComponents() {
        setTitle("Hóa đơn nạp Coin - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🧾  Hóa đơn của tôi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Ví Coin");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new WalletFrame().setVisible(true);
            this.dispose();
        });

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        // Table
        String[] columns = {"Mã đơn", "Gói", "Số Coin", "Số tiền (VNĐ)", "Phương thức", "Trạng thái", "Thời gian"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);

        // Xem chi tiết khi double-click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showInvoiceDetail(row);
                }
            }
        });

        JLabel lblHint = new JLabel("  (Double-click để xem chi tiết hóa đơn)");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHint.setForeground(Color.GRAY);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(15, 20, 10, 20));
        center.add(lblHint, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadInvoices() {
        model.setRowCount(0);
        String sql = "SELECT po.order_code, ISNULL(cp.package_name, N'Không rõ') AS package_name, "
                   + "po.coin_amount, po.amount_vnd, po.payment_method, po.status, po.created_at "
                   + "FROM payment_orders po "
                   + "LEFT JOIN coin_packages cp ON po.package_id = cp.package_id "
                   + "WHERE po.user_id = ? "
                   + "ORDER BY po.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("order_code"),
                        rs.getString("package_name"),
                        rs.getInt("coin_amount"),
                        String.format("%,d", rs.getLong("amount_vnd")),
                        rs.getString("payment_method"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showInvoiceDetail(int row) {
        String orderCode = (String) model.getValueAt(row, 0);
        String pkg = (String) model.getValueAt(row, 1);
        Object coin = model.getValueAt(row, 2);
        Object money = model.getValueAt(row, 3);
        String method = (String) model.getValueAt(row, 4);
        String status = (String) model.getValueAt(row, 5);
        Object time = model.getValueAt(row, 6);

        String detail = String.format(
            "<html><body style='width:320px; font-family:Segoe UI; padding:8px;'>"
          + "<h2 style='color:#0066CC;'>HÓA ĐƠN NẠP COIN</h2>"
          + "<hr>"
          + "<b>Mã đơn:</b> %s<br><br>"
          + "<b>Khách hàng:</b> %s<br>"
          + "<b>Username:</b> %s<br><br>"
          + "<b>Gói:</b> %s<br>"
          + "<b>Số Coin nhận:</b> %s<br>"
          + "<b>Số tiền:</b> %s VNĐ<br>"
          + "<b>Phương thức:</b> %s<br>"
          + "<b>Trạng thái:</b> %s<br>"
          + "<b>Thời gian:</b> %s<br>"
          + "<hr>"
          + "<i>Cảm ơn bạn đã sử dụng NovelApp!</i>"
          + "</body></html>",
            orderCode,
            currentUser.getFullName(),
            currentUser.getUsername(),
            pkg, coin, money, method, status, time
        );

        JOptionPane.showMessageDialog(this, new JLabel(detail),
                "Chi tiết hóa đơn", JOptionPane.INFORMATION_MESSAGE);
    }
}
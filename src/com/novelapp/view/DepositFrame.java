package com.novelapp.view;

import com.novelapp.dao.CoinPackageDAO;
import com.novelapp.model.CoinPackage;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import com.novelapp.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.List;

public class DepositFrame extends JFrame {

    private final User currentUser;
    private final CoinPackageDAO packageDAO = new CoinPackageDAO();

    public DepositFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Nạp Coin - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("💳  Chọn gói Coin");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Quay lại Ví");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new WalletFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnBack, BorderLayout.EAST);

        // Packages
        JPanel packagesPanel = new JPanel();
        packagesPanel.setLayout(new BoxLayout(packagesPanel, BoxLayout.Y_AXIS));
        packagesPanel.setBackground(Color.WHITE);
        packagesPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        List<CoinPackage> packages = packageDAO.getActivePackages();
        if (packages.isEmpty()) {
            packagesPanel.add(new JLabel("Chưa có gói nào."));
        } else {
            for (CoinPackage pkg : packages) {
                packagesPanel.add(createPackageCard(pkg));
                packagesPanel.add(Box.createVerticalStrut(12));
            }
        }

        JScrollPane scrollPane = new JScrollPane(packagesPanel);
        scrollPane.setBorder(null);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createPackageCard(CoinPackage pkg) {
        JPanel card = new JPanel(new BorderLayout(15, 8));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(16, 20, 16, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblName = new JLabel(pkg.getPackageName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String bonus = pkg.getBonusCoin() > 0 ? " (+" + pkg.getBonusCoin() + " thưởng)" : "";
        JLabel lblCoin = new JLabel(pkg.getCoinAmount() + " Coin" + bonus);
        lblCoin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCoin.setForeground(new Color(0, 102, 204));

        JLabel lblPrice = new JLabel(String.format("%,d VNĐ", pkg.getPriceVnd()));
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPrice.setForeground(Color.GRAY);

        info.add(lblName);
        info.add(Box.createVerticalStrut(4));
        info.add(lblCoin);
        info.add(Box.createVerticalStrut(4));
        info.add(lblPrice);

        JButton btnBuy = new JButton("Mua ngay");
        btnBuy.setPreferredSize(new Dimension(120, 40));
        btnBuy.setBackground(new Color(0, 153, 76));
        btnBuy.setForeground(Color.WHITE);
        btnBuy.setFocusPainted(false);
        btnBuy.setBorderPainted(false);
        btnBuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBuy.addActionListener(e -> processPurchase(pkg));

        card.add(info, BorderLayout.CENTER);
        card.add(btnBuy, BorderLayout.EAST);
        return card;
    }

    private void processPurchase(CoinPackage pkg) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận mua gói \"" + pkg.getPackageName() + "\"?\n"
                + "Giá: " + String.format("%,d", pkg.getPriceVnd()) + " VNĐ\n"
                + "Nhận: " + pkg.getTotalCoin() + " Coin\n\n"
                + "(Thanh toán mô phỏng)",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        if (mockPayment(pkg)) {
            JOptionPane.showMessageDialog(this, "Thanh toán thành công!\nBạn đã nhận " + pkg.getTotalCoin() + " Coin.");
            new WalletFrame().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Thanh toán thất bại!");
        }
    }

    private boolean mockPayment(CoinPackage pkg) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String orderCode = "ORD" + System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO payment_orders (user_id, package_id, order_code, amount_vnd, coin_amount, status, payment_method) VALUES (?, ?, ?, ?, ?, 'SUCCESS', 'MOCK')")) {
                ps.setInt(1, currentUser.getUserId());
                ps.setInt(2, pkg.getPackageId());
                ps.setString(3, orderCode);
                ps.setLong(4, pkg.getPriceVnd());
                ps.setInt(5, pkg.getTotalCoin());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE wallets SET balance = balance + ?, updated_at = GETDATE() WHERE user_id = ?")) {
                ps.setLong(1, pkg.getTotalCoin());
                ps.setInt(2, currentUser.getUserId());
                ps.executeUpdate();
            }

            long newBalance = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM wallets WHERE user_id = ?")) {
                ps.setInt(1, currentUser.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) newBalance = rs.getLong("balance");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) VALUES (?, ?, ?, 'DEPOSIT', ?)")) {
                ps.setInt(1, currentUser.getUserId());
                ps.setLong(2, pkg.getTotalCoin());
                ps.setLong(3, newBalance);
                ps.setString(4, "Nạp gói " + pkg.getPackageName());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {}
        }
    }
}
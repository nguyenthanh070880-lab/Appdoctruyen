package com.novelapp.view;

import com.novelapp.dao.CoinPackageDAO;
import com.novelapp.dao.WalletDAO;
import com.novelapp.model.CoinPackage;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import com.novelapp.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.UUID;

public class DepositFrame extends JFrame {

    private final User currentUser;
    private final CoinPackageDAO packageDAO = new CoinPackageDAO();
    private final WalletDAO walletDAO = new WalletDAO();

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
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Chọn gói Coin");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JPanel packagesPanel = new JPanel();
        packagesPanel.setLayout(new BoxLayout(packagesPanel, BoxLayout.Y_AXIS));
        packagesPanel.setOpaque(false);

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

        JButton btnBack = new JButton("Quay lại Ví");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new WalletFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createPackageCard(CoinPackage pkg) {
        JPanel card = new JPanel(new BorderLayout(15, 8));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblName = new JLabel(pkg.getPackageName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String bonusText = pkg.getBonusCoin() > 0 ? " (+" + pkg.getBonusCoin() + " thưởng)" : "";
        JLabel lblCoin = new JLabel(pkg.getCoinAmount() + " Coin" + bonusText);
        lblCoin.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblCoin.setForeground(new Color(0, 102, 204));

        JLabel lblPrice = new JLabel(String.format("%,d VNĐ", pkg.getPriceVnd()));
        lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPrice.setForeground(Color.GRAY);

        info.add(lblName);
        info.add(Box.createVerticalStrut(3));
        info.add(lblCoin);
        info.add(Box.createVerticalStrut(3));
        info.add(lblPrice);

        JButton btnBuy = new JButton("Mua ngay");
        btnBuy.setFocusPainted(false);
        btnBuy.setBackground(new Color(0, 153, 76));
        btnBuy.setForeground(Color.WHITE);
        btnBuy.setPreferredSize(new Dimension(110, 36));
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
                + "(Đây là thanh toán mô phỏng - sẽ thành công ngay)",
                "Xác nhận thanh toán",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // ===== Thanh toán mô phỏng =====
        boolean success = mockPayment(pkg);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Thanh toán thành công!\nBạn đã nhận " + pkg.getTotalCoin() + " Coin.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            new WalletFrame().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Thanh toán thất bại, vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Thanh toán mô phỏng + cộng coin + ghi lịch sử
    private boolean mockPayment(CoinPackage pkg) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String orderCode = "ORD" + System.currentTimeMillis();

            // 1. Tạo payment_order
            String orderSql = "INSERT INTO payment_orders (user_id, package_id, order_code, amount_vnd, coin_amount, status, payment_method) "
                            + "VALUES (?, ?, ?, ?, ?, 'SUCCESS', 'MOCK')";
            try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                ps.setInt(1, currentUser.getUserId());
                ps.setInt(2, pkg.getPackageId());
                ps.setString(3, orderCode);
                ps.setLong(4, pkg.getPriceVnd());
                ps.setInt(5, pkg.getTotalCoin());
                ps.executeUpdate();
            }

            // 2. Cộng coin vào ví
            String updateWallet = "UPDATE wallets SET balance = balance + ?, updated_at = GETDATE() WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateWallet)) {
                ps.setLong(1, pkg.getTotalCoin());
                ps.setInt(2, currentUser.getUserId());
                ps.executeUpdate();
            }

            // 3. Lấy số dư mới
            long newBalance = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM wallets WHERE user_id = ?")) {
                ps.setInt(1, currentUser.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) newBalance = rs.getLong("balance");
                }
            }

            // 4. Ghi coin_transaction
            String transSql = "INSERT INTO coin_transactions (user_id, amount, balance_after, type, description) "
                            + "VALUES (?, ?, ?, 'DEPOSIT', ?)";
            try (PreparedStatement ps = conn.prepareStatement(transSql)) {
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
package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardFrame extends JFrame {

    private final User currentUser;

    public AdminDashboardFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("ADMIN") && !currentUser.hasRole("STAFF"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền truy cập!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Admin Dashboard - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("⚙️  Trang quản trị hệ thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnBack, BorderLayout.EAST);

        // 2. Center Content (Stats Panel + Menu Cards Scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // -- Truy vấn các chỉ số thống kê tổng quan --
        long totalUsers = q("SELECT COUNT(*) FROM users WHERE ISNULL(is_deleted,0)=0");
        long newUsers = q("SELECT COUNT(*) FROM users WHERE ISNULL(is_deleted,0)=0 AND created_at>=DATEADD(DAY,-7,GETDATE())");
        long totalStories = q("SELECT COUNT(*) FROM stories WHERE ISNULL(is_deleted,0)=0");
        long totalChapters = q("SELECT COUNT(*) FROM chapters WHERE ISNULL(is_deleted,0)=0");
        long pendingReports = q("SELECT COUNT(*) FROM reports WHERE status='PENDING'");
        long totalCoins = q("SELECT ISNULL(SUM(coin_amount),0) FROM payment_orders WHERE status='SUCCESS'");
        long totalVnd = q("SELECT ISNULL(SUM(amount_vnd),0) FROM payment_orders WHERE status='SUCCESS'");
        long successOrders = q("SELECT COUNT(*) FROM payment_orders WHERE status='SUCCESS'");
        long failedOrders = q("SELECT COUNT(*) FROM payment_orders WHERE status='FAILED' OR status='CANCELLED'");

        // -- Thống kê tổng quan (Thẻ lớn) Grid 3x3 --
        JPanel statsPanel = new JPanel(new GridLayout(3, 3, 12, 12));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        statsPanel.add(createStatCard("👥 Tổng User", String.format("%,d", totalUsers), new Color(0, 102, 204)));
        statsPanel.add(createStatCard("👤 User mới (7 ngày)", String.format("%,d", newUsers), new Color(0, 153, 76)));
        statsPanel.add(createStatCard("📚 Tổng Truyện", String.format("%,d", totalStories), new Color(102, 16, 242)));
        statsPanel.add(createStatCard("📖 Tổng Chương", String.format("%,d", totalChapters), new Color(23, 162, 184)));
        statsPanel.add(createStatCard("🚨 Báo cáo chờ xử lý", String.format("%,d", pendingReports), new Color(255, 193, 7)));
        statsPanel.add(createStatCard("🪙 Tổng Coin nạp", String.format("%,d", totalCoins), new Color(255, 140, 0)));
        statsPanel.add(createStatCard("💵 Doanh thu VNĐ", String.format("%,d đ", totalVnd), new Color(40, 167, 69)));
        statsPanel.add(createStatCard("✅ Đơn nạp thành công", String.format("%,d", successOrders), new Color(32, 201, 151)));
        statsPanel.add(createStatCard("⚠️ Đơn nạp thất bại / Hủy", String.format("%,d", failedOrders), new Color(220, 53, 69)));

        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(25));

        // -- Dynamic Section Label --
        JLabel lblMenuHeader = new JLabel("Chức năng quản trị");
        lblMenuHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMenuHeader.setForeground(new Color(50, 50, 50));
        lblMenuHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblMenuHeader);
        contentPanel.add(Box.createVerticalStrut(10));

        // -- Menu Cards Grid (Layout 3 cột) --
        JPanel menuPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        menuPanel.setOpaque(false);

        menuPanel.add(createMenuCard("Duyệt Truyện", "Phê duyệt / Từ chối truyện mới", e -> {
            new AdminStoryModerationFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Duyệt Chương", "Phê duyệt / Từ chối chương mới", e -> {
            new AdminChapterModerationFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Quản lý User", "Khóa / Mở khóa / Cấp quyền", e -> {
            new AdminUserFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Quản lý Thể loại", "Thêm / Sửa / Ẩn thể loại", e -> {
            new AdminGenreFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Thống kê", "Xem thống kê hệ thống chi tiết", e -> {
            new AdminStatsFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Xử lý Báo cáo", "Xem và xử lý các báo cáo vi phạm", e -> {
            new AdminReportFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Kiểm duyệt Bình luận", "Ẩn / Hiện / Xóa bình luận", e -> {
            new AdminCommentModerationFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Giao dịch & Coin", "Xem lịch sử + hoàn tiền / điều chỉnh", e -> {
            new AdminTransactionFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Thông báo hệ thống", "Gửi thông báo hàng loạt cho user", e -> {
            new AdminNotificationFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Khuyến mãi", "Quản lý gói Coin khuyến mãi", e -> {
            new AdminPromotionFrame().setVisible(true);
            this.dispose();
        }));

        contentPanel.add(menuPanel);

        // ScrollPane hỗ trợ cuộn mượt mà khi màn hình nhỏ
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)),
                        new EmptyBorder(10, 14, 10, 14)
                )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(new Color(100, 100, 100));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblVal.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMenuCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel lblDesc = new JLabel("<html>" + desc + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(Color.GRAY);

        JButton btn = new JButton("Vào →");
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(lblTitle);
        top.add(Box.createVerticalStrut(6));
        top.add(lblDesc);

        card.add(top, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    private long q(String sql) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
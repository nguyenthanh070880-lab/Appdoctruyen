package com.novelapp.view;

import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        setSize(900, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Trang quản trị");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // ===== Các chức năng =====
        JPanel menuPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        menuPanel.add(createMenuCard("Duyệt Truyện", "Phê duyệt / Từ chối truyện mới", e -> {
            new AdminStoryModerationFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Duyệt Chương", "Phê duyệt / Từ chối chương mới", e -> {
            new AdminChapterModerationFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Quản lý User", "Xem / Khóa tài khoản (sẽ làm sau)", e -> {
            new AdminUserFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Thống kê", "Xem thống kê hệ thống", e -> {
            new AdminStatsFrame().setVisible(true);
            this.dispose();
        }));

        menuPanel.add(createMenuCard("Quản lý Thể loại", "Thêm / Sửa / Ẩn thể loại truyện", e -> {
            new AdminGenreFrame().setVisible(true);
            this.dispose();
        }));

        JButton btnBack = new JButton("Quay lại trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createMenuCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(new Color(245, 247, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(Color.GRAY);

        JButton btn = new JButton("Vào");
        btn.setFocusPainted(false);
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
}

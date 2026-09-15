package com.novelapp.view;

import com.novelapp.dao.NotificationDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class NotificationFrame extends JFrame {

    private final User currentUser;
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private JPanel listPanel;

    public NotificationFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        setTitle("Thông báo - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông báo");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnMarkAll = new JButton("Đánh dấu tất cả đã đọc");
        btnMarkAll.setFocusPainted(false);
        btnMarkAll.addActionListener(e -> {
            notificationDAO.markAllAsRead(currentUser.getUserId());
            loadNotifications();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnMarkAll, BorderLayout.EAST);

        // List
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Back
        JButton btnBack = new JButton("Quay lại trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadNotifications() {
        listPanel.removeAll();
        List<Map<String, Object>> list = notificationDAO.getNotifications(currentUser.getUserId());

        if (list.isEmpty()) {
            JLabel empty = new JLabel("Chưa có thông báo nào.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(empty);
        } else {
            for (Map<String, Object> n : list) {
                listPanel.add(createNotificationCard(n));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createNotificationCard(Map<String, Object> n) {
        boolean isRead = (boolean) n.get("isRead");

        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(isRead ? new Color(250, 250, 250) : new Color(232, 245, 253));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel((String) n.get("title"));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblContent = new JLabel((String) n.get("content"));
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblContent.setForeground(Color.DARK_GRAY);

        JLabel lblTime = new JLabel(n.get("createdAt").toString());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(Color.GRAY);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblContent);
        left.add(Box.createVerticalStrut(4));
        left.add(lblTime);

        card.add(left, BorderLayout.CENTER);

        // Click để đánh dấu đã đọc
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isRead) {
                    notificationDAO.markAsRead((int) n.get("id"));
                    loadNotifications();
                }
            }
        });

        return card;
    }
}
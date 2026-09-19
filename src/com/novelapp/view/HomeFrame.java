package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.StoryDAO;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.List;

public class HomeFrame extends JFrame {

    private User currentUser;
    private JPanel contentPanel;
    private final StoryDAO storyDAO = new StoryDAO();

    public HomeFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadStories();
    }

    private void initComponents() {
        setTitle("NovelApp - Trang chủ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setPreferredSize(new Dimension(0, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

        JLabel lblLogo = new JLabel("📚  NOVEL APP");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblWelcome = new JLabel("Xin chào, " + currentUser.getFullName() + "  ");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcome.setForeground(new Color(220, 230, 255));

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(new Color(0, 102, 204));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(lblWelcome);
        rightHeader.add(btnLogout);

        headerPanel.add(lblLogo, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 247, 250));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 12, 25, 12));

        sidebar.add(createMenuButton("🏠  Trang chủ", e -> loadStories()));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("🔍  Khám phá", e -> {
            new SearchFrame().setVisible(true);
            this.dispose();
        }));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("🏆  Xếp hạng & Đề xuất", e -> {
            new RankingFrame().setVisible(true);
            this.dispose();
        }));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("📚  Thư viện", e -> {
            new LibraryFrame().setVisible(true);
            this.dispose();
        }));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("💰  Ví Coin", e -> {
            new WalletFrame().setVisible(true);
            this.dispose();
        }));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("🔔  Thông báo", e -> {
            new NotificationFrame().setVisible(true);
            this.dispose();
        }));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("👤  Hồ sơ cá nhân", e -> {
            new ProfileFrame().setVisible(true);
            this.dispose();
        }));

        if (currentUser.hasRole("AUTHOR") || currentUser.hasRole("ADMIN")) {
            sidebar.add(Box.createVerticalStrut(25));
            JLabel lblAuthor = new JLabel("  — TÁC GIẢ —");
            lblAuthor.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblAuthor.setForeground(new Color(120, 120, 120));
            sidebar.add(lblAuthor);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(createMenuButton("✍️  Quản lý truyện", e -> {
                new AuthorStoryFrame().setVisible(true);
                this.dispose();
            }));
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(createMenuButton("📊  Thống kê & Doanh thu", e -> {
                new AuthorStatsFrame().setVisible(true);
                this.dispose();
            }));
        }

        if (currentUser.hasRole("ADMIN") || currentUser.hasRole("STAFF")) {
            sidebar.add(Box.createVerticalStrut(25));
            JLabel lblAdmin = new JLabel("  — QUẢN TRỊ —");
            lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblAdmin.setForeground(new Color(120, 120, 120));
            sidebar.add(lblAdmin);
            sidebar.add(Box.createVerticalStrut(10));
            sidebar.add(createMenuButton("⚙️  Admin Dashboard", e -> {
                new AdminDashboardFrame().setVisible(true);
                this.dispose();
            }));
        }

        // ===== CONTENT =====
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadStories() {
        contentPanel.removeAll();

        // ========== 1. DANH MỤC THỂ LOẠI ==========
        JLabel lblGenres = new JLabel("📂  Thể loại");
        lblGenres.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGenres.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblGenres);
        contentPanel.add(Box.createVerticalStrut(10));

        JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        genrePanel.setOpaque(false);
        genrePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        genrePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT genre_id, genre_name FROM genres WHERE is_active = 1 ORDER BY genre_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int genreId = rs.getInt("genre_id");
                String name = rs.getString("genre_name");
                JButton btn = new JButton(name);
                btn.setFocusPainted(false);
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                final int gId = genreId;
                final String gName = name;
                btn.addActionListener(e -> {
                    new SearchFrame(gId, gName).setVisible(true);
                    this.dispose();
                });
                genrePanel.add(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            genrePanel.add(new JLabel("Chưa có thể loại."));
        }
        contentPanel.add(genrePanel);
        contentPanel.add(Box.createVerticalStrut(25));

        // ========== 2. TRUYỆN NỔI BẬT ==========
        JLabel lblHot = new JLabel("🔥  Truyện nổi bật");
        lblHot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHot.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblHot);
        contentPanel.add(Box.createVerticalStrut(12));

        List<Story> hotStories = storyDAO.getTopStoriesByView(5);
        if (hotStories.isEmpty()) {
            contentPanel.add(new JLabel("Chưa có dữ liệu."));
        } else {
            for (Story s : hotStories) {
                contentPanel.add(createStoryCard(s));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
        contentPanel.add(Box.createVerticalStrut(25));

        // ========== 3. TRUYỆN MỚI CẬP NHẬT ==========
        JLabel lblTitle = new JLabel("📖  Truyện mới cập nhật");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);
        contentPanel.add(Box.createVerticalStrut(12));

        List<Story> stories = storyDAO.getApprovedStories(20);
        if (stories.isEmpty()) {
            JLabel empty = new JLabel("Chưa có truyện nào.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            empty.setForeground(Color.GRAY);
            contentPanel.add(empty);
        } else {
            for (Story story : stories) {
                contentPanel.add(createStoryCard(story));
                contentPanel.add(Box.createVerticalStrut(12));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createStoryCard(Story story) {
        JPanel card = new JPanel(new BorderLayout(18, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(245, 248, 255));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
                        BorderFactory.createEmptyBorder(13, 17, 13, 17)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18)
                ));
            }
        });

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(25, 25, 25));

        JLabel lblAuthor = new JLabel("Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAuthor.setForeground(new Color(90, 90, 90));

        JLabel lblMeta = new JLabel(String.format("👁  %,d lượt xem    |    %s    |    ★ %.1f",
                story.getViewCount(), story.getStatus(), story.getRatingAvg()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(new Color(130, 130, 130));

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblAuthor);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblMeta);

        JButton btnDetail = new JButton("Xem chi tiết →");
        btnDetail.setFocusPainted(false);
        btnDetail.setBackground(new Color(0, 102, 204));
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setBorderPainted(false);
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetail.setPreferredSize(new Dimension(130, 38));
        btnDetail.addActionListener(e -> {
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            this.dispose();
        });

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnDetail, BorderLayout.EAST);
        return card;
    }

    private JButton createMenuButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(190, 42));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(0, 102, 204));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(50, 50, 50));
            }
        });
        btn.addActionListener(action);
        return btn;
    }
}
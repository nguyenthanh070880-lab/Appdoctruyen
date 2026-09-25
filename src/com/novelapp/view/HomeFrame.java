package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.StoryDAO;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblLogo = new JLabel("📚  NOVEL APP");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        JTextField txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(220, 32));

        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFocusPainted(false);
        btnSearch.setBackground(Color.WHITE);
        btnSearch.setForeground(new Color(0, 102, 204));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Logic xử lý khi tìm kiếm: Lấy nội dung từ ô nhập liệu và chuyển sang SearchFrame
        Runnable openSearch = () -> {
            String kw = txtSearch.getText().trim();
            new SearchFrame(-1, null, kw.isEmpty() ? null : kw).setVisible(true);
            this.dispose();
        };

        btnSearch.addActionListener(e -> openSearch.run());
        txtSearch.addActionListener(e -> openSearch.run());

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        searchBox.setOpaque(false);
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);

        JLabel lblWelcome = new JLabel("Xin chào, " + currentUser.getFullName() + "  ");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblWelcome.setForeground(new Color(220, 230, 255));

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(new Color(0, 102, 204));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);
        rightHeader.add(lblWelcome);
        rightHeader.add(btnLogout);

        headerPanel.add(lblLogo, BorderLayout.WEST);
        headerPanel.add(searchBox, BorderLayout.CENTER);
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
        contentPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

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

        // ===== THỂ LOẠI =====
        JLabel lblGenres = new JLabel("📂  Thể loại");
        lblGenres.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGenres.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblGenres);
        contentPanel.add(Box.createVerticalStrut(10));

        JButton btnGenreMenu = new JButton("Chọn thể loại  ▼");
        btnGenreMenu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnGenreMenu.setFocusPainted(false);
        btnGenreMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGenreMenu.setPreferredSize(new Dimension(180, 36));
        btnGenreMenu.setMaximumSize(new Dimension(180, 36));
        btnGenreMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGenreMenu.addActionListener(e -> showGenrePopup(btnGenreMenu));

        contentPanel.add(btnGenreMenu);
        contentPanel.add(Box.createVerticalStrut(22));

        // ===== TRUYỆN NỔI BẬT (hàng ảnh ngang) =====
        JLabel lblHot = new JLabel("🔥  Truyện nổi bật / Đề cử");
        lblHot.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHot.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblHot);
        contentPanel.add(Box.createVerticalStrut(12));

        JPanel hotRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        hotRow.setOpaque(false);
        hotRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        hotRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        List<Story> hotStories = storyDAO.getTopStoriesByView(5);
        if (hotStories.isEmpty()) {
            hotRow.add(new JLabel("Chưa có dữ liệu."));
        } else {
            for (Story s : hotStories) {
                hotRow.add(createMiniCoverCard(s));
            }
        }
        contentPanel.add(hotRow);
        contentPanel.add(Box.createVerticalStrut(22));

        // ===== MỚI CẬP NHẬT (list có bìa) =====
        JLabel lblNew = new JLabel("📖  Truyện mới cập nhật");
        lblNew.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblNew.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblNew);
        contentPanel.add(Box.createVerticalStrut(12));

        List<Story> stories = storyDAO.getApprovedStories(20);
        if (stories.isEmpty()) {
            JLabel empty = new JLabel("Chưa có truyện nào.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            contentPanel.add(empty);
        } else {
            for (Story story : stories) {
                contentPanel.add(createStoryCard(story));
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showGenrePopup(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT genre_id, genre_name FROM genres WHERE is_active = 1 ORDER BY genre_name");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int genreId = rs.getInt("genre_id");
                String name = rs.getString("genre_name");

                JButton item = new JButton(name);
                item.setHorizontalAlignment(SwingConstants.LEFT);
                item.setMaximumSize(new Dimension(220, 32));
                item.setPreferredSize(new Dimension(220, 32));
                item.setFocusPainted(false);
                item.setContentAreaFilled(false);
                item.setBorderPainted(false);
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                item.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                item.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        item.setOpaque(true);
                        item.setBackground(new Color(230, 242, 255));
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        item.setOpaque(false);
                        item.setBackground(Color.WHITE);
                    }
                });

                final int gId = genreId;
                final String gName = name;

                item.addActionListener(ev -> {
                    popup.setVisible(false);
                    new SearchFrame(gId, gName).setVisible(true);
                    HomeFrame.this.dispose();
                });

                listPanel.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listPanel.add(new JLabel("  Không tải được thể loại"));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setPreferredSize(new Dimension(240, 220)); // cao cố định → lăn chuột
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        popup.add(scroll);
        popup.show(invoker, 0, invoker.getHeight());
    }

    /** Card nhỏ kiểu web: ảnh + tên */
    private JPanel createMiniCoverCard(Story story) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setPreferredSize(new Dimension(120, 185));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225)));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel cover = new JLabel("No img", SwingConstants.CENTER);
        cover.setPreferredSize(new Dimension(120, 145));
        cover.setOpaque(true);
        cover.setBackground(new Color(235, 235, 235));
        if (story.getCoverUrl() != null && !story.getCoverUrl().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(story.getCoverUrl());
                Image img = icon.getImage().getScaledInstance(120, 145, Image.SCALE_SMOOTH);
                cover.setIcon(new ImageIcon(img));
                cover.setText("");
            } catch (Exception ignored) {}
        }

        String title = story.getTitle();
        if (title != null && title.length() > 28) title = title.substring(0, 26) + "…";
        JLabel name = new JLabel("<html><center>" + title + "</center></html>", SwingConstants.CENTER);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        p.add(cover, BorderLayout.CENTER);
        p.add(name, BorderLayout.SOUTH);

        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new StoryDetailFrame(story.getStoryId()).setVisible(true);
                HomeFrame.this.dispose();
            }
        });
        return p;
    }

    /** Card list: bìa trái + info */
    private JPanel createStoryCard(Story story) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblCover = new JLabel("No img", SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(64, 85));
        lblCover.setOpaque(true);
        lblCover.setBackground(new Color(240, 240, 240));
        lblCover.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        if (story.getCoverUrl() != null && !story.getCoverUrl().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(story.getCoverUrl());
                Image img = icon.getImage().getScaledInstance(64, 85, Image.SCALE_SMOOTH);
                lblCover.setIcon(new ImageIcon(img));
                lblCover.setText("");
            } catch (Exception ignored) {}
        }

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel lblAuthor = new JLabel("Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAuthor.setForeground(Color.GRAY);
        JLabel lblMeta = new JLabel(String.format("👁 %,d  |  %s  |  ★ %.1f",
                story.getViewCount(), story.getStatus(), story.getRatingAvg()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(new Color(120, 120, 120));

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(4));
        info.add(lblAuthor);
        info.add(Box.createVerticalStrut(4));
        info.add(lblMeta);

        JButton btn = new JButton("Xem");
        btn.setPreferredSize(new Dimension(80, 32));
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            HomeFrame.this.dispose();
        });

        card.add(lblCover, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(btn, BorderLayout.EAST);
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
package com.novelapp.view;

import com.novelapp.dao.StoryDAO;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RankingFrame extends JFrame {

    private final User currentUser;
    private final StoryDAO storyDAO = new StoryDAO();
    private JPanel contentPanel;

    public RankingFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadRanking("view");
    }

    private void initComponents() {
        setTitle("Bảng xếp hạng & Đề xuất - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🏆  Bảng xếp hạng & Đề xuất");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
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

        // Tabs
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        tabPanel.setBackground(Color.WHITE);
        tabPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        JButton btnView = createTabButton("Top Lượt xem");
        JButton btnRating = createTabButton("Top Đánh giá");
        JButton btnRecommend = createTabButton("Đề xuất cho bạn");

        btnView.addActionListener(e -> loadRanking("view"));
        btnRating.addActionListener(e -> loadRanking("rating"));
        btnRecommend.addActionListener(e -> loadRanking("recommend"));

        tabPanel.add(btnView);
        tabPanel.add(btnRating);
        tabPanel.add(btnRecommend);

        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel center = new JPanel(new BorderLayout());
        center.add(tabPanel, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(center, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return btn;
    }

    private void loadRanking(String type) {
        contentPanel.removeAll();

        List<Story> stories;
        String title;

        switch (type) {
            case "rating":
                stories = storyDAO.getTopStoriesByRating(15);
                title = "Top truyện đánh giá cao nhất";
                break;
            case "recommend":
                stories = storyDAO.getRecommendedStories(15);
                title = "Đề xuất dành cho bạn";
                break;
            default:
                stories = storyDAO.getTopStoriesByView(15);
                title = "Top truyện có lượt xem cao nhất";
        }

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lbl);
        contentPanel.add(Box.createVerticalStrut(18));

        if (stories.isEmpty()) {
            contentPanel.add(new JLabel("Chưa có dữ liệu."));
        } else {
            int rank = 1;
            for (Story story : stories) {
                contentPanel.add(createRankCard(rank, story));
                contentPanel.add(Box.createVerticalStrut(10));
                rank++;
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createRankCard(int rank, Story story) {
        JPanel card = new JPanel(new BorderLayout(15, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRank = new JLabel("#" + rank);
        lblRank.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRank.setForeground(rank <= 3 ? new Color(220, 53, 69) : new Color(100, 100, 100));
        lblRank.setPreferredSize(new Dimension(55, 0));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel lblAuthor = new JLabel("Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAuthor.setForeground(Color.GRAY);
        JLabel lblMeta = new JLabel(String.format("👁 %,d   |   ★ %.1f (%d đánh giá)",
                story.getViewCount(), story.getRatingAvg(), story.getRatingCount()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(new Color(130, 130, 130));

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(4));
        info.add(lblAuthor);
        info.add(Box.createVerticalStrut(4));
        info.add(lblMeta);

        JButton btn = new JButton("Xem");
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            this.dispose();
        });

        card.add(lblRank, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(btn, BorderLayout.EAST);
        return card;
    }
}
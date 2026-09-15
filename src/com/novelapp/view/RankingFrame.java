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
        setSize(950, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Bảng xếp hạng & Đề xuất");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // Nút chuyển loại xếp hạng
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        tabPanel.setOpaque(false);

        JButton btnView = new JButton("Top Lượt xem");
        JButton btnRating = new JButton("Top Đánh giá");
        JButton btnRecommend = new JButton("Đề xuất cho bạn");

        btnView.setFocusPainted(false);
        btnRating.setFocusPainted(false);
        btnRecommend.setFocusPainted(false);

        btnView.addActionListener(e -> loadRanking("view"));
        btnRating.addActionListener(e -> loadRanking("rating"));
        btnRecommend.addActionListener(e -> loadRanking("recommend"));

        tabPanel.add(btnView);
        tabPanel.add(btnRating);
        tabPanel.add(btnRecommend);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton btnBack = new JButton("Quay lại trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnBack);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(tabPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
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
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lbl);
        contentPanel.add(Box.createVerticalStrut(15));

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
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Số xếp hạng
        JLabel lblRank = new JLabel("#" + rank);
        lblRank.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRank.setForeground(rank <= 3 ? new Color(220, 53, 69) : new Color(100, 100, 100));
        lblRank.setPreferredSize(new Dimension(50, 0));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel lblAuthor = new JLabel("Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAuthor.setForeground(Color.DARK_GRAY);

        JLabel lblMeta = new JLabel(String.format("Lượt xem: %,d  |  %.1f★ (%d đánh giá)",
                story.getViewCount(), story.getRatingAvg(), story.getRatingCount()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(Color.GRAY);

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblAuthor);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblMeta);

        JButton btnDetail = new JButton("Xem");
        btnDetail.setFocusPainted(false);
        btnDetail.addActionListener(e -> {
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            this.dispose();
        });

        card.add(lblRank, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnDetail, BorderLayout.EAST);

        return card;
    }
}
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
    private JPanel listPanel;
    private JComboBox<String> cboType;

    public RankingFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadRanking();
    }

    private void initComponents() {
        setTitle("Xếp hạng & Đề xuất - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🏆  Xếp hạng & Đề xuất");
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

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(10, 20, 5, 20));

        filterPanel.add(new JLabel("Bảng xếp hạng:"));
        cboType = new JComboBox<>(new String[]{
                "Lượt xem cao nhất",
                "Đánh giá cao nhất",
                "Đề xuất cho bạn"
        });
        cboType.addActionListener(e -> loadRanking());
        filterPanel.add(cboType);

        // List
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel center = new JPanel(new BorderLayout());
        center.add(filterPanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadRanking() {
        listPanel.removeAll();

        String type = (String) cboType.getSelectedItem();
        List<Story> list;

        if ("Đánh giá cao nhất".equals(type)) {
            list = storyDAO.getTopStoriesByRating(15);
        } else if ("Đề xuất cho bạn".equals(type)) {
            list = storyDAO.getRecommendedStories(15);
        } else {
            list = storyDAO.getTopStoriesByView(15);
        }

        if (list == null || list.isEmpty()) {
            JLabel empty = new JLabel("Chưa có dữ liệu xếp hạng.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(Color.GRAY);
            listPanel.add(empty);
        } else {
            int rank = 1;
            for (Story s : list) {
                listPanel.add(createRankCard(rank++, s));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createRankCard(int rank, Story story) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Số hạng
        JLabel lblRank = new JLabel(String.format("%02d", rank));
        lblRank.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblRank.setForeground(rank <= 3 ? new Color(220, 53, 69) : new Color(100, 100, 100));
        lblRank.setPreferredSize(new Dimension(42, 40));
        lblRank.setHorizontalAlignment(SwingConstants.CENTER);

        // Ảnh bìa
        JLabel lblCover = new JLabel("No img", SwingConstants.CENTER);
        lblCover.setPreferredSize(new Dimension(56, 75));
        lblCover.setOpaque(true);
        lblCover.setBackground(new Color(240, 240, 240));
        lblCover.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        if (story.getCoverUrl() != null && !story.getCoverUrl().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(story.getCoverUrl());
                Image img = icon.getImage().getScaledInstance(56, 75, Image.SCALE_SMOOTH);
                lblCover.setIcon(new ImageIcon(img));
                lblCover.setText("");
            } catch (Exception ignored) {}
        }

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel lblMeta = new JLabel(String.format("%s  ·  👁 %,d  ·  ★ %.1f  ·  %s",
                story.getAuthorName() != null ? story.getAuthorName() : "N/A",
                story.getViewCount(),
                story.getRatingAvg(),
                story.getStatus() != null ? story.getStatus() : ""));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(Color.GRAY);

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(6));
        info.add(lblMeta);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(lblRank);
        left.add(lblCover);

        JButton btnView = new JButton("Xem");
        btnView.setPreferredSize(new Dimension(80, 32));
        btnView.setBackground(new Color(0, 102, 204));
        btnView.setForeground(Color.WHITE);
        btnView.setFocusPainted(false);
        btnView.setBorderPainted(false);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnView.addActionListener(e -> openDetail(story.getStoryId()));

        card.add(left, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(btnView, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDetail(story.getStoryId());
            }
        });

        return card;
    }

    private void openDetail(int storyId) {
        new StoryDetailFrame(storyId).setVisible(true);
        this.dispose();
    }
}
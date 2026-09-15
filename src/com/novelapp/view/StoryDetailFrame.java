package com.novelapp.view;

import com.novelapp.dao.*;
import com.novelapp.model.Chapter;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StoryDetailFrame extends JFrame {

    private final Story story;
    private final User currentUser;
    private final StoryDAO storyDAO = new StoryDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private final FollowDAO followDAO = new FollowDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final CommentDAO commentDAO = new CommentDAO();
    private final RatingDAO ratingDAO = new RatingDAO();

    private JButton btnFollow;
    private JButton btnFavorite;
    private JPanel commentListPanel;

    public StoryDetailFrame(int storyId) {
        this.currentUser = SessionManager.getCurrentUser();
        this.story = storyDAO.findById(storyId);

        if (story == null) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy truyện!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }

        initComponents();
    }

    private void initComponents() {
        setTitle(story.getTitle() + " - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // ===== Thông tin truyện =====
        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(25, 25, 25));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAuthor = new JLabel("✍️  Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblAuthor.setForeground(new Color(80, 80, 80));
        lblAuthor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMeta = new JLabel(String.format(
                "📊  %s   •   👁 %,d lượt xem   •   ❤️ %,d theo dõi   •   ★ %.1f (%d đánh giá)",
                story.getStatus(), story.getViewCount(), story.getFollowCount(),
                story.getRatingAvg(), story.getRatingCount()
        ));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMeta.setForeground(new Color(120, 120, 120));
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtDescription = new JTextArea(
                story.getDescription() != null ? story.getDescription() : "Chưa có mô tả.");
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setBackground(new Color(248, 248, 248));
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        txtDescription.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(lblAuthor);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblMeta);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(txtDescription);

        // ===== Nút hành động =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnFollow = new JButton();
        btnFollow.setPreferredSize(new Dimension(130, 36));
        btnFollow.setFocusPainted(false);
        updateFollowButton();

        btnFavorite = new JButton();
        btnFavorite.setPreferredSize(new Dimension(130, 36));
        btnFavorite.setFocusPainted(false);
        updateFavoriteButton();

        JButton btnRate = new JButton("Đánh giá");
        btnRate.setPreferredSize(new Dimension(110, 36));
        btnRate.setFocusPainted(false);
        btnRate.addActionListener(e -> showRatingDialog());

        JButton btnReport = new JButton("Báo cáo");
        btnReport.setPreferredSize(new Dimension(100, 36));
        btnReport.setFocusPainted(false);
        btnReport.setForeground(new Color(220, 53, 69));
        btnReport.addActionListener(e -> showReportDialog("STORY", story.getStoryId()));

        JButton btnBack = new JButton("Quay lại");
        btnBack.setPreferredSize(new Dimension(100, 36));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        btnFollow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFavorite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnFollow.addActionListener(e -> {
            if (followDAO.isFollowing(currentUser.getUserId(), story.getStoryId())) {
                followDAO.unfollow(currentUser.getUserId(), story.getStoryId());
                JOptionPane.showMessageDialog(this, "Đã bỏ theo dõi.");
            } else {
                followDAO.follow(currentUser.getUserId(), story.getStoryId());
                JOptionPane.showMessageDialog(this, "Đã theo dõi truyện!");
            }
            updateFollowButton();
        });

        btnFavorite.addActionListener(e -> {
            if (favoriteDAO.isFavorited(currentUser.getUserId(), story.getStoryId())) {
                favoriteDAO.removeFavorite(currentUser.getUserId(), story.getStoryId());
                JOptionPane.showMessageDialog(this, "Đã bỏ yêu thích.");
            } else {
                favoriteDAO.addFavorite(currentUser.getUserId(), story.getStoryId());
                JOptionPane.showMessageDialog(this, "Đã thêm vào yêu thích!");
            }
            updateFavoriteButton();
        });

        buttonPanel.add(btnFollow);
        buttonPanel.add(btnFavorite);
        buttonPanel.add(btnRate);
        buttonPanel.add(btnReport);
        buttonPanel.add(btnBack);

        infoPanel.add(Box.createVerticalStrut(18));
        infoPanel.add(buttonPanel);

        // ===== Danh sách chương =====
        JLabel lblChapters = new JLabel("Danh sách chương");
        lblChapters.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblChapters.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chapterListPanel = new JPanel();
        chapterListPanel.setLayout(new BoxLayout(chapterListPanel, BoxLayout.Y_AXIS));
        chapterListPanel.setOpaque(false);

        List<Chapter> chapters = chapterDAO.getChaptersByStoryId(story.getStoryId());
        if (chapters.isEmpty()) {
            chapterListPanel.add(new JLabel("Chưa có chương nào."));
        } else {
            for (Chapter chapter : chapters) {
                chapterListPanel.add(createChapterRow(chapter));
                chapterListPanel.add(Box.createVerticalStrut(6));
            }
        }

        infoPanel.add(Box.createVerticalStrut(25));
        infoPanel.add(lblChapters);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(chapterListPanel);

        // ===== Bình luận =====
        JLabel lblComment = new JLabel("Bình luận");
        lblComment.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblComment.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Form viết bình luận
        JPanel writePanel = new JPanel(new BorderLayout(10, 5));
        writePanel.setOpaque(false);
        writePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        writePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtComment = new JTextArea(3, 40);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JButton btnSend = new JButton("Gửi");
        btnSend.setFocusPainted(false);
        btnSend.setPreferredSize(new Dimension(80, 40));
        btnSend.addActionListener(e -> {
            String content = txtComment.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung bình luận!");
                return;
            }

            if (commentDAO.addComment(currentUser.getUserId(), story.getStoryId(), null, content)) {
                txtComment.setText("");
                loadComments();
                JOptionPane.showMessageDialog(this, "Đã gửi bình luận!");
            }
        });

        writePanel.add(new JScrollPane(txtComment), BorderLayout.CENTER);
        writePanel.add(btnSend, BorderLayout.EAST);

        commentListPanel = new JPanel();
        commentListPanel.setLayout(new BoxLayout(commentListPanel, BoxLayout.Y_AXIS));
        commentListPanel.setOpaque(false);
        commentListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(lblComment);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(writePanel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(commentListPanel);

        loadComments();

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void loadComments() {
        commentListPanel.removeAll();
        List<Map<String, Object>> comments = commentDAO.getCommentsByStory(story.getStoryId());

        if (comments.isEmpty()) {
            JLabel empty = new JLabel("Chưa có bình luận nào.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(Color.GRAY);
            commentListPanel.add(empty);
        } else {
            for (Map<String, Object> c : comments) {
                commentListPanel.add(createCommentCard(c));
                commentListPanel.add(Box.createVerticalStrut(8));
            }
        }

        commentListPanel.revalidate();
        commentListPanel.repaint();
    }

    private JPanel createCommentCard(Map<String, Object> c) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(new Color(248, 248, 248));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = new JLabel(c.get("fullName") + " (@" + c.get("username") + ")");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblContent = new JLabel("<html>" + c.get("content") + "</html>");
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblTime = new JLabel(c.get("createdAt").toString());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(Color.GRAY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lblUser, BorderLayout.WEST);
        top.add(lblTime, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(lblContent, BorderLayout.CENTER);

        return card;
    }

    private void showRatingDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 8));

        JLabel lbl = new JLabel("Chọn số sao (1-5):");
        String[] stars = {"1 ★", "2 ★", "3 ★", "4 ★", "5 ★"};
        JComboBox<String> cboStar = new JComboBox<>(stars);

        int current = ratingDAO.getUserRating(currentUser.getUserId(), story.getStoryId());
        if (current > 0) cboStar.setSelectedIndex(current - 1);

        JTextArea txtReview = new JTextArea(3, 25);
        txtReview.setLineWrap(true);
        txtReview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(lbl);
        panel.add(cboStar);
        panel.add(new JScrollPane(txtReview));

        int result = JOptionPane.showConfirmDialog(this, panel, "Đánh giá truyện",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int score = cboStar.getSelectedIndex() + 1;
            String review = txtReview.getText().trim();

            if (ratingDAO.addOrUpdateRating(currentUser.getUserId(), story.getStoryId(), score, review)) {
                JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã đánh giá!");
                // Reload trang để cập nhật điểm
                new StoryDetailFrame(story.getStoryId()).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra!");
            }
        }
    }

    private void showReportDialog(String targetType, int targetId) {
        String[] reasons = {
                "Nội dung vi phạm",
                "Spam / Quảng cáo",
                "Nội dung không phù hợp",
                "Trùng lặp / Đạo văn",
                "Lỗi kỹ thuật",
                "Khác"
        };

        JComboBox<String> cboReason = new JComboBox<>(reasons);
        JTextArea txtDesc = new JTextArea(4, 25);
        txtDesc.setLineWrap(true);
        txtDesc.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.add(new JLabel("Lý do báo cáo:"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(cboReason, BorderLayout.NORTH);
        center.add(new JScrollPane(txtDesc), BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Báo cáo nội dung",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String reason = (String) cboReason.getSelectedItem();
            String desc = txtDesc.getText().trim();

            ReportDAO reportDAO = new ReportDAO();
            if (reportDAO.createReport(currentUser.getUserId(), targetType, targetId, reason, desc)) {
                JOptionPane.showMessageDialog(this, "Đã gửi báo cáo. Cảm ơn bạn!");
            } else {
                JOptionPane.showMessageDialog(this, "Gửi báo cáo thất bại!");
            }
        }
    }

    private void updateFollowButton() {
        if (followDAO.isFollowing(currentUser.getUserId(), story.getStoryId())) {
            btnFollow.setText("Bỏ theo dõi");
            btnFollow.setBackground(new Color(220, 53, 69));
            btnFollow.setForeground(Color.WHITE);
        } else {
            btnFollow.setText("Theo dõi");
            btnFollow.setBackground(UIManager.getColor("Button.background"));
            btnFollow.setForeground(Color.BLACK);
        }
    }

    private void updateFavoriteButton() {
        if (favoriteDAO.isFavorited(currentUser.getUserId(), story.getStoryId())) {
            btnFavorite.setText("Bỏ yêu thích");
            btnFavorite.setBackground(new Color(255, 193, 7));
            btnFavorite.setForeground(Color.BLACK);
        } else {
            btnFavorite.setText("Yêu thích");
            btnFavorite.setBackground(UIManager.getColor("Button.background"));
            btnFavorite.setForeground(Color.BLACK);
        }
    }

    private JPanel createChapterRow(Chapter chapter) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(new Color(250, 250, 250));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String priceText = chapter.isFree() ? "Miễn phí" : chapter.getPriceCoin() + " Coin";
        JLabel lblInfo = new JLabel(String.format("Chương %.0f: %s   (%s)",
                chapter.getChapterNumber(), chapter.getTitle(), priceText));
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnRead = new JButton("Đọc");
        btnRead.setFocusPainted(false);
        btnRead.setPreferredSize(new Dimension(80, 30));
        btnRead.addActionListener(e -> handleReadChapter(chapter));

        row.add(lblInfo, BorderLayout.CENTER);
        row.add(btnRead, BorderLayout.EAST);
        return row;
    }

    private void handleReadChapter(Chapter chapter) {
        if (chapter.isFree()) {
            openReader(chapter.getChapterId());
            return;
        }

        ChapterAccessDAO accessDAO = new ChapterAccessDAO();
        WalletDAO walletDAO = new WalletDAO();

        if (accessDAO.hasAccess(currentUser.getUserId(), chapter.getChapterId())) {
            openReader(chapter.getChapterId());
            return;
        }

        int price = chapter.getPriceCoin();
        long balance = walletDAO.getBalance(currentUser.getUserId());

        int choice = JOptionPane.showConfirmDialog(this,
                "Chương này cần " + price + " Coin để mở khóa.\nSố dư hiện tại: " + balance + " Coin\n\nBạn có muốn mở khóa không?",
                "Mở khóa chương", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        if (balance < price) {
            JOptionPane.showMessageDialog(this, "Bạn không đủ Coin!", "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = walletDAO.deductCoin(currentUser.getUserId(), price, "UNLOCK",
                "Mở khóa chương " + (int) chapter.getChapterNumber() + ": " + chapter.getTitle());

        if (success) {
            accessDAO.grantAccess(currentUser.getUserId(), chapter.getChapterId(), "PURCHASE");
            JOptionPane.showMessageDialog(this, "Mở khóa thành công!");
            openReader(chapter.getChapterId());
        } else {
            JOptionPane.showMessageDialog(this, "Mở khóa thất bại!");
        }
    }

    private void openReader(int chapterId) {
        new ReaderFrame(chapterId).setVisible(true);
        this.dispose();
    }
}
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
        setSize(1000, 780);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 35, 25, 35));

        // ===== THÔNG TIN TRUYỆN =====
        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(25, 25, 25));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAuthor = new JLabel("✍️  Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblAuthor.setForeground(new Color(80, 80, 80));
        lblAuthor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMeta = new JLabel(String.format(
                "📊 %s   •   👁 %,d lượt xem   •   ❤️ %,d theo dõi   •   ★ %.1f (%d đánh giá)",
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
        txtDescription.setBackground(new Color(248, 249, 250));
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(12, 14, 12, 14)
        ));
        txtDescription.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(10));
        content.add(lblAuthor);
        content.add(Box.createVerticalStrut(8));
        content.add(lblMeta);
        content.add(Box.createVerticalStrut(18));
        content.add(txtDescription);

        // ===== NÚT HÀNH ĐỘNG =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnFollow = new JButton();
        btnFollow.setPreferredSize(new Dimension(130, 38));
        btnFollow.setFocusPainted(false);
        btnFollow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateFollowButton();

        btnFavorite = new JButton();
        btnFavorite.setPreferredSize(new Dimension(130, 38));
        btnFavorite.setFocusPainted(false);
        btnFavorite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateFavoriteButton();

        JButton btnRate = createActionButton("⭐ Đánh giá");
        JButton btnReport = createActionButton("🚩 Báo cáo");
        JButton btnBack = createActionButton("← Quay lại");

        btnFollow.addActionListener(e -> {
            if (followDAO.isFollowing(currentUser.getUserId(), story.getStoryId())) {
                followDAO.unfollow(currentUser.getUserId(), story.getStoryId());
            } else {
                followDAO.follow(currentUser.getUserId(), story.getStoryId());
            }
            updateFollowButton();
        });

        btnFavorite.addActionListener(e -> {
            if (favoriteDAO.isFavorited(currentUser.getUserId(), story.getStoryId())) {
                favoriteDAO.removeFavorite(currentUser.getUserId(), story.getStoryId());
            } else {
                favoriteDAO.addFavorite(currentUser.getUserId(), story.getStoryId());
            }
            updateFavoriteButton();
        });

        btnRate.addActionListener(e -> showRatingDialog());
        btnReport.addActionListener(e -> showReportDialog("STORY", story.getStoryId()));
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnFollow);
        buttonPanel.add(btnFavorite);
        buttonPanel.add(btnRate);
        buttonPanel.add(btnReport);
        buttonPanel.add(btnBack);

        content.add(Box.createVerticalStrut(20));
        content.add(buttonPanel);

        // ===== DANH SÁCH CHƯƠNG =====
        JLabel lblChapters = new JLabel("📖  Danh sách chương");
        lblChapters.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblChapters.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chapterListPanel = new JPanel();
        chapterListPanel.setLayout(new BoxLayout(chapterListPanel, BoxLayout.Y_AXIS));
        chapterListPanel.setOpaque(false);
        chapterListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Chapter> chapters = chapterDAO.getChaptersByStoryId(story.getStoryId());
        if (chapters.isEmpty()) {
            JLabel empty = new JLabel("Chưa có chương nào.");
            empty.setForeground(Color.GRAY);
            chapterListPanel.add(empty);
        } else {
            for (Chapter chapter : chapters) {
                chapterListPanel.add(createChapterRow(chapter));
                chapterListPanel.add(Box.createVerticalStrut(8));
            }
        }

        content.add(Box.createVerticalStrut(30));
        content.add(lblChapters);
        content.add(Box.createVerticalStrut(12));
        content.add(chapterListPanel);

        // ===== BÌNH LUẬN =====
        JLabel lblComment = new JLabel("💬  Bình luận");
        lblComment.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblComment.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel writePanel = new JPanel(new BorderLayout(10, 5));
        writePanel.setOpaque(false);
        writePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        writePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtComment = new JTextArea(3, 40);
        txtComment.setLineWrap(true);
        txtComment.setWrapStyleWord(true);
        txtComment.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JButton btnSend = new JButton("Gửi");
        btnSend.setPreferredSize(new Dimension(90, 40));
        btnSend.setBackground(new Color(0, 102, 204));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setBorderPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSend.addActionListener(e -> {
            String contentText = txtComment.getText().trim();
            if (contentText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung bình luận!");
                return;
            }
            if (commentDAO.addComment(currentUser.getUserId(), story.getStoryId(), null, contentText)) {
                txtComment.setText("");
                loadComments();
            }
        });

        writePanel.add(new JScrollPane(txtComment), BorderLayout.CENTER);
        writePanel.add(btnSend, BorderLayout.EAST);

        commentListPanel = new JPanel();
        commentListPanel.setLayout(new BoxLayout(commentListPanel, BoxLayout.Y_AXIS));
        commentListPanel.setOpaque(false);
        commentListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(Box.createVerticalStrut(35));
        content.add(lblComment);
        content.add(Box.createVerticalStrut(12));
        content.add(writePanel);
        content.add(Box.createVerticalStrut(15));
        content.add(commentListPanel);

        loadComments();

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(250, 250, 250));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String priceText = chapter.isFree() ? "Miễn phí" : chapter.getPriceCoin() + " Coin";
        JLabel lblInfo = new JLabel(String.format("Chương %.0f: %s   (%s)",
                chapter.getChapterNumber(), chapter.getTitle(), priceText));
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnRead = new JButton("Đọc");
        btnRead.setFocusPainted(false);
        btnRead.setPreferredSize(new Dimension(85, 32));
        btnRead.setBackground(new Color(0, 102, 204));
        btnRead.setForeground(Color.WHITE);
        btnRead.setBorderPainted(false);
        btnRead.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(12, 14, 12, 14)
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
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        String[] stars = {"1 ★", "2 ★", "3 ★", "4 ★", "5 ★"};
        JComboBox<String> cboStar = new JComboBox<>(stars);
        int current = ratingDAO.getUserRating(currentUser.getUserId(), story.getStoryId());
        if (current > 0) cboStar.setSelectedIndex(current - 1);

        JTextArea txtReview = new JTextArea(3, 25);
        txtReview.setLineWrap(true);
        txtReview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(new JLabel("Chọn số sao:"), BorderLayout.NORTH);
        panel.add(cboStar, BorderLayout.CENTER);
        panel.add(new JScrollPane(txtReview), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Đánh giá truyện",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int score = cboStar.getSelectedIndex() + 1;
            if (ratingDAO.addOrUpdateRating(currentUser.getUserId(), story.getStoryId(), score, txtReview.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã đánh giá!");
                new StoryDetailFrame(story.getStoryId()).setVisible(true);
                this.dispose();
            }
        }
    }

    private void showReportDialog(String targetType, int targetId) {
        String[] reasons = {"Nội dung vi phạm", "Spam / Quảng cáo", "Nội dung không phù hợp", "Trùng lặp / Đạo văn", "Lỗi kỹ thuật", "Khác"};
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
            ReportDAO reportDAO = new ReportDAO();
            if (reportDAO.createReport(currentUser.getUserId(), targetType, targetId,
                    (String) cboReason.getSelectedItem(), txtDesc.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Đã gửi báo cáo. Cảm ơn bạn!");
            }
        }
    }
}
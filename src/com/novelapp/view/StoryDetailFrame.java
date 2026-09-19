package com.novelapp.view;

import com.novelapp.dao.*;
import com.novelapp.model.Chapter;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    private JLabel lblCover;

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
        setSize(1050, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(250, 250, 250));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ==================== PHẦN TRÊN: ẢNH + THÔNG TIN ====================
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 25);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;

        // Ảnh bìa
        JPanel coverPanel = new JPanel(new BorderLayout());
        coverPanel.setPreferredSize(new Dimension(180, 270));
        coverPanel.setMinimumSize(new Dimension(180, 270));
        coverPanel.setMaximumSize(new Dimension(180, 270));
        coverPanel.setBackground(new Color(240, 240, 240));
        coverPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        lblCover = new JLabel("Không có ảnh", SwingConstants.CENTER);
        lblCover.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCover.setForeground(Color.GRAY);
        loadCoverImage();
        coverPanel.add(lblCover, BorderLayout.CENTER);

        if (currentUser.hasRole("ADMIN") || currentUser.hasRole("AUTHOR")) {
            JButton btnChangeCover = new JButton("Đổi ảnh bìa");
            btnChangeCover.setFocusPainted(false);
            btnChangeCover.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChangeCover.addActionListener(e -> changeCoverImage());
            coverPanel.add(btnChangeCover, BorderLayout.SOUTH);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        topPanel.add(coverPanel, gbc);

        // Thông tin
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(25, 25, 25));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAuthor = new JLabel("Tác giả: " + (story.getAuthorName() != null ? story.getAuthorName() : "N/A"));
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblAuthor.setForeground(new Color(70, 70, 70));
        lblAuthor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblStatus = new JLabel("Tình trạng: " + story.getStatus());
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thể loại dạng Tag / Badge
        JPanel genreTagPanel = createGenreTagsPanel();

        JLabel lblMeta = new JLabel(String.format(
                "👁 %,d lượt xem   •   ❤️ %,d theo dõi   •   ★ %.1f (%d đánh giá)",
                story.getViewCount(), story.getFollowCount(),
                story.getRatingAvg(), story.getRatingCount()
        ));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMeta.setForeground(new Color(110, 110, 110));
        lblMeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnReadNow = new JButton("📖 Đọc ngay");
        btnReadNow.setPreferredSize(new Dimension(120, 36));
        btnReadNow.setBackground(new Color(0, 153, 76));
        btnReadNow.setForeground(Color.WHITE);
        btnReadNow.setFocusPainted(false);
        btnReadNow.setBorderPainted(false);
        btnReadNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReadNow.addActionListener(e -> readFirstChapter());

        btnFollow = new JButton();
        btnFollow.setPreferredSize(new Dimension(120, 36));
        btnFollow.setFocusPainted(false);
        btnFollow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateFollowButton();

        btnFavorite = new JButton();
        btnFavorite.setPreferredSize(new Dimension(120, 36));
        btnFavorite.setFocusPainted(false);
        btnFavorite.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateFavoriteButton();

        JButton btnRate = new JButton("⭐ Đánh giá");
        btnRate.setPreferredSize(new Dimension(110, 36));
        btnRate.setFocusPainted(false);
        btnRate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRate.addActionListener(e -> showRatingDialog());

        JButton btnShare = new JButton("🔗 Chia sẻ");
        btnShare.setPreferredSize(new Dimension(100, 36));
        btnShare.setFocusPainted(false);
        btnShare.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShare.addActionListener(e -> shareStory());

        JButton btnReport = new JButton("🚩 Báo cáo");
        btnReport.setPreferredSize(new Dimension(100, 36));
        btnReport.setFocusPainted(false);
        btnReport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReport.addActionListener(e -> showReportDialog("STORY", story.getStoryId()));

        JButton btnBack = new JButton("← Quay lại");
        btnBack.setPreferredSize(new Dimension(100, 36));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

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

        actionPanel.add(btnReadNow);
        actionPanel.add(btnFollow);
        actionPanel.add(btnFavorite);
        actionPanel.add(btnRate);
        actionPanel.add(btnShare);
        actionPanel.add(btnReport);
        actionPanel.add(btnBack);

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(lblAuthor);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(lblStatus);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(genreTagPanel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(lblMeta);
        infoPanel.add(Box.createVerticalStrut(18));
        infoPanel.add(actionPanel);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(infoPanel, gbc);

        content.add(topPanel);
        content.add(Box.createVerticalStrut(25));

        // ==================== MÔ TẢ ====================
        JLabel lblDesc = new JLabel("📝  Nội dung truyện");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setPreferredSize(new Dimension(100, 120));
        scrollDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        scrollDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDesc.setBorder(null);

        content.add(lblDesc);
        content.add(Box.createVerticalStrut(10));
        content.add(scrollDesc);
        content.add(Box.createVerticalStrut(25));

        // ==================== DANH SÁCH CHƯƠNG ====================
        JLabel lblChapters = new JLabel("📖  Danh sách chương");
        lblChapters.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblChapters.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chapterListPanel = new JPanel();
        chapterListPanel.setLayout(new BoxLayout(chapterListPanel, BoxLayout.Y_AXIS));
        chapterListPanel.setOpaque(false);
        chapterListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Chapter> chapters = chapterDAO.getChaptersByStoryId(story.getStoryId());
        if (chapters.isEmpty()) {
            JLabel emptyLbl = new JLabel("Chưa có chương nào.");
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyLbl.setForeground(Color.GRAY);
            chapterListPanel.add(emptyLbl);
        } else {
            for (Chapter chapter : chapters) {
                chapterListPanel.add(createChapterRow(chapter));
                chapterListPanel.add(Box.createVerticalStrut(7));
            }
        }

        content.add(lblChapters);
        content.add(Box.createVerticalStrut(12));
        content.add(chapterListPanel);
        content.add(Box.createVerticalStrut(30));

        // ==================== BÌNH LUẬN ====================
        JLabel lblComment = new JLabel("💬  Bình luận");
        lblComment.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblComment.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel writePanel = new JPanel(new BorderLayout(10, 5));
        writePanel.setOpaque(false);
        writePanel.setPreferredSize(new Dimension(100, 80));
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
            String text = txtComment.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung!");
                return;
            }
            if (commentDAO.addComment(currentUser.getUserId(), story.getStoryId(), null, text)) {
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

        content.add(lblComment);
        content.add(Box.createVerticalStrut(10));
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

    private void readFirstChapter() {
        List<Chapter> chapters = chapterDAO.getChaptersByStoryId(story.getStoryId());
        if (chapters == null || chapters.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Truyện chưa có chương nào!");
            return;
        }
        Chapter first = chapters.get(0);
        for (Chapter c : chapters) {
            if (c.getChapterNumber() < first.getChapterNumber()) {
                first = c;
            }
        }
        handleReadChapter(first);
    }

    private void shareStory() {
        String text = "Đọc truyện \"" + story.getTitle() + "\" trên NovelApp!\n"
                + "Tác giả: " + story.getAuthorName();
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(text), null);
        JOptionPane.showMessageDialog(this,
                "Đã copy link/thông tin truyện vào clipboard!\n\n" + text,
                "Chia sẻ", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createGenreTagsPanel() {
        JPanel genrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        genrePanel.setOpaque(false);
        genrePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGenreTitle = new JLabel("Thể loại: ");
        lblGenreTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        genrePanel.add(lblGenreTitle);

        List<Map<String, Object>> genres = storyDAO.getGenreListByStoryId(story.getStoryId());
        if (genres.isEmpty()) {
            JLabel lblNone = new JLabel("Chưa cập nhật");
            lblNone.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            lblNone.setForeground(Color.GRAY);
            genrePanel.add(lblNone);
        } else {
            for (Map<String, Object> g : genres) {
                int genreId = (int) g.get("genreId");
                String genreName = (String) g.get("genreName");

                JButton btnGenreTag = new JButton(genreName);
                btnGenreTag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnGenreTag.setForeground(new Color(0, 102, 204));
                btnGenreTag.setBackground(new Color(230, 242, 255));
                btnGenreTag.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(180, 215, 255), 1, true),
                        new EmptyBorder(3, 8, 3, 8)
                ));
                btnGenreTag.setFocusPainted(false);
                btnGenreTag.setCursor(new Cursor(Cursor.HAND_CURSOR));

                btnGenreTag.addActionListener(e -> {
                    new SearchFrame(genreId, genreName).setVisible(true);
                    this.dispose();
                });

                genrePanel.add(btnGenreTag);
            }
        }
        return genrePanel;
    }

    private void loadCoverImage() {
        String coverUrl = story.getCoverUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(coverUrl);
                Image img = icon.getImage().getScaledInstance(180, 240, Image.SCALE_SMOOTH);
                lblCover.setIcon(new ImageIcon(img));
                lblCover.setText("");
            } catch (Exception e) {
                lblCover.setIcon(null);
                lblCover.setText("Không có ảnh");
            }
        }
    }

    private void changeCoverImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Ảnh (jpg, png, jpeg)", "jpg", "png", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selected = chooser.getSelectedFile();
        try {
            File coversDir = new File("covers");
            if (!coversDir.exists()) coversDir.mkdir();

            String ext = selected.getName().substring(selected.getName().lastIndexOf('.'));
            String newFileName = "cover_" + story.getStoryId() + "_" + System.currentTimeMillis() + ext;
            File dest = new File(coversDir, newFileName);
            Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String path = dest.getAbsolutePath();
            storyDAO.updateCoverUrl(story.getStoryId(), path);
            story.setCoverUrl(path);
            loadCoverImage();
            JOptionPane.showMessageDialog(this, "Đã cập nhật ảnh bìa!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu ảnh: " + e.getMessage());
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
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(new Color(250, 250, 250));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 14, 10, 14)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String priceText = chapter.isFree() ? "Miễn phí" : chapter.getPriceCoin() + " Coin";
        JLabel lblInfo = new JLabel(String.format("Chương %.0f: %s   (%s)",
                chapter.getChapterNumber(), chapter.getTitle(), priceText));
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnRead = new JButton("Đọc");
        btnRead.setPreferredSize(new Dimension(80, 30));
        btnRead.setBackground(new Color(0, 102, 204));
        btnRead.setForeground(Color.WHITE);
        btnRead.setFocusPainted(false);
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
                "Chương này cần " + price + " Coin.\nSố dư: " + balance + " Coin\nMở khóa?",
                "Mở khóa chương", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        if (balance < price) {
            JOptionPane.showMessageDialog(this, "Không đủ Coin!");
            return;
        }

        if (walletDAO.deductCoin(currentUser.getUserId(), price, "UNLOCK",
                "Mở khóa chương " + (int) chapter.getChapterNumber())) {
            accessDAO.grantAccess(currentUser.getUserId(), chapter.getChapterId(), "PURCHASE");
            openReader(chapter.getChapterId());
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
        boolean isReply = c.get("parentId") != null;
        boolean isMine = currentUser.getUserId() == (int) c.get("userId");

        JPanel card = new JPanel(new BorderLayout(8, 4));

        if (isMine) {
            card.setBackground(new Color(232, 245, 233));
        } else if (isReply) {
            card.setBackground(new Color(245, 248, 255));
        } else {
            card.setBackground(new Color(248, 249, 250));
        }

        int leftPad = isReply ? 40 : 12;
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, leftPad, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String nameText = c.get("fullName") + " (@" + c.get("username") + ")";
        if (isMine) nameText += "    •    Bạn";
        if (isReply) nameText = "↳    " + nameText;

        JLabel lblUser = new JLabel(nameText);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        if (isMine) lblUser.setForeground(new Color(0, 120, 60));

        JLabel lblContent = new JLabel("<html>" + c.get("content") + "</html>");
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblTime = new JLabel(c.get("createdAt").toString());
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(Color.GRAY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(lblUser, BorderLayout.WEST);
        top.add(lblTime, BorderLayout.EAST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        // Nút Like
        Object likeCountObj = c.get("likeCount");
        int likeCount = likeCountObj != null ? ((Number) likeCountObj).intValue() : 0;
        JButton btnLike = new JButton("👍 " + likeCount);
        btnLike.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLike.setFocusPainted(false);
        btnLike.setBorderPainted(false);
        btnLike.setContentAreaFilled(false);
        btnLike.setForeground(new Color(0, 102, 204));
        btnLike.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLike.addActionListener(e -> {
            commentDAO.toggleLike(currentUser.getUserId(), (int) c.get("commentId"));
            loadComments();
        });

        // Nút Phản hồi
        JButton btnReply = new JButton("Phản hồi");
        btnReply.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReply.setFocusPainted(false);
        btnReply.setBorderPainted(false);
        btnReply.setContentAreaFilled(false);
        btnReply.setForeground(new Color(0, 102, 204));
        btnReply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReply.addActionListener(e -> showReplyDialog((int) c.get("commentId")));

        // Nút Báo cáo
        JButton btnReportCmt = new JButton("Báo cáo");
        btnReportCmt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReportCmt.setFocusPainted(false);
        btnReportCmt.setBorderPainted(false);
        btnReportCmt.setContentAreaFilled(false);
        btnReportCmt.setForeground(new Color(220, 53, 69));
        btnReportCmt.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReportCmt.addActionListener(e -> showReportDialog("COMMENT", (int) c.get("commentId")));

        actionRow.add(btnLike);
        actionRow.add(btnReply);
        actionRow.add(btnReportCmt);

        // Nút Xóa (chỉ xuất hiện nếu là bình luận của chính người dùng hiện tại)
        if (currentUser.getUserId() == (int) c.get("userId")) {
            JButton btnDelete = new JButton("Xóa");
            btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnDelete.setFocusPainted(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setForeground(new Color(220, 53, 69));
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "Xóa bình luận?", "Xác nhận",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (commentDAO.deleteOwnComment(currentUser.getUserId(), (int) c.get("commentId"))) {
                        loadComments();
                    }
                }
            });
            actionRow.add(btnDelete);
        }

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(lblContent, BorderLayout.CENTER);
        center.add(actionRow, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private void showReplyDialog(int parentCommentId) {
        JTextArea txtReply = new JTextArea(4, 30);
        txtReply.setLineWrap(true);
        txtReply.setWrapStyleWord(true);
        txtReply.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.add(new JLabel("Nội dung phản hồi:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(txtReply), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Phản hồi bình luận",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String content = txtReply.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung!");
                return;
            }
            if (commentDAO.replyComment(currentUser.getUserId(), story.getStoryId(), parentCommentId, content)) {
                JOptionPane.showMessageDialog(this, "Đã gửi phản hồi!");
                loadComments();
            } else {
                JOptionPane.showMessageDialog(this, "Gửi thất bại!");
            }
        }
    }

    private void showRatingDialog() {
        String[] stars = {"1 ★", "2 ★", "3 ★", "4 ★", "5 ★"};
        JComboBox<String> cbo = new JComboBox<>(stars);
        int cur = ratingDAO.getUserRating(currentUser.getUserId(), story.getStoryId());
        if (cur > 0) cbo.setSelectedIndex(cur - 1);

        JTextArea review = new JTextArea(3, 25);
        review.setLineWrap(true);

        JPanel p = new JPanel(new BorderLayout(5, 8));
        p.add(new JLabel("Chọn số sao:"), BorderLayout.NORTH);
        p.add(cbo, BorderLayout.CENTER);
        p.add(new JScrollPane(review), BorderLayout.SOUTH);

        if (JOptionPane.showConfirmDialog(this, p, "Đánh giá", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ratingDAO.addOrUpdateRating(currentUser.getUserId(), story.getStoryId(),
                    cbo.getSelectedIndex() + 1, review.getText().trim());
            JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã đánh giá!");
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            this.dispose();
        }
    }

    private void showReportDialog(String type, int id) {
        String[] reasons = {"Nội dung vi phạm", "Spam", "Không phù hợp", "Đạo văn", "Lỗi kỹ thuật", "Khác"};
        JComboBox<String> cbo = new JComboBox<>(reasons);
        JTextArea desc = new JTextArea(3, 25);
        desc.setLineWrap(true);

        JPanel p = new JPanel(new BorderLayout(5, 8));
        p.add(cbo, BorderLayout.NORTH);
        p.add(new JScrollPane(desc), BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(this, p, "Báo cáo", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new ReportDAO().createReport(currentUser.getUserId(), type, id,
                    (String) cbo.getSelectedItem(), desc.getText().trim());
            JOptionPane.showMessageDialog(this, "Đã gửi báo cáo!");
        }
    }
}
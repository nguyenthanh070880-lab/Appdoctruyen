package com.novelapp.view;

import com.novelapp.dao.ChapterDAO;
import com.novelapp.dao.ChapterAccessDAO;
import com.novelapp.dao.WalletDAO;
import com.novelapp.model.Chapter;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import com.novelapp.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReaderFrame extends JFrame {

    private Chapter chapter;
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private JTextArea txtContent;
    private JLabel lblTitle;
    private JButton btnPrev;
    private JButton btnNext;
    private float fontSize = 16f;
    private final User currentUser;

    public ReaderFrame(int chapterId) {
        this.currentUser = SessionManager.getCurrentUser();
        this.chapter = chapterDAO.findById(chapterId);

        if (chapter == null) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy chương!");
            this.dispose();
            return;
        }

        // Tăng lượt xem + lưu lịch sử đọc
        chapterDAO.increaseViewCount(chapterId);
        saveReadingHistory();

        initComponents();
        updateNavigationButtons();
    }

    private void initComponents() {
        setTitle("Đọc truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 720);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Color.WHITE);

        // ===== Header =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        lblTitle = new JLabel();
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        updateTitle();

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlPanel.setOpaque(false);

        JButton btnZoomIn = new JButton("A+");
        JButton btnZoomOut = new JButton("A-");
        JButton btnBack = new JButton("Quay lại");

        btnZoomIn.setFocusPainted(false);
        btnZoomOut.setFocusPainted(false);
        btnBack.setFocusPainted(false);

        btnZoomIn.addActionListener(e -> changeFontSize(2f));
        btnZoomOut.addActionListener(e -> changeFontSize(-2f));
        btnBack.addActionListener(e -> {
            new StoryDetailFrame(chapter.getStoryId()).setVisible(true);
            this.dispose();
        });

        controlPanel.add(btnZoomOut);
        controlPanel.add(btnZoomIn);
        controlPanel.add(btnBack);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);

        // ===== Nội dung =====
        txtContent = new JTextArea();
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, (int) fontSize));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        txtContent.setBackground(new Color(252, 252, 252));
        updateContent();

        JScrollPane scrollPane = new JScrollPane(txtContent);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        // ===== Footer =====
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 8));
        footerPanel.setOpaque(false);

        btnPrev = new JButton("← Chương trước");
        btnNext = new JButton("Chương sau →");
        btnPrev.setFocusPainted(false);
        btnNext.setFocusPainted(false);
        btnPrev.setPreferredSize(new Dimension(140, 36));
        btnNext.setPreferredSize(new Dimension(140, 36));

        btnPrev.addActionListener(e -> goToPreviousChapter());
        btnNext.addActionListener(e -> goToNextChapter());

        footerPanel.add(btnPrev);
        footerPanel.add(btnNext);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void updateTitle() {
        lblTitle.setText("Chương " + (int) chapter.getChapterNumber() + ": " + chapter.getTitle());
    }

    private void updateContent() {
        txtContent.setText(chapter.getContent() != null ? chapter.getContent() : "Chưa có nội dung.");
        txtContent.setCaretPosition(0); // cuộn lên đầu
    }

    private void updateNavigationButtons() {
        Chapter prev = chapterDAO.getPreviousChapter(chapter.getStoryId(), chapter.getChapterNumber());
        Chapter next = chapterDAO.getNextChapter(chapter.getStoryId(), chapter.getChapterNumber());

        btnPrev.setEnabled(prev != null);
        btnNext.setEnabled(next != null);
    }

    private void goToPreviousChapter() {
        Chapter prev = chapterDAO.getPreviousChapter(chapter.getStoryId(), chapter.getChapterNumber());
        if (prev != null) {
            handleChapterAccess(prev);
        }
    }

    private void goToNextChapter() {
        Chapter next = chapterDAO.getNextChapter(chapter.getStoryId(), chapter.getChapterNumber());
        if (next != null) {
            handleChapterAccess(next);
        }
    }

    // Kiểm tra quyền trước khi chuyển chương
    private void handleChapterAccess(Chapter targetChapter) {
        // Chương miễn phí → cho đọc luôn
        if (targetChapter.isFree()) {
            loadChapter(targetChapter);
            return;
        }

        ChapterAccessDAO accessDAO = new ChapterAccessDAO();
        WalletDAO walletDAO = new WalletDAO();

        // Đã mở khóa rồi
        if (accessDAO.hasAccess(currentUser.getUserId(), targetChapter.getChapterId())) {
            loadChapter(targetChapter);
            return;
        }

        // Chưa mở khóa
        int price = targetChapter.getPriceCoin();
        long balance = walletDAO.getBalance(currentUser.getUserId());

        int choice = JOptionPane.showConfirmDialog(this,
                "Chương này cần " + price + " Coin để mở khóa.\n"
                + "Số dư hiện tại: " + balance + " Coin\n\n"
                + "Bạn có muốn mở khóa không?",
                "Mở khóa chương",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        if (balance < price) {
            JOptionPane.showMessageDialog(this,
                    "Bạn không đủ Coin!\nVui lòng nạp thêm.",
                    "Không đủ tiền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = walletDAO.deductCoin(
                currentUser.getUserId(),
                price,
                "UNLOCK",
                "Mở khóa chương " + (int) targetChapter.getChapterNumber() + ": " + targetChapter.getTitle()
        );

        if (success) {
            accessDAO.grantAccess(currentUser.getUserId(), targetChapter.getChapterId(), "PURCHASE");
            JOptionPane.showMessageDialog(this, "Mở khóa thành công!");
            loadChapter(targetChapter);
        } else {
            JOptionPane.showMessageDialog(this, "Mở khóa thất bại, vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChapter(Chapter newChapter) {
        this.chapter = newChapter;
        chapterDAO.increaseViewCount(newChapter.getChapterId());
        saveReadingHistory();
        updateTitle();
        updateContent();
        updateNavigationButtons();
    }

    private void changeFontSize(float delta) {
        fontSize += delta;
        if (fontSize < 12f) fontSize = 12f;
        if (fontSize > 28f) fontSize = 28f;
        txtContent.setFont(txtContent.getFont().deriveFont(fontSize));
    }

    // Lưu lịch sử đọc
    private void saveReadingHistory() {
        if (currentUser == null) return;

        String sql = "MERGE reading_history AS target "
                   + "USING (SELECT ? AS user_id, ? AS story_id, ? AS chapter_id) AS source "
                   + "ON target.user_id = source.user_id AND target.story_id = source.story_id "
                   + "WHEN MATCHED THEN "
                   + "  UPDATE SET chapter_id = source.chapter_id, last_read_at = GETDATE() "
                   + "WHEN NOT MATCHED THEN "
                   + "  INSERT (user_id, story_id, chapter_id, last_read_at) "
                   + "  VALUES (source.user_id, source.story_id, source.chapter_id, GETDATE());";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentUser.getUserId());
            ps.setInt(2, chapter.getStoryId());
            ps.setInt(3, chapter.getChapterId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

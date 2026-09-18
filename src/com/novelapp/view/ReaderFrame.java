package com.novelapp.view;

import com.novelapp.dao.ChapterAccessDAO;
import com.novelapp.dao.ChapterDAO;
import com.novelapp.dao.WalletDAO;
import com.novelapp.dao.ReportDAO;
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
    private float fontSize = 17f;
    private final User currentUser;

    public ReaderFrame(int chapterId) {
        this.currentUser = SessionManager.getCurrentUser();
        this.chapter = chapterDAO.findById(chapterId);

        if (chapter == null) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy chương!");
            this.dispose();
            return;
        }

        chapterDAO.increaseViewCount(chapterId);
        saveReadingHistory();
        initComponents();
        updateNavigationButtons();
    }

    private void initComponents() {
        setTitle("Đọc truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 740);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(250, 250, 250));

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        lblTitle = new JLabel();
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        updateTitle();

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlPanel.setOpaque(false);

        JButton btnZoomOut = createHeaderButton("A-");
        JButton btnZoomIn = createHeaderButton("A+");
        JButton btnBack = createHeaderButton("Quay lại");

        btnZoomOut.addActionListener(e -> changeFontSize(-1.5f));
        btnZoomIn.addActionListener(e -> changeFontSize(1.5f));
        btnBack.addActionListener(e -> {
            new StoryDetailFrame(chapter.getStoryId()).setVisible(true);
            this.dispose();
        });

        controlPanel.add(btnZoomOut);
        controlPanel.add(btnZoomIn);
        controlPanel.add(btnBack);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);

        // ===== NỘI DUNG =====
        txtContent = new JTextArea();
        txtContent.setFont(new Font("Segoe UI", Font.PLAIN, (int) fontSize));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setBorder(new EmptyBorder(25, 40, 25, 40));
        txtContent.setBackground(Color.WHITE);
        updateContent();

        JScrollPane scrollPane = new JScrollPane(txtContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(22);

        // ===== FOOTER =====
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 12));
        footerPanel.setBackground(new Color(245, 247, 250));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        btnPrev = new JButton("← Chương trước");
        btnNext = new JButton("Chương sau →");
        styleNavButton(btnPrev);
        styleNavButton(btnNext);

        btnPrev.addActionListener(e -> goToPreviousChapter());
        btnNext.addActionListener(e -> goToNextChapter());

        footerPanel.add(btnPrev);
        footerPanel.add(btnNext);

        JButton btnReportChapter = new JButton("🚩 Báo cáo chương");
        btnReportChapter.setFocusPainted(false);
        btnReportChapter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReportChapter.addActionListener(e -> showReportChapterDialog());
        footerPanel.add(btnReportChapter);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(255, 255, 255, 30));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 80)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleNavButton(JButton btn) {
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
    }

    private void updateTitle() {
        lblTitle.setText("Chương " + (int) chapter.getChapterNumber() + ": " + chapter.getTitle());
    }

    private void updateContent() {
        txtContent.setText(chapter.getContent() != null ? chapter.getContent() : "Chưa có nội dung.");
        txtContent.setCaretPosition(0);
    }

    private void updateNavigationButtons() {
        Chapter prev = chapterDAO.getPreviousChapter(chapter.getStoryId(), chapter.getChapterNumber());
        Chapter next = chapterDAO.getNextChapter(chapter.getStoryId(), chapter.getChapterNumber());
        btnPrev.setEnabled(prev != null);
        btnNext.setEnabled(next != null);
    }

    private void goToPreviousChapter() {
        Chapter prev = chapterDAO.getPreviousChapter(chapter.getStoryId(), chapter.getChapterNumber());
        if (prev != null) handleChapterAccess(prev);
    }

    private void goToNextChapter() {
        Chapter next = chapterDAO.getNextChapter(chapter.getStoryId(), chapter.getChapterNumber());
        if (next != null) handleChapterAccess(next);
    }

    private void handleChapterAccess(Chapter targetChapter) {
        if (targetChapter.isFree()) {
            loadChapter(targetChapter);
            return;
        }

        ChapterAccessDAO accessDAO = new ChapterAccessDAO();
        WalletDAO walletDAO = new WalletDAO();

        if (accessDAO.hasAccess(currentUser.getUserId(), targetChapter.getChapterId())) {
            loadChapter(targetChapter);
            return;
        }

        int price = targetChapter.getPriceCoin();
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
                "Mở khóa chương " + (int) targetChapter.getChapterNumber() + ": " + targetChapter.getTitle());

        if (success) {
            accessDAO.grantAccess(currentUser.getUserId(), targetChapter.getChapterId(), "PURCHASE");
            JOptionPane.showMessageDialog(this, "Mở khóa thành công!");
            loadChapter(targetChapter);
        } else {
            JOptionPane.showMessageDialog(this, "Mở khóa thất bại!");
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
        if (fontSize < 13f) fontSize = 13f;
        if (fontSize > 28f) fontSize = 28f;
        txtContent.setFont(txtContent.getFont().deriveFont(fontSize));
    }

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

    private void showReportChapterDialog() {
        String[] reasons = {
            "Nội dung vi phạm",
            "Spam / Quảng cáo",
            "Nội dung không phù hợp",
            "Lỗi kỹ thuật",
            "Trùng lặp",
            "Khác"
        };

        JComboBox<String> cbo = new JComboBox<>(reasons);

        JTextArea desc = new JTextArea(4, 25);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.add(new JLabel("Lý do báo cáo chương:"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(cbo, BorderLayout.NORTH);
        center.add(new JScrollPane(desc), BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Báo cáo chương",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            ReportDAO reportDAO = new ReportDAO();

            boolean ok = reportDAO.createReport(
                    currentUser.getUserId(),
                    "CHAPTER",
                    chapter.getChapterId(),
                    (String) cbo.getSelectedItem(),
                    desc.getText().trim()
            );

            if (ok) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã gửi báo cáo chương. Cảm ơn bạn!"
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Gửi báo cáo thất bại!"
                );
            }
        }
    }
}
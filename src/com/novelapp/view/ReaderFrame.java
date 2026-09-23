package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.ChapterAccessDAO;
import com.novelapp.dao.ChapterDAO;
import com.novelapp.dao.WalletDAO;
import com.novelapp.model.Chapter;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ReaderFrame extends JFrame {

    private Chapter chapter;
    private final ChapterDAO chapterDAO = new ChapterDAO();
    private JTextArea txtContent;
    private JLabel lblTitle;
    private JButton btnPrev;
    private JButton btnNext;
    private JScrollPane scrollPane;
    private float fontSize = 17f;
    private boolean darkMode = false;
    private final User currentUser;

    // ===== BIẾN BỔ SUNG CHO TÍNH NĂNG PHÂN TRANG =====
    private String fullContent = "";
    private int currentPage = 0;
    private int charsPerPage = 1200; // Số ký tự mỗi trang
    private boolean pageMode = true; // true = Phân trang, false = Cuộn hết
    private JLabel lblPageInfo;
    private JButton btnPagePrev;
    private JButton btnPageNext;

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
        restoreScrollPosition();
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
        JButton btnTheme = createHeaderButton("🌙 Tối");
        JButton btnTogglePage = createHeaderButton("📄 Phân trang");
        JButton btnReport = createHeaderButton("🚩 Báo cáo");
        JButton btnBack = createHeaderButton("Quay lại");

        btnZoomOut.addActionListener(e -> changeFontSize(-1.5f));
        btnZoomIn.addActionListener(e -> changeFontSize(1.5f));
        btnTheme.addActionListener(e -> toggleTheme(btnTheme));
        btnTogglePage.addActionListener(e -> togglePageMode(btnTogglePage));
        btnReport.addActionListener(e -> showReportChapterDialog());
        btnBack.addActionListener(e -> {
            saveScrollPosition();
            new StoryDetailFrame(chapter.getStoryId()).setVisible(true);
            this.dispose();
        });

        controlPanel.add(btnZoomOut);
        controlPanel.add(btnZoomIn);
        controlPanel.add(btnTheme);
        controlPanel.add(btnTogglePage);
        controlPanel.add(btnReport);
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

        scrollPane = new JScrollPane(txtContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(22);

        // Nạp và hiển thị nội dung chương
        updateContent();

        // ===== FOOTER =====
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        footerPanel.setBackground(new Color(245, 247, 250));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        btnPrev = new JButton("← Chương trước");
        btnNext = new JButton("Chương sau →");
        styleNavButton(btnPrev);
        styleNavButton(btnNext);

        btnPrev.addActionListener(e -> {
            saveScrollPosition();
            goToPreviousChapter();
        });
        btnNext.addActionListener(e -> {
            saveScrollPosition();
            goToNextChapter();
        });

        // Điều khiển phân trang
        btnPagePrev = new JButton("◀ Trang");
        btnPageNext = new JButton("Trang ▶");
        styleNavButton(btnPagePrev);
        styleNavButton(btnPageNext);
        btnPagePrev.setPreferredSize(new Dimension(100, 35));
        btnPageNext.setPreferredSize(new Dimension(100, 35));

        btnPagePrev.addActionListener(e -> goPrevPage());
        btnPageNext.addActionListener(e -> goNextPage());

        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        footerPanel.add(btnPrev);
        footerPanel.add(btnPagePrev);
        footerPanel.add(lblPageInfo);
        footerPanel.add(btnPageNext);
        footerPanel.add(btnNext);

        // Cập nhật trạng thái hiển thị của các nút phân trang ban đầu
        showPage();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(new Color(0, 90, 180));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 255)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(0, 120, 210));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(0, 90, 180));
            }
        });
        return btn;
    }

    private void styleNavButton(JButton btn) {
        btn.setPreferredSize(new Dimension(140, 38));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void toggleTheme(JButton btnTheme) {
        darkMode = !darkMode;
        if (darkMode) {
            txtContent.setBackground(new Color(30, 30, 30));
            txtContent.setForeground(new Color(220, 220, 220));
            scrollPane.getViewport().setBackground(new Color(30, 30, 30));
            btnTheme.setText("☀️ Sáng");
        } else {
            txtContent.setBackground(Color.WHITE);
            txtContent.setForeground(Color.BLACK);
            scrollPane.getViewport().setBackground(Color.WHITE);
            btnTheme.setText("🌙 Tối");
        }
    }

    private void togglePageMode(JButton btnToggle) {
        pageMode = !pageMode;
        if (pageMode) {
            btnToggle.setText("📄 Phân trang");
            btnPagePrev.setVisible(true);
            btnPageNext.setVisible(true);
            lblPageInfo.setVisible(true);
            showPage();
        } else {
            btnToggle.setText("📜 Cuộn hết");
            btnPagePrev.setVisible(false);
            btnPageNext.setVisible(false);
            lblPageInfo.setVisible(false);
            txtContent.setText(fullContent);
            txtContent.setCaretPosition(0);
        }
    }

    private void updateTitle() {
        lblTitle.setText("Chương " + (int) chapter.getChapterNumber() + ": " + chapter.getTitle());
    }

    private void updateContent() {
        fullContent = chapter.getContent() != null ? chapter.getContent() : "Chưa có nội dung.";
        currentPage = 0;
        if (pageMode) {
            showPage();
        } else {
            txtContent.setText(fullContent);
            txtContent.setCaretPosition(0);
        }
    }

    // ===== HÀM XỬ LÝ PHÂN TRANG =====
    private int getTotalPages() {
        if (fullContent == null || fullContent.isEmpty()) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil((double) fullContent.length() / charsPerPage));
    }

    private void showPage() {
        if (!pageMode) {
            return;
        }

        int total = getTotalPages();
        if (currentPage < 0) {
            currentPage = 0;
        }
        if (currentPage >= total) {
            currentPage = total - 1;
        }

        int start = currentPage * charsPerPage;
        int end = Math.min(start + charsPerPage, fullContent.length());

        // Cắt ở vị trí khoảng trắng gần nhất để không bị ngắt giữa chừng 1 từ
        if (end < fullContent.length()) {
            int space = fullContent.lastIndexOf(' ', end);
            if (space > start + charsPerPage / 2) {
                end = space;
            }
        }

        txtContent.setText(fullContent.substring(start, end).trim());
        txtContent.setCaretPosition(0);

        if (lblPageInfo != null) {
            lblPageInfo.setText("Trang " + (currentPage + 1) + " / " + total);
        }
        if (btnPagePrev != null) {
            btnPagePrev.setEnabled(currentPage > 0);
        }
        if (btnPageNext != null) {
            btnPageNext.setEnabled(currentPage < total - 1);
        }
    }

    private void goPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            showPage();
        }
    }

    private void goNextPage() {
        if (currentPage < getTotalPages() - 1) {
            currentPage++;
            showPage();
        }
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
                "Chương này cần " + price + " Coin.\nSố dư: " + balance + " Coin\nMở khóa?",
                "Mở khóa chương", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (balance < price) {
            JOptionPane.showMessageDialog(this, "Không đủ Coin!");
            return;
        }
        if (walletDAO.deductCoin(currentUser.getUserId(), price, "UNLOCK",
                "Mở khóa chương " + (int) targetChapter.getChapterNumber())) {
            accessDAO.grantAccess(currentUser.getUserId(), targetChapter.getChapterId(), "PURCHASE");
            loadChapter(targetChapter);
        }
    }

    private void loadChapter(Chapter newChapter) {
        this.chapter = newChapter;
        chapterDAO.increaseViewCount(newChapter.getChapterId());
        saveReadingHistory();
        updateTitle();
        updateContent();
        updateNavigationButtons();
        restoreScrollPosition();
    }

    private void changeFontSize(float delta) {
        fontSize += delta;
        if (fontSize < 13f) {
            fontSize = 13f;
        }
        if (fontSize > 28f) {
            fontSize = 28f;
        }
        txtContent.setFont(txtContent.getFont().deriveFont(fontSize));
    }

    private void saveReadingHistory() {
        if (currentUser == null) {
            return;
        }
        String sql = "MERGE reading_history AS target "
                + "USING (SELECT ? AS user_id, ? AS story_id, ? AS chapter_id) AS source "
                + "ON target.user_id = source.user_id AND target.story_id = source.story_id "
                + "WHEN MATCHED THEN "
                + "  UPDATE SET chapter_id = source.chapter_id, last_read_at = GETDATE() "
                + "WHEN NOT MATCHED THEN "
                + "  INSERT (user_id, story_id, chapter_id, last_read_at) "
                + "  VALUES (source.user_id, source.story_id, source.chapter_id, GETDATE());";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            ps.setInt(2, chapter.getStoryId());
            ps.setInt(3, chapter.getChapterId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveScrollPosition() {
        if (currentUser == null || chapter == null || scrollPane == null) {
            return;
        }
        int pos = scrollPane.getVerticalScrollBar().getValue();
        String sql = "UPDATE reading_history SET scroll_position = ?, last_read_at = GETDATE() "
                + "WHERE user_id = ? AND story_id = ? AND chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pos);
            ps.setInt(2, currentUser.getUserId());
            ps.setInt(3, chapter.getStoryId());
            ps.setInt(4, chapter.getChapterId());
            ps.executeUpdate();
        } catch (SQLException e) {
            // bỏ qua nếu chưa có cột
        }
    }

    private void restoreScrollPosition() {
        if (currentUser == null || chapter == null || scrollPane == null) {
            return;
        }
        String sql = "SELECT scroll_position FROM reading_history "
                + "WHERE user_id = ? AND story_id = ? AND chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            ps.setInt(2, chapter.getStoryId());
            ps.setInt(3, chapter.getChapterId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int pos = rs.getInt("scroll_position");
                    SwingUtilities.invokeLater(()
                            -> scrollPane.getVerticalScrollBar().setValue(pos));
                }
            }
        } catch (SQLException e) {
            // bỏ qua
        }
    }

    private void showReportChapterDialog() {
        String[] reasons = {"Nội dung vi phạm", "Spam", "Không phù hợp", "Lỗi kỹ thuật", "Khác"};
        JComboBox<String> cbo = new JComboBox<>(reasons);
        JTextArea desc = new JTextArea(4, 25);
        desc.setLineWrap(true);
        JPanel panel = new JPanel(new BorderLayout(5, 8));
        panel.add(cbo, BorderLayout.NORTH);
        panel.add(new JScrollPane(desc), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, "Báo cáo chương",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            new com.novelapp.dao.ReportDAO().createReport(
                    currentUser.getUserId(), "CHAPTER", chapter.getChapterId(),
                    (String) cbo.getSelectedItem(), desc.getText().trim());
            JOptionPane.showMessageDialog(this, "Đã gửi báo cáo!");
        }
    }
}

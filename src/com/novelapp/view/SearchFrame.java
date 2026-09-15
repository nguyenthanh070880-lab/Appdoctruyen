package com.novelapp.view;

import com.novelapp.dao.StoryDAO;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;
import com.novelapp.config.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchFrame extends JFrame {

    private final User currentUser;
    private JTextField txtKeyword;
    private JComboBox<String> cboStatus;
    private JComboBox<String> cboPaid;
    private JComboBox<String> cboSort;
    private JPanel resultPanel;

    public SearchFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        doSearch();
    }

    private void initComponents() {
        setTitle("Khám phá truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(250, 250, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("🔍  Khám phá truyện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnBack = new JButton("← Trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });
        headerPanel.add(btnBack, BorderLayout.EAST);

        // Filter
        JPanel filterPanel = new JPanel(new BorderLayout(10, 12));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JPanel searchRow = new JPanel(new BorderLayout(12, 0));
        searchRow.setOpaque(false);
        txtKeyword = new JTextField();
        txtKeyword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtKeyword.setPreferredSize(new Dimension(0, 40));
        txtKeyword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setPreferredSize(new Dimension(120, 40));
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.addActionListener(e -> doSearch());
        txtKeyword.addActionListener(e -> doSearch());

        searchRow.add(txtKeyword, BorderLayout.CENTER);
        searchRow.add(btnSearch, BorderLayout.EAST);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterRow.setOpaque(false);

        filterRow.add(new JLabel("Trạng thái:"));
        cboStatus = new JComboBox<>(new String[]{"Tất cả", "ONGOING", "COMPLETED", "DROPPED"});
        filterRow.add(cboStatus);

        filterRow.add(new JLabel("Loại:"));
        cboPaid = new JComboBox<>(new String[]{"Tất cả", "Miễn phí", "Trả phí"});
        filterRow.add(cboPaid);

        filterRow.add(new JLabel("Sắp xếp:"));
        cboSort = new JComboBox<>(new String[]{"Mới cập nhật", "Lượt xem cao", "Đánh giá cao"});
        filterRow.add(cboSort);

        JButton btnFilter = new JButton("Áp dụng");
        btnFilter.setFocusPainted(false);
        btnFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFilter.addActionListener(e -> doSearch());
        filterRow.add(btnFilter);

        filterPanel.add(searchRow, BorderLayout.NORTH);
        filterPanel.add(filterRow, BorderLayout.SOUTH);

        // Results
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(filterPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        // Fix layout
        JPanel center = new JPanel(new BorderLayout());
        center.add(filterPanel, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(center, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void doSearch() {
        resultPanel.removeAll();
        String keyword = txtKeyword.getText().trim();
        String status = (String) cboStatus.getSelectedItem();
        String paid = (String) cboPaid.getSelectedItem();
        String sort = (String) cboSort.getSelectedItem();

        List<Story> stories = searchWithFilter(keyword, status, paid, sort);

        if (stories.isEmpty()) {
            JLabel msg = new JLabel("Không tìm thấy truyện nào phù hợp.");
            msg.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            msg.setForeground(Color.GRAY);
            resultPanel.add(msg);
        } else {
            JLabel lbl = new JLabel("Tìm thấy " + stories.size() + " kết quả");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultPanel.add(lbl);
            resultPanel.add(Box.createVerticalStrut(15));

            for (Story story : stories) {
                resultPanel.add(createStoryCard(story));
                resultPanel.add(Box.createVerticalStrut(12));
            }
        }
        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private List<Story> searchWithFilter(String keyword, String status, String paid, String sort) {
        List<Story> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP 50 s.*, u.full_name AS author_name FROM stories s " +
            "JOIN users u ON s.author_id = u.user_id " +
            "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 "
        );
        List<Object> params = new ArrayList<>();

        if (!keyword.isEmpty()) {
            sql.append("AND (s.title LIKE ? OR u.full_name LIKE ? OR s.description LIKE ?) ");
            String key = "%" + keyword + "%";
            params.add(key); params.add(key); params.add(key);
        }
        if (!"Tất cả".equals(status)) {
            sql.append("AND s.status = ? ");
            params.add(status);
        }
        if ("Miễn phí".equals(paid)) sql.append("AND s.is_paid = 0 ");
        else if ("Trả phí".equals(paid)) sql.append("AND s.is_paid = 1 ");

        switch (sort) {
            case "Lượt xem cao": sql.append("ORDER BY s.view_count DESC"); break;
            case "Đánh giá cao": sql.append("ORDER BY s.rating_avg DESC"); break;
            default: sql.append("ORDER BY s.updated_at DESC");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Story s = new Story();
                    s.setStoryId(rs.getInt("story_id"));
                    s.setAuthorName(rs.getString("author_name"));
                    s.setTitle(rs.getString("title"));
                    s.setStatus(rs.getString("status"));
                    s.setViewCount(rs.getLong("view_count"));
                    s.setRatingAvg(rs.getDouble("rating_avg"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private JPanel createStoryCard(Story story) {
        JPanel card = new JPanel(new BorderLayout(15, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lblAuthor = new JLabel("Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAuthor.setForeground(Color.GRAY);
        JLabel lblMeta = new JLabel(String.format("👁 %,d  |  %s  |  ★ %.1f",
                story.getViewCount(), story.getStatus(), story.getRatingAvg()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(new Color(130, 130, 130));

        info.add(lblTitle);
        info.add(Box.createVerticalStrut(4));
        info.add(lblAuthor);
        info.add(Box.createVerticalStrut(4));
        info.add(lblMeta);

        JButton btn = new JButton("Xem chi tiết");
        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            this.dispose();
        });

        card.add(info, BorderLayout.CENTER);
        card.add(btn, BorderLayout.EAST);
        return card;
    }
}
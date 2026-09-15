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
    private final StoryDAO storyDAO = new StoryDAO();
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
        doSearch(); // load mặc định
    }

    private void initComponents() {
        setTitle("Khám phá truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // ===== Thanh tìm kiếm + bộ lọc =====
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setOpaque(false);

        // Dòng 1: ô tìm kiếm
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        txtKeyword = new JTextField();
        txtKeyword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtKeyword.setPreferredSize(new Dimension(0, 38));
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setFocusPainted(false);
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(120, 38));
        btnSearch.addActionListener(e -> doSearch());
        txtKeyword.addActionListener(e -> doSearch());
        searchRow.add(txtKeyword, BorderLayout.CENTER);
        searchRow.add(btnSearch, BorderLayout.EAST);

        // Dòng 2: bộ lọc
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

        JButton btnFilter = new JButton("Lọc");
        btnFilter.setFocusPainted(false);
        btnFilter.addActionListener(e -> doSearch());
        filterRow.add(btnFilter);

        filterPanel.add(searchRow, BorderLayout.NORTH);
        filterPanel.add(filterRow, BorderLayout.SOUTH);

        // ===== Kết quả =====
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(resultPanel);
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

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
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
            JLabel lblResult = new JLabel("Tìm thấy " + stories.size() + " kết quả:");
            lblResult.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblResult.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultPanel.add(lblResult);
            resultPanel.add(Box.createVerticalStrut(12));

            for (Story story : stories) {
                resultPanel.add(createStoryCard(story));
                resultPanel.add(Box.createVerticalStrut(10));
            }
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private List<Story> searchWithFilter(String keyword, String status, String paid, String sort) {
        List<Story> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT TOP 50 s.*, u.full_name AS author_name " +
            "FROM stories s " +
            "JOIN users u ON s.author_id = u.user_id " +
            "WHERE s.moderation_status = 'APPROVED' AND s.is_deleted = 0 "
        );

        List<Object> params = new ArrayList<>();

        if (!keyword.isEmpty()) {
            sql.append("AND (s.title LIKE ? OR u.full_name LIKE ? OR s.description LIKE ?) ");
            String key = "%" + keyword + "%";
            params.add(key);
            params.add(key);
            params.add(key);
        }

        if (!"Tất cả".equals(status)) {
            sql.append("AND s.status = ? ");
            params.add(status);
        }

        if ("Miễn phí".equals(paid)) {
            sql.append("AND s.is_paid = 0 ");
        } else if ("Trả phí".equals(paid)) {
            sql.append("AND s.is_paid = 1 ");
        }

        switch (sort) {
            case "Lượt xem cao":
                sql.append("ORDER BY s.view_count DESC");
                break;
            case "Đánh giá cao":
                sql.append("ORDER BY s.rating_avg DESC");
                break;
            default:
                sql.append("ORDER BY s.updated_at DESC");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStory(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Story mapStory(ResultSet rs) throws SQLException {
        Story story = new Story();
        story.setStoryId(rs.getInt("story_id"));
        story.setAuthorId(rs.getInt("author_id"));
        story.setAuthorName(rs.getString("author_name"));
        story.setTitle(rs.getString("title"));
        story.setDescription(rs.getString("description"));
        story.setStatus(rs.getString("status"));
        story.setPaid(rs.getBoolean("is_paid"));
        story.setViewCount(rs.getLong("view_count"));
        story.setRatingAvg(rs.getDouble("rating_avg"));
        story.setRatingCount(rs.getInt("rating_count"));
        return story;
    }

    private JPanel createStoryCard(Story story) {
        JPanel card = new JPanel(new BorderLayout(15, 5));
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(story.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblAuthor = new JLabel("Tác giả: " + story.getAuthorName());
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAuthor.setForeground(Color.DARK_GRAY);

        JLabel lblMeta = new JLabel(String.format("Lượt xem: %,d  |  %s  |  %.1f★",
                story.getViewCount(), story.getStatus(), story.getRatingAvg()));
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMeta.setForeground(Color.GRAY);

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblAuthor);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(lblMeta);

        JButton btnDetail = new JButton("Xem chi tiết");
        btnDetail.setFocusPainted(false);
        btnDetail.addActionListener(e -> {
            new StoryDetailFrame(story.getStoryId()).setVisible(true);
            this.dispose();
        });

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnDetail, BorderLayout.EAST);
        return card;
    }
}
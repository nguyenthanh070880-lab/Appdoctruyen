package com.novelapp.view;

import com.novelapp.dao.StoryDAO;
import com.novelapp.model.Story;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFrame extends JFrame {

    private final User currentUser;
    private final StoryDAO storyDAO;

    private JTextField txtKeyword;
    private JComboBox<String> cboStatus;
    private JComboBox<String> cboPaid;
    private JComboBox<String> cboSort;
    private JComboBox<String> cboGenre;
    private JPanel resultPanel;

    private final Map<String, Integer> genreMap = new HashMap<>();

    public SearchFrame() {
        this(-1, null);
    }

    public SearchFrame(int genreId, String genreName) {
        this.currentUser = SessionManager.getCurrentUser();
        this.storyDAO = new StoryDAO();

        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }

        initComponents();

        // Nạp thể loại vào ComboBox
        loadGenresToCombo();

        // Xử lý chọn trước thể loại (nếu được truyền từ màn hình khác sang)
        if (genreId > 0 && genreName != null) {
            cboGenre.setSelectedItem(genreName);
        }

        // Thực hiện tìm kiếm ban đầu
        doSearch();
    }

    private void initComponents() {
        setTitle("Khám phá truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(250, 250, 250));

        // --- Header Panel ---
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

        // --- Filter Panel ---
        JPanel filterPanel = new JPanel(new BorderLayout(10, 12));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        // Thanh tìm kiếm bằng từ khóa
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

        // Các bộ lọc bổ sung (Thể loại, Trạng thái, Loại, Sắp xếp)
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        filterRow.setOpaque(false);

        filterRow.add(new JLabel("Thể loại:"));
        cboGenre = new JComboBox<>();
        cboGenre.addItem("Tất cả");
        filterRow.add(cboGenre);

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

        // --- Result Panel ---
        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(filterPanel, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(center, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadGenresToCombo() {
        genreMap.clear();
        cboGenre.removeAllItems();
        cboGenre.addItem("Tất cả");

        // Load danh sách thể loại bất đồng bộ
        new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() {
                return storyDAO.getAllActiveGenres();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> genres = get();
                    for (Map.Entry<String, Integer> entry : genres.entrySet()) {
                        genreMap.put(entry.getKey(), entry.getValue());
                        cboGenre.addItem(entry.getKey());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void doSearch() {
        resultPanel.removeAll();
        
        // Hiển thị trạng thái đang tải
        JLabel lblLoading = new JLabel("Đang tải dữ liệu...");
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblLoading.setForeground(Color.GRAY);
        resultPanel.add(lblLoading);
        resultPanel.revalidate();
        resultPanel.repaint();

        String keyword = txtKeyword.getText().trim();
        String status = (String) cboStatus.getSelectedItem();
        String paid = (String) cboPaid.getSelectedItem();
        String sort = (String) cboSort.getSelectedItem();

        Integer genreId = null;
        String genreName = (String) cboGenre.getSelectedItem();
        if (genreName != null && !"Tất cả".equals(genreName) && genreMap.containsKey(genreName)) {
            genreId = genreMap.get(genreName);
        }

        final Integer finalGenreId = genreId;

        // Xử lý truy vấn dữ liệu bất đồng bộ với SwingWorker
        new SwingWorker<List<Story>, Void>() {
            @Override
            protected List<Story> doInBackground() {
                return storyDAO.searchWithFilter(keyword, status, paid, sort, finalGenreId);
            }

            @Override
            protected void done() {
                try {
                    List<Story> stories = get();
                    resultPanel.removeAll();

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
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
package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.StoryDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditStoryFrame extends JFrame {

    private final User currentUser;
    private final int storyId;
    private final StoryDAO storyDAO = new StoryDAO();
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> cboStatus;
    private JLabel lblCoverPreview;
    private String selectedCoverPath = null;

    // Biến cho phần Thể loại
    private JList<String> listGenres;
    private DefaultListModel<String> genreListModel;
    private Map<String, Integer> genreMap = new HashMap<>();

    public EditStoryFrame(int storyId) {
        this.currentUser = SessionManager.getCurrentUser();
        this.storyId = storyId;
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadStory();
        loadGenresAndSelect(storyId);
        loadCurrentCover();
    }

    private void initComponents() {
        setTitle("Sửa truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Sửa thông tin truyện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Tên truyện
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên truyện:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField(30);
        formPanel.add(txtTitle, gbc);

        // 2. Trạng thái
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cboStatus = new JComboBox<>(new String[]{"ONGOING", "COMPLETED", "DROPPED"});
        formPanel.add(cboStatus, gbc);

        // 3. Thể loại
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Thể loại:"), gbc);
        gbc.gridx = 1;
        genreListModel = new DefaultListModel<>();
        listGenres = new JList<>(genreListModel);
        listGenres.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listGenres.setVisibleRowCount(5);
        JScrollPane spGenres = new JScrollPane(listGenres);
        spGenres.setPreferredSize(new Dimension(280, 100));
        formPanel.add(spGenres, gbc);

        // 4. Mô tả
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtDescription = new JTextArea(5, 30);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtDescription), gbc);

        // 5. Ảnh bìa
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        formPanel.add(new JLabel("Ảnh bìa:"), gbc);

        gbc.gridx = 1;
        JPanel coverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        coverPanel.setOpaque(false);

        lblCoverPreview = new JLabel("Chưa có ảnh");
        lblCoverPreview.setPreferredSize(new Dimension(100, 130));
        lblCoverPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblCoverPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblCoverPreview.setOpaque(true);
        lblCoverPreview.setBackground(new Color(245, 245, 245));

        JButton btnChooseCover = new JButton("Đổi ảnh...");
        btnChooseCover.setFocusPainted(false);
        btnChooseCover.addActionListener(e -> chooseCover());

        coverPanel.add(lblCoverPreview);
        coverPanel.add(btnChooseCover);
        formPanel.add(coverPanel, gbc);

        // Panel Nút thao tác
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(0, 102, 204));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveStory());

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> {
            new AuthorStoryFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadStory() {
        String sql;
        if (currentUser.hasRole("ADMIN")) {
            sql = "SELECT title, description, status FROM stories WHERE story_id = ? AND is_deleted = 0";
        } else {
            sql = "SELECT title, description, status FROM stories WHERE story_id = ? AND author_id = ? AND is_deleted = 0";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, storyId);
            if (!currentUser.hasRole("ADMIN")) {
                ps.setInt(2, currentUser.getUserId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtTitle.setText(rs.getString("title"));
                    txtDescription.setText(rs.getString("description"));
                    cboStatus.setSelectedItem(rs.getString("status"));
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy truyện hoặc bạn không có quyền!");
                    new AuthorStoryFrame().setVisible(true);
                    this.dispose();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadGenresAndSelect(int storyId) {
        genreListModel.clear();
        genreMap.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT genre_id, genre_name FROM genres WHERE is_active = 1 ORDER BY genre_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("genre_name");
                genreMap.put(name, rs.getInt("genre_id"));
                genreListModel.addElement(name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> current = storyDAO.getGenreListByStoryId(storyId);
        listGenres.clearSelection();
        for (Map<String, Object> g : current) {
            String name = (String) g.get("genreName");
            int idx = genreListModel.indexOf(name);
            if (idx >= 0) {
                listGenres.addSelectionInterval(idx, idx);
            }
        }
    }

    private void chooseCover() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Ảnh (jpg, png, jpeg)", "jpg", "png", "jpeg"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        selectedCoverPath = file.getAbsolutePath();

        ImageIcon icon = new ImageIcon(selectedCoverPath);
        Image img = icon.getImage().getScaledInstance(100, 130, Image.SCALE_SMOOTH);
        lblCoverPreview.setIcon(new ImageIcon(img));
        lblCoverPreview.setText("");
    }

    private void loadCurrentCover() {
        try {
            String sql = "SELECT cover_url FROM stories WHERE story_id = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, storyId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String coverUrl = rs.getString("cover_url");

                        if (coverUrl != null && !coverUrl.isEmpty()) {
                            ImageIcon icon = new ImageIcon(coverUrl);
                            Image img = icon.getImage().getScaledInstance(
                                    100, 130, Image.SCALE_SMOOTH);
                            lblCoverPreview.setIcon(new ImageIcon(img));
                            lblCoverPreview.setText("");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveStory() {
        String title = txtTitle.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên truyện không được để trống!");
            return;
        }

        String sql;
        if (currentUser.hasRole("ADMIN")) {
            sql = "UPDATE stories SET title = ?, description = ?, status = ?, updated_at = GETDATE() "
                + "WHERE story_id = ?";
        } else {
            sql = "UPDATE stories SET title = ?, description = ?, status = ?, updated_at = GETDATE() "
                + "WHERE story_id = ? AND author_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, txtDescription.getText().trim());
            ps.setString(3, (String) cboStatus.getSelectedItem());
            ps.setInt(4, storyId);

            if (!currentUser.hasRole("ADMIN")) {
                ps.setInt(5, currentUser.getUserId());
            }

            int rows = ps.executeUpdate();
            if (rows == 0) {
                JOptionPane.showMessageDialog(this, "Không cập nhật được (không có quyền hoặc truyện không tồn tại)!");
                return;
            }

            // Cập nhật lại danh sách Thể loại
            List<String> selected = listGenres.getSelectedValuesList();
            storyDAO.clearStoryGenres(storyId);
            for (String name : selected) {
                Integer gid = genreMap.get(name);
                if (gid != null) {
                    storyDAO.addStoryGenre(storyId, gid);
                }
            }

            // Nếu người dùng chọn ảnh mới thì copy và cập nhật path
            if (selectedCoverPath != null) {
                try {
                    File coversDir = new File("covers");

                    if (!coversDir.exists()) {
                        coversDir.mkdir();
                    }

                    File src = new File(selectedCoverPath);

                    String ext = src.getName().substring(
                            src.getName().lastIndexOf('.'));

                    String newName = "cover_" + storyId + "_"
                            + System.currentTimeMillis() + ext;

                    File dest = new File(coversDir, newName);

                    java.nio.file.Files.copy(
                            src.toPath(),
                            dest.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );

                    storyDAO.updateCoverUrl(
                            storyId,
                            dest.getAbsolutePath()
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            new AuthorStoryFrame().setVisible(true);
            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
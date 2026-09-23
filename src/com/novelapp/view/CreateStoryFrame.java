package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.StoryDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateStoryFrame extends JFrame {

    private final User currentUser;
    private final StoryDAO storyDAO = new StoryDAO();

    private JTextField txtTitle;
    private JTextField txtAuthorName;
    private JComboBox<String> cboStatus;
    private JList<String> listGenres;
    private DefaultListModel<String> genreListModel;
    private final Map<String, Integer> genreMap = new HashMap<>();
    private JTextArea txtDescription;

    public CreateStoryFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || (!currentUser.hasRole("AUTHOR") && !currentUser.hasRole("ADMIN"))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền!");
            new HomeFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadGenresToList();
    }

    private void initComponents() {
        setTitle("Thêm truyện mới - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel lblTitle = new JLabel("Thêm truyện mới", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0, 102, 204));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên truyện
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Tên truyện:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtTitle = new JTextField(25);
        form.add(txtTitle, gbc);

        // Tên tác giả
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Tên tác giả:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtAuthorName = new JTextField(25);
        txtAuthorName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        // Author: có thể khóa; Admin sửa được
        if (currentUser.hasRole("AUTHOR") && !currentUser.hasRole("ADMIN")) {
            txtAuthorName.setEditable(false);
        }
        form.add(txtAuthorName, gbc);

        // Tình trạng
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Tình trạng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboStatus = new JComboBox<>(new String[]{"ONGOING", "COMPLETED", "DROPPED"});
        form.add(cboStatus, gbc);

        // Thể loại (chọn nhiều)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Thể loại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.anchor = GridBagConstraints.WEST;
        genreListModel = new DefaultListModel<>();
        listGenres = new JList<>(genreListModel);
        listGenres.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listGenres.setVisibleRowCount(6);
        JScrollPane scrollGenre = new JScrollPane(listGenres);
        scrollGenre.setPreferredSize(new Dimension(280, 110));
        form.add(scrollGenre, gbc);

        JLabel hint = new JLabel("Giữ Ctrl (hoặc Cmd) để chọn nhiều thể loại");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = 4;
        form.add(hint, gbc);

        // Mô tả
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        txtDescription = new JTextArea(6, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescription), gbc);

        card.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        buttons.setOpaque(false);

        JButton btnSave = new JButton("Lưu truyện");
        btnSave.setPreferredSize(new Dimension(130, 40));
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveStory());

        JButton btnBack = new JButton("Hủy");
        btnBack.setPreferredSize(new Dimension(100, 40));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new AuthorStoryFrame().setVisible(true);
            this.dispose();
        });

        buttons.add(btnSave);
        buttons.add(btnBack);
        card.add(buttons, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(card);
        main.add(wrap, BorderLayout.CENTER);
        add(main);
    }

    private void loadGenresToList() {
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
    }

    private void saveStory() {
        String title = txtTitle.getText().trim();
        String authorName = txtAuthorName.getText().trim();
        String status = (String) cboStatus.getSelectedItem();
        String description = txtDescription.getText().trim();
        List<String> selectedGenres = listGenres.getSelectedValuesList();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên truyện!");
            return;
        }
        if (selectedGenres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 thể loại!");
            return;
        }

        // slug đơn giản
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");

        String sql = "INSERT INTO stories (author_id, title, slug, description, status, moderation_status, published_at) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDING', GETDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, currentUser.getUserId());
            ps.setString(2, title);
            ps.setString(3, slug);
            ps.setString(4, description.isEmpty() ? null : description);
            ps.setString(5, status);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                int newStoryId = -1;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newStoryId = keys.getInt(1);
                }

                // Gắn nhiều thể loại
                if (newStoryId > 0) {
                    for (String name : selectedGenres) {
                        Integer gid = genreMap.get(name);
                        if (gid != null) {
                            storyDAO.addStoryGenre(newStoryId, gid);
                        }
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Thêm truyện thành công!\nTruyện đang chờ kiểm duyệt.\n"
                      + "Thể loại: " + String.join(", ", selectedGenres));
                new AuthorStoryFrame().setVisible(true);
                this.dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
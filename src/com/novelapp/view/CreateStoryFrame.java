package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.dao.StoryDAO;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    private JLabel lblCoverPreview;
    private String selectedCoverPath = null;

    public CreateStoryFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null
                || (!currentUser.hasRole("AUTHOR") && !currentUser.hasRole("ADMIN"))) {
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
        setSize(580, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTitle = new JLabel("Thêm truyện mới", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(0, 102, 204));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Tên truyện
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        form.add(new JLabel("Tên truyện:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtTitle = new JTextField(25);
        form.add(txtTitle, gbc);
        row++;

        // Tác giả
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Tác giả:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtAuthorName = new JTextField(25);
        if (currentUser.hasRole("ADMIN")) {
            txtAuthorName.setText("");
            txtAuthorName.setToolTipText("Nhập tên tác giả hiển thị");
        } else {
            txtAuthorName.setText(
                    currentUser.getFullName() != null
                            ? currentUser.getFullName()
                            : currentUser.getUsername());
            txtAuthorName.setEditable(false);
            txtAuthorName.setBackground(new Color(240, 240, 240));
        }
        form.add(txtAuthorName, gbc);
        row++;

        // Tình trạng
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Tình trạng:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        cboStatus = new JComboBox<>(new String[]{"ONGOING", "COMPLETED", "DROPPED"});
        form.add(cboStatus, gbc);
        row++;

        // Thể loại
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Thể loại:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        genreListModel = new DefaultListModel<>();
        listGenres = new JList<>(genreListModel);
        listGenres.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listGenres.setVisibleRowCount(5);
        form.add(new JScrollPane(listGenres) {{ setPreferredSize(new Dimension(280, 100)); }}, gbc);
        row++;

        gbc.gridx = 1; gbc.gridy = row;
        JLabel hint = new JLabel("Giữ Ctrl để chọn nhiều thể loại");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        form.add(hint, gbc);
        row++;

        // Mô tả
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        txtDescription = new JTextArea(5, 25);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        form.add(new JScrollPane(txtDescription), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        row++;

        // Ảnh bìa
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.NORTH;
        form.add(new JLabel("Ảnh bìa:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JPanel coverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        coverPanel.setOpaque(false);
        lblCoverPreview = new JLabel("Chưa có ảnh");
        lblCoverPreview.setPreferredSize(new Dimension(100, 130));
        lblCoverPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblCoverPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblCoverPreview.setOpaque(true);
        lblCoverPreview.setBackground(new Color(245, 245, 245));
        JButton btnCover = new JButton("Chọn ảnh...");
        btnCover.setFocusPainted(false);
        btnCover.addActionListener(e -> chooseCover());
        coverPanel.add(lblCoverPreview);
        coverPanel.add(btnCover);
        form.add(coverPanel, gbc);

        card.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        buttons.setOpaque(false);
        JButton btnSave = new JButton("Lưu truyện");
        btnSave.setPreferredSize(new Dimension(130, 40));
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setOpaque(true);
        btnSave.addActionListener(e -> saveStory());
        JButton btnBack = new JButton("Hủy");
        btnBack.setPreferredSize(new Dimension(100, 40));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new AuthorStoryFrame().setVisible(true);
            this.dispose();
        });
        buttons.add(btnSave);
        buttons.add(btnBack);
        card.add(buttons, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(15, 15, 15, 15));
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
                genreMap.put(rs.getString("genre_name"), rs.getInt("genre_id"));
                genreListModel.addElement(rs.getString("genre_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void chooseCover() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Ảnh (jpg, png, jpeg)", "jpg", "png", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        selectedCoverPath = chooser.getSelectedFile().getAbsolutePath();
        ImageIcon icon = new ImageIcon(selectedCoverPath);
        lblCoverPreview.setIcon(new ImageIcon(
                icon.getImage().getScaledInstance(100, 130, Image.SCALE_SMOOTH)));
        lblCoverPreview.setText("");
    }

    private void saveStory() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên truyện!");
            return;
        }

        String displayName = txtAuthorName.getText().trim();
        if (displayName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên tác giả!");
            return;
        }

        List<String> selectedGenres = listGenres.getSelectedValuesList();
        if (selectedGenres.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn ít nhất 1 thể loại!");
            return;
        }

        int authorId = currentUser.getUserId();

        String base = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (base.isEmpty()) base = "story";
        String slug = base + "-" + System.currentTimeMillis();

        String sql = "INSERT INTO stories (author_id, title, slug, description, status, "
                + "moderation_status, author_display_name, published_at) "
                + "VALUES (?, ?, ?, ?, ?, 'PENDING', ?, GETDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, authorId);
            ps.setString(2, title);
            ps.setString(3, slug);
            ps.setString(4, txtDescription.getText().trim().isEmpty() ? null : txtDescription.getText().trim());
            ps.setString(5, (String) cboStatus.getSelectedItem());
            ps.setString(6, displayName);

            if (ps.executeUpdate() > 0) {
                int newStoryId = -1;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) newStoryId = keys.getInt(1);
                }
                if (newStoryId > 0) {
                    for (String name : selectedGenres) {
                        Integer gid = genreMap.get(name);
                        if (gid != null) storyDAO.addStoryGenre(newStoryId, gid);
                    }
                    if (selectedCoverPath != null) {
                        try {
                            File dir = new File("covers");
                            if (!dir.exists()) dir.mkdir();
                            File src = new File(selectedCoverPath);
                            String ext = src.getName().substring(src.getName().lastIndexOf('.'));
                            File dest = new File(dir, "cover_" + newStoryId + "_" + System.currentTimeMillis() + ext);
                            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            storyDAO.updateCoverUrl(newStoryId, dest.getAbsolutePath());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Thêm truyện thành công!\nTruyện đang chờ duyệt.");
                new AuthorStoryFrame().setVisible(true);
                this.dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
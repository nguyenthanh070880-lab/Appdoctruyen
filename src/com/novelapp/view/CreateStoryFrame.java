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
import java.text.Normalizer;
import java.util.regex.Pattern;

public class CreateStoryFrame extends JFrame {

    private final User currentUser;
    private final StoryDAO storyDAO = new StoryDAO();
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> cboStatus;
    private JLabel lblCoverPreview;
    private String selectedCoverPath = null;

    public CreateStoryFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Thêm truyện mới - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Thêm truyện mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // ===== Form =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tên truyện
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên truyện:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField(30);
        formPanel.add(txtTitle, gbc);

        // Trạng thái
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cboStatus = new JComboBox<>(new String[]{"ONGOING", "COMPLETED", "DROPPED"});
        formPanel.add(cboStatus, gbc);

        // Ảnh bìa
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Ảnh bìa:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;

        JPanel coverChoosePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        coverChoosePanel.setOpaque(false);

        lblCoverPreview = new JLabel("Chưa chọn ảnh");
        lblCoverPreview.setPreferredSize(new Dimension(100, 130));
        lblCoverPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblCoverPreview.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblCoverPreview.setOpaque(true);
        lblCoverPreview.setBackground(new Color(245, 245, 245));

        JButton btnChooseCover = new JButton("Chọn ảnh...");
        btnChooseCover.setFocusPainted(false);
        btnChooseCover.addActionListener(e -> chooseCover());

        coverChoosePanel.add(lblCoverPreview);
        coverChoosePanel.add(btnChooseCover);
        formPanel.add(coverChoosePanel, gbc);

        // Mô tả
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Mô tả:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        txtDescription = new JTextArea(8, 30);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        formPanel.add(new JScrollPane(txtDescription), gbc);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnSave = new JButton("Lưu truyện");
        btnSave.setBackground(new Color(0, 153, 76));
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

    private String saveCoverFile(int storyId) {
        if (selectedCoverPath == null) return null;

        try {
            File coversDir = new File("covers");
            if (!coversDir.exists()) coversDir.mkdir();

            File src = new File(selectedCoverPath);
            String ext = src.getName().substring(src.getName().lastIndexOf('.'));

            String newName = "cover_" + storyId + "_" + System.currentTimeMillis() + ext;

            File dest = new File(coversDir, newName);

            java.nio.file.Files.copy(
                    src.toPath(),
                    dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return dest.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveStory() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String status = (String) cboStatus.getSelectedItem();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập tên truyện!",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String slug = toSlug(title);

        String sql = "INSERT INTO stories (author_id, title, slug, description, status, moderation_status, published_at) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDING', GETDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, currentUser.getUserId());
            ps.setString(2, title);
            ps.setString(3, slug);
            ps.setString(4, description.isEmpty() ? null : description);
            ps.setString(5, status);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                int newStoryId = -1;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        newStoryId = keys.getInt(1);
                    }
                }

                // Lưu ảnh bìa nếu có
                if (newStoryId > 0 && selectedCoverPath != null) {
                    String coverPath = saveCoverFile(newStoryId);

                    if (coverPath != null) {
                        storyDAO.updateCoverUrl(newStoryId, coverPath);
                    }
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Thêm truyện thành công!\nTruyện đang chờ kiểm duyệt.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                new AuthorStoryFrame().setVisible(true);
                this.dispose();
            }

        } catch (SQLException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi thêm truyện: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Chuyển tên truyện thành slug
    private String toSlug(String input) {
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);

        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        temp = pattern.matcher(temp).replaceAll("");

        temp = temp.toLowerCase()
                   .replaceAll("[^a-z0-9\\s-]", "")
                   .replaceAll("\\s+", "-")
                   .replaceAll("-+", "-");

        return temp + "-" + System.currentTimeMillis();
    }
}
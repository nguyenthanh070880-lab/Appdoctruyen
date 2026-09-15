package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class CreateChapterFrame extends JFrame {

    private final User currentUser;
    private final int storyId;
    private final String storyTitle;

    private JTextField txtChapterNumber;
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JCheckBox chkIsFree;
    private JTextField txtPriceCoin;

    public CreateChapterFrame(int storyId, String storyTitle) {
        this.currentUser = SessionManager.getCurrentUser();
        this.storyId = storyId;
        this.storyTitle = storyTitle;

        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Thêm chương - " + storyTitle);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Thêm chương mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        // ===== Form =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Số chương
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Số chương:"), gbc);
        gbc.gridx = 1;
        txtChapterNumber = new JTextField(10);
        formPanel.add(txtChapterNumber, gbc);

        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tiêu đề chương:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField(30);
        formPanel.add(txtTitle, gbc);

        // Miễn phí
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Miễn phí:"), gbc);
        gbc.gridx = 1;
        chkIsFree = new JCheckBox("Chương miễn phí");
        chkIsFree.setSelected(true);
        chkIsFree.addActionListener(e -> {
            txtPriceCoin.setEnabled(!chkIsFree.isSelected());
            if (chkIsFree.isSelected()) {
                txtPriceCoin.setText("0");
            }
        });
        formPanel.add(chkIsFree, gbc);

        // Giá Coin
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Giá Coin:"), gbc);
        gbc.gridx = 1;
        txtPriceCoin = new JTextField("0");
        txtPriceCoin.setEnabled(false);
        formPanel.add(txtPriceCoin, gbc);

        // Nội dung
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Nội dung:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtContent = new JTextArea(12, 40);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtContent), gbc);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnSave = new JButton("Lưu chương");
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveChapter());

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> {
            new AuthorChapterFrame(storyId, storyTitle).setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void saveChapter() {
        String numberStr = txtChapterNumber.getText().trim();
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        boolean isFree = chkIsFree.isSelected();
        String priceStr = txtPriceCoin.getText().trim();

        // Validate
        if (numberStr.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số chương và tiêu đề!", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double chapterNumber;
        try {
            chapterNumber = Double.parseDouble(numberStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số chương không hợp lệ!", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int priceCoin = 0;
        if (!isFree) {
            try {
                priceCoin = Integer.parseInt(priceStr);
                if (priceCoin < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Giá Coin không hợp lệ!", 
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String sql = "INSERT INTO chapters (story_id, chapter_number, title, content, is_free, price_coin, moderation_status, published_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', GETDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, storyId);
            ps.setDouble(2, chapterNumber);
            ps.setString(3, title);
            ps.setString(4, content.isEmpty() ? null : content);
            ps.setBoolean(5, isFree);
            ps.setInt(6, priceCoin);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Thêm chương thành công!\nChương đang chờ kiểm duyệt.", 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                new AuthorChapterFrame(storyId, storyTitle).setVisible(true);
                this.dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("UQ_story_chapter")) {
                JOptionPane.showMessageDialog(this, "Số chương này đã tồn tại!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm chương: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
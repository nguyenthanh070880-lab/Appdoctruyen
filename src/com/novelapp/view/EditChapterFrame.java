package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class EditChapterFrame extends JFrame {

    private final User currentUser;
    private final int chapterId;
    private final int storyId;
    private final String storyTitle;

    private JTextField txtChapterNumber;
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JCheckBox chkIsFree;
    private JTextField txtPriceCoin;

    public EditChapterFrame(int chapterId, int storyId, String storyTitle) {
        this.currentUser = SessionManager.getCurrentUser();
        this.chapterId = chapterId;
        this.storyId = storyId;
        this.storyTitle = storyTitle;

        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadChapter();
    }

    private void initComponents() {
        setTitle("Sửa chương - " + storyTitle);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Sửa chương");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

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
        chkIsFree.addActionListener(e -> {
            txtPriceCoin.setEnabled(!chkIsFree.isSelected());
            if (chkIsFree.isSelected()) txtPriceCoin.setText("0");
        });
        formPanel.add(chkIsFree, gbc);

        // Giá Coin
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Giá Coin:"), gbc);
        gbc.gridx = 1;
        txtPriceCoin = new JTextField("0");
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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(0, 102, 204));
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

    private void loadChapter() {
        String sql = "SELECT chapter_number, title, content, is_free, price_coin FROM chapters WHERE chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtChapterNumber.setText(String.valueOf(rs.getDouble("chapter_number")));
                    txtTitle.setText(rs.getString("title"));
                    txtContent.setText(rs.getString("content"));
                    boolean isFree = rs.getBoolean("is_free");
                    chkIsFree.setSelected(isFree);
                    txtPriceCoin.setText(String.valueOf(rs.getInt("price_coin")));
                    txtPriceCoin.setEnabled(!isFree);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveChapter() {
        String numberStr = txtChapterNumber.getText().trim();
        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();
        boolean isFree = chkIsFree.isSelected();
        String priceStr = txtPriceCoin.getText().trim();

        if (numberStr.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số chương và tiêu đề!");
            return;
        }

        double chapterNumber;
        try {
            chapterNumber = Double.parseDouble(numberStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số chương không hợp lệ!");
            return;
        }

        int priceCoin = 0;
        if (!isFree) {
            try {
                priceCoin = Integer.parseInt(priceStr);
                if (priceCoin < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Giá Coin không hợp lệ!");
                return;
            }
        }

        String sql = "UPDATE chapters SET chapter_number = ?, title = ?, content = ?, is_free = ?, price_coin = ?, updated_at = GETDATE() "
                   + "WHERE chapter_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, chapterNumber);
            ps.setString(2, title);
            ps.setString(3, content.isEmpty() ? null : content);
            ps.setBoolean(4, isFree);
            ps.setInt(5, priceCoin);
            ps.setInt(6, chapterId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cập nhật chương thành công!");
            new AuthorChapterFrame(storyId, storyTitle).setVisible(true);
            this.dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
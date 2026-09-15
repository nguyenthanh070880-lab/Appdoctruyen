package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class EditStoryFrame extends JFrame {

    private final User currentUser;
    private final int storyId;
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> cboStatus;

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
    }

    private void initComponents() {
        setTitle("Sửa truyện - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 480);
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

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Tên truyện:"), gbc);
        gbc.gridx = 1;
        txtTitle = new JTextField(30);
        formPanel.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cboStatus = new JComboBox<>(new String[]{"ONGOING", "COMPLETED", "DROPPED"});
        formPanel.add(cboStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Mô tả:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtDescription = new JTextArea(8, 30);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(txtDescription), gbc);

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
        String sql = "SELECT title, description, status FROM stories WHERE story_id = ? AND author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtTitle.setText(rs.getString("title"));
                    txtDescription.setText(rs.getString("description"));
                    cboStatus.setSelectedItem(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveStory() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên truyện không được để trống!");
            return;
        }

        String sql = "UPDATE stories SET title = ?, description = ?, status = ?, updated_at = GETDATE() "
                   + "WHERE story_id = ? AND author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, txtDescription.getText().trim());
            ps.setString(3, (String) cboStatus.getSelectedItem());
            ps.setInt(4, storyId);
            ps.setInt(5, currentUser.getUserId());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            new AuthorStoryFrame().setVisible(true);
            this.dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
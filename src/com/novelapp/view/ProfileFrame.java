package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.User;
import com.novelapp.util.PasswordUtil;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ProfileFrame extends JFrame {

    private final User currentUser;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    public ProfileFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadUserInfo();
    }

    private void initComponents() {
        setTitle("Hồ sơ cá nhân - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitle = new JLabel("👤  Hồ sơ cá nhân");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← Trang chủ");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(new Color(0, 102, 204));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnBack, BorderLayout.EAST);

        // Form
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username (readonly)
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField txtUsername = new JTextField(currentUser.getUsername());
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(240, 240, 240));
        txtUsername.setPreferredSize(new Dimension(250, 36));
        formPanel.add(txtUsername, gbc);

        // Full name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFullName = createField();
        formPanel.add(txtFullName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtEmail = createField();
        formPanel.add(txtEmail, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPhone = createField();
        formPanel.add(txtPhone, gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(Box.createVerticalStrut(10), gbc);

        gbc.gridy = 5;
        JLabel lblPass = new JLabel("— Đổi mật khẩu —");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(lblPass, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        formPanel.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtOldPassword = createPasswordField();
        formPanel.add(txtOldPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        formPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNewPassword = createPasswordField();
        formPanel.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0;
        formPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtConfirmPassword = createPasswordField();
        formPanel.add(txtConfirmPassword, gbc);

        card.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
        buttonPanel.setOpaque(false);

        JButton btnDelete = new JButton("Xóa tài khoản");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteAccount());

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnSave.setBackground(new Color(0, 102, 204));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProfile());

        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSave);
        card.add(buttonPanel, BorderLayout.SOUTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JTextField createField() {
        JTextField tf = new JTextField(20);
        tf.setPreferredSize(new Dimension(250, 36));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField(20);
        pf.setPreferredSize(new Dimension(250, 36));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    private void loadUserInfo() {
        txtFullName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
        txtEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        txtPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
    }

    private void saveProfile() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và Email không được để trống!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = GETDATE() WHERE user_id = ?")) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone.isEmpty() ? null : phone);
            ps.setInt(4, currentUser.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thông tin!");
            return;
        }

        if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin đổi mật khẩu!");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return;
            }
            if (!PasswordUtil.checkPassword(oldPass, currentUser.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Mật khẩu cũ không đúng!");
                return;
            }

            String newHash = PasswordUtil.hashPassword(newPass);
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE users SET password_hash = ?, updated_at = GETDATE() WHERE user_id = ?")) {
                ps.setString(1, newHash);
                ps.setInt(2, currentUser.getUserId());
                ps.executeUpdate();
                currentUser.setPasswordHash(newHash);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi đổi mật khẩu!");
                return;
            }
        }

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        SessionManager.setCurrentUser(currentUser);

        JOptionPane.showMessageDialog(this, "Cập nhật hồ sơ thành công!");
        new HomeFrame().setVisible(true);
        this.dispose();
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có CHẮC CHẮN muốn xóa tài khoản?\nHành động này không thể hoàn tác!",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String password = JOptionPane.showInputDialog(this, "Nhập mật khẩu để xác nhận xóa:");
        if (password == null || password.isEmpty()) return;

        if (!PasswordUtil.checkPassword(password, currentUser.getPasswordHash())) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không đúng!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users SET is_deleted = 1, status = 'BANNED', updated_at = GETDATE() WHERE user_id = ?")) {
            ps.setInt(1, currentUser.getUserId());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Tài khoản đã được xóa.\nBạn sẽ được đăng xuất.");
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
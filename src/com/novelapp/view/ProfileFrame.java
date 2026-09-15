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
        setSize(550, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Hồ sơ cá nhân");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        // ===== Form thông tin =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username (không cho sửa)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField txtUsername = new JTextField(currentUser.getUsername());
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(240, 240, 240));
        formPanel.add(txtUsername, gbc);

        // Họ tên
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(25);
        formPanel.add(txtFullName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(25);
        formPanel.add(txtEmail, gbc);

        // Số điện thoại
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(25);
        formPanel.add(txtPhone, gbc);

        // ===== Đổi mật khẩu =====
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(Box.createVerticalStrut(15), gbc);

        gbc.gridy = 5;
        JLabel lblChangePass = new JLabel("— Đổi mật khẩu —");
        lblChangePass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(lblChangePass, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1;
        txtOldPassword = new JPasswordField(25);
        formPanel.add(txtOldPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1;
        txtNewPassword = new JPasswordField(25);
        formPanel.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        formPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1;
        txtConfirmPassword = new JPasswordField(25);
        formPanel.add(txtConfirmPassword, gbc);

        // ===== Buttons =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        JButton btnDeleteAccount = new JButton("Xóa tài khoản");
        btnDeleteAccount.setBackground(new Color(220, 53, 69));
        btnDeleteAccount.setForeground(Color.WHITE);
        btnDeleteAccount.setFocusPainted(false);
        btnDeleteAccount.addActionListener(e -> deleteAccount());

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(0, 102, 204));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveProfile());

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new HomeFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnDeleteAccount);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
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
            JOptionPane.showMessageDialog(this, "Họ tên và Email không được để trống!", 
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Cập nhật thông tin cơ bản
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone.isEmpty() ? null : phone);
            ps.setInt(4, currentUser.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật thông tin: " + e.getMessage());
            return;
        }

        // Đổi mật khẩu (nếu có nhập)
        if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirmPass.isEmpty()) {
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin đổi mật khẩu!", 
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", 
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự!", 
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Kiểm tra mật khẩu cũ
            if (!PasswordUtil.checkPassword(oldPass, currentUser.getPasswordHash())) {
                JOptionPane.showMessageDialog(this, "Mật khẩu cũ không đúng!", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Cập nhật mật khẩu mới
            String newHash = PasswordUtil.hashPassword(newPass);
            String sqlPass = "UPDATE users SET password_hash = ?, updated_at = GETDATE() WHERE user_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlPass)) {
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

        // Cập nhật session
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
                "Xác nhận xóa tài khoản",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Xác nhận lần 2 bằng mật khẩu
        String password = JOptionPane.showInputDialog(this, "Nhập mật khẩu để xác nhận xóa:");
        if (password == null || password.isEmpty()) return;

        if (!PasswordUtil.checkPassword(password, currentUser.getPasswordHash())) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không đúng!");
            return;
        }

        String sql = "UPDATE users SET is_deleted = 1, status = 'BANNED', updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Tài khoản đã được xóa.\nBạn sẽ được đăng xuất.");
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            this.dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }
}
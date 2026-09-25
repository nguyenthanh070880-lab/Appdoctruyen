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
    private JPasswordField txtOldPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public ProfileFrame() {
        this.currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            new LoginFrame().setVisible(true);
            this.dispose();
            return;
        }
        initComponents();
        loadProfile();
    }

    private void initComponents() {
        setTitle("Hồ sơ cá nhân - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(245, 247, 250));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

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

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnBack, BorderLayout.EAST);

        // Card
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username (không sửa)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField txtUsername = new JTextField(currentUser.getUsername());
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(240, 240, 240));
        form.add(txtUsername, gbc);

        // Họ tên
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtFullName = new JTextField(20);
        form.add(txtFullName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtEmail = new JTextField(20);
        form.add(txtEmail, gbc);

        // SĐT
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        form.add(new JLabel("Số điện thoại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtPhone = new JTextField(20);
        form.add(txtPhone, gbc);

        // Đổi MK
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JLabel lblPw = new JLabel("— Đổi mật khẩu —", SwingConstants.CENTER);
        lblPw.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(lblPw, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        form.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtOldPass = new JPasswordField(20);
        form.add(txtOldPass, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        form.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtNewPass = new JPasswordField(20);
        form.add(txtNewPass, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        form.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtConfirmPass = new JPasswordField(20);
        form.add(txtConfirmPass, gbc);

        card.add(form, BorderLayout.CENTER);

        // Nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setOpaque(false);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.setBackground(new Color(0, 102, 204));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setOpaque(true);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveProfile());

        JButton btnDelete = new JButton("Xóa tài khoản");
        btnDelete.setPreferredSize(new Dimension(150, 40));
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setOpaque(true);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> deleteAccount());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);
        card.add(buttonPanel, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrap.add(card);

        main.add(header, BorderLayout.NORTH);
        main.add(wrap, BorderLayout.CENTER);
        add(main);
    }

    private void loadProfile() {
        String sql = "SELECT full_name, email, phone FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtFullName.setText(rs.getString("full_name"));
                    txtEmail.setText(rs.getString("email"));
                    String phone = rs.getString("phone");
                    txtPhone.setText(phone != null ? phone : "");
                }
            }
        } catch (SQLException e) {
            // Nếu chưa có cột phone → load không phone
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT full_name, email FROM users WHERE user_id = ?")) {
                ps.setInt(1, currentUser.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        txtFullName.setText(rs.getString("full_name"));
                        txtEmail.setText(rs.getString("email"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void saveProfile() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String oldPass = new String(txtOldPass.getPassword());
        String newPass = new String(txtNewPass.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());

        if (fullName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và Email không được trống!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Đổi mật khẩu nếu có nhập
            if (!oldPass.isEmpty() || !newPass.isEmpty() || !confirm.isEmpty()) {
                if (oldPass.isEmpty() || newPass.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nhập đủ mật khẩu cũ và mới!");
                    return;
                }
                if (!newPass.equals(confirm)) {
                    JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
                    return;
                }
                if (newPass.length() < 6) {
                    JOptionPane.showMessageDialog(this, "Mật khẩu mới tối thiểu 6 ký tự!");
                    return;
                }

                String hash = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT password_hash FROM users WHERE user_id = ?")) {
                    ps.setInt(1, currentUser.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) hash = rs.getString("password_hash");
                    }
                }
                if (hash == null || !PasswordUtil.checkPassword(oldPass, hash)) {
                    JOptionPane.showMessageDialog(this, "Mật khẩu cũ không đúng!");
                    return;
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET password_hash = ?, updated_at = GETDATE() WHERE user_id = ?")) {
                    ps.setString(1, PasswordUtil.hashPassword(newPass));
                    ps.setInt(2, currentUser.getUserId());
                    ps.executeUpdate();
                }
            }

            // Cập nhật thông tin (có phone)
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET full_name = ?, email = ?, phone = ?, updated_at = GETDATE() WHERE user_id = ?")) {
                ps.setString(1, fullName);
                ps.setString(2, email);
                ps.setString(3, phone.isEmpty() ? null : phone);
                ps.setInt(4, currentUser.getUserId());
                ps.executeUpdate();
            } catch (SQLException ex) {
                // Không có cột phone
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET full_name = ?, email = ?, updated_at = GETDATE() WHERE user_id = ?")) {
                    ps.setString(1, fullName);
                    ps.setString(2, email);
                    ps.setInt(3, currentUser.getUserId());
                    ps.executeUpdate();
                }
            }

            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            JOptionPane.showMessageDialog(this, "Đã lưu thay đổi!");
            txtOldPass.setText("");
            txtNewPass.setText("");
            txtConfirmPass.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn xóa tài khoản?\nSẽ không đăng nhập được nữa.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE users SET is_deleted = 1, status = 'LOCKED', updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa tài khoản.");
                SessionManager.logout();
                new LoginFrame().setVisible(true);
                this.dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
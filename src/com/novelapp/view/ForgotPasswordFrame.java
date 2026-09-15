package com.novelapp.view;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.util.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class ForgotPasswordFrame extends JFrame {

    private JTextField txtEmail;
    private JTextField txtCode;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private String generatedCode;
    private int userId = -1;

    public ForgotPasswordFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Quên mật khẩu - NovelApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Quên mật khẩu", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 102, 204));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Mã xác nhận:"), gbc);
        gbc.gridx = 1;
        txtCode = new JTextField(20);
        formPanel.add(txtCode, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1;
        txtNewPassword = new JPasswordField(20);
        formPanel.add(txtNewPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1;
        txtConfirmPassword = new JPasswordField(20);
        formPanel.add(txtConfirmPassword, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setOpaque(false);

        JButton btnSendCode = new JButton("Gửi mã");
        btnSendCode.setFocusPainted(false);
        btnSendCode.addActionListener(e -> sendCode());

        JButton btnReset = new JButton("Đặt lại mật khẩu");
        btnReset.setBackground(new Color(0, 102, 204));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> resetPassword());

        JButton btnBack = new JButton("Quay lại");
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnSendCode);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnBack);

        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void sendCode() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email!");
            return;
        }

        String sql = "SELECT user_id FROM users WHERE email = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    // Tạo mã 6 số ngẫu nhiên (mô phỏng gửi email)
                    generatedCode = String.valueOf((int)(Math.random() * 900000) + 100000);
                    
                    JOptionPane.showMessageDialog(this,
                            "Mã xác nhận đã được gửi (mô phỏng):\n\n" + generatedCode +
                            "\n\n(Trong thực tế sẽ gửi qua email)",
                            "Mã xác nhận", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Email không tồn tại trong hệ thống!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetPassword() {
        if (userId == -1 || generatedCode == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng gửi mã xác nhận trước!");
            return;
        }

        String code = txtCode.getText().trim();
        String newPass = new String(txtNewPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (!code.equals(generatedCode)) {
            JOptionPane.showMessageDialog(this, "Mã xác nhận không đúng!");
            return;
        }
        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        if (!newPass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
            return;
        }

        String hash = PasswordUtil.hashPassword(newPass);
        String sql = "UPDATE users SET password_hash = ?, updated_at = GETDATE() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Đặt lại mật khẩu thành công!\nBạn có thể đăng nhập lại.");
            new LoginFrame().setVisible(true);
            this.dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
    }
}
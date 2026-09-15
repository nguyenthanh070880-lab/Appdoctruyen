package com.novelapp.view;

import com.novelapp.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtFullName;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;
    private JButton btnBack;

    private final AuthService authService = new AuthService();

    public RegisterFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Đăng ký tài khoản - NovelApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // Card
        JPanel card = new JPanel(new BorderLayout(15, 20));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));

        // Title
        JLabel lblTitle = new JLabel("Đăng ký tài khoản", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 102, 204));
        card.add(lblTitle, BorderLayout.NORTH);

        // Form (nhãn trái - ô phải)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        txtUsername = createTextField();
        formPanel.add(txtUsername, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        txtEmail = createTextField();
        formPanel.add(txtEmail, gbc);

        // Họ tên
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        txtFullName = createTextField();
        formPanel.add(txtFullName, gbc);

        // Mật khẩu
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        txtPassword = createPasswordField();
        formPanel.add(txtPassword, gbc);

        // Xác nhận mật khẩu
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        txtConfirmPassword = createPasswordField();
        formPanel.add(txtConfirmPassword, gbc);

        card.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.setOpaque(false);

        btnRegister = new JButton("Đăng ký");
        btnRegister.setPreferredSize(new Dimension(140, 42));
        btnRegister.setBackground(new Color(0, 153, 76));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> handleRegister());

        btnBack = new JButton("Quay lại");
        btnBack.setPreferredSize(new Dimension(140, 42));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBack);
        card.add(buttonPanel, BorderLayout.SOUTH);

        // Center card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);
        add(mainPanel);

        getRootPane().setDefaultButton(btnRegister);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(250, 36));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField(20);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setPreferredSize(new Dimension(250, 36));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        String error = authService.register(username, email, password, confirmPassword, fullName);

        if (error == null) {
            JOptionPane.showMessageDialog(this,
                    "Đăng ký thành công!\nBạn có thể đăng nhập ngay bây giờ.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, error, "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }
}
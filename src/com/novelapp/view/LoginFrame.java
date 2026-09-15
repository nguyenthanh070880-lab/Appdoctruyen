package com.novelapp.view;

import com.novelapp.model.User;
import com.novelapp.service.AuthService;
import com.novelapp.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private JCheckBox chkRemember;

    private final AuthService authService = new AuthService();
    private static final String REMEMBER_FILE = "remember.properties";

    public LoginFrame() {
        initComponents();
        loadRemember();
    }

    private void initComponents() {
        setTitle("Đăng nhập - NovelApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        // Nền tổng
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // Card đăng nhập
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));

        // Tiêu đề
        JLabel lblTitle = new JLabel("📚  NOVEL APP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(0, 102, 204));

        JLabel lblSubtitle = new JLabel("Đăng nhập để tiếp tục", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(lblSubtitle);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username / Email"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Mật khẩu"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(txtPassword, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        chkRemember = new JCheckBox("Ghi nhớ đăng nhập");
        chkRemember.setOpaque(false);
        formPanel.add(chkRemember, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        topButtons.setOpaque(false);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setPreferredSize(new Dimension(130, 40));
        btnLogin.setBackground(new Color(0, 102, 204));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRegister = new JButton("Đăng ký");
        btnRegister.setPreferredSize(new Dimension(130, 40));
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        topButtons.add(btnLogin);
        topButtons.add(btnRegister);

        JButton btnForgot = new JButton("Quên mật khẩu?");
        btnForgot.setFocusPainted(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setForeground(new Color(0, 102, 204));
        btnForgot.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> {
            new ForgotPasswordFrame().setVisible(true);
            this.dispose();
        });

        buttonPanel.add(topButtons);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnForgot);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(formPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        // Căn giữa card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(card);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);
        add(mainPanel);

        // Sự kiện
        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            this.dispose();
        });
        getRootPane().setDefaultButton(btnLogin);
    }

    private void handleLogin() {
        String usernameOrEmail = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = authService.login(usernameOrEmail, password);

        if (user != null) {
            SessionManager.setCurrentUser(user);

            if (chkRemember.isSelected()) {
                saveRemember(usernameOrEmail);
            } else {
                clearRemember();
            }

            JOptionPane.showMessageDialog(this,
                    "Đăng nhập thành công!\nXin chào " + user.getFullName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            this.dispose();
            new HomeFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Sai tên đăng nhập hoặc mật khẩu!\nHoặc tài khoản đã bị khóa.",
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveRemember(String username) {
        try {
            Properties props = new Properties();
            props.setProperty("username", username);
            props.setProperty("remember", "true");
            try (FileOutputStream out = new FileOutputStream(REMEMBER_FILE)) {
                props.store(out, "Remember login");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearRemember() {
        try {
            File file = new File(REMEMBER_FILE);
            if (file.exists()) file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRemember() {
        try {
            File file = new File(REMEMBER_FILE);
            if (!file.exists()) return;

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            }
            String username = props.getProperty("username");
            String remember = props.getProperty("remember");

            if ("true".equals(remember) && username != null) {
                txtUsername.setText(username);
                chkRemember.setSelected(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
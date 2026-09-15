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
        setSize(460, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        // Title
        JLabel lblTitle = new JLabel("📚  NOVEL APP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Đăng nhập để tiếp tục");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(Color.GRAY);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(25));

        // Username
        JLabel lblUser = new JLabel("Username / Email");
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblUser);
        card.add(Box.createVerticalStrut(6));

        txtUsername = new JTextField();
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(15));

        // Password
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblPass);
        card.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField();
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(12));

        // Remember
        chkRemember = new JCheckBox("Ghi nhớ đăng nhập");
        chkRemember.setOpaque(false);
        chkRemember.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(chkRemember);
        card.add(Box.createVerticalStrut(25));

        // ===== NÚT ĐĂNG NHẬP + ĐĂNG KÝ =====
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        topButtons.setOpaque(false);
        topButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setPreferredSize(new Dimension(140, 42));
        btnLogin.setBackground(new Color(0, 102, 204));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRegister = new JButton("Đăng ký");
        btnRegister.setPreferredSize(new Dimension(140, 42));
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setBackground(new Color(240, 240, 240));

        topButtons.add(btnLogin);
        topButtons.add(btnRegister);
        card.add(topButtons);
        card.add(Box.createVerticalStrut(15));

        // Quên mật khẩu
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
        card.add(btnForgot);

        // Căn giữa
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
package com.novelapp.service;

import com.novelapp.dao.UserDAO;
import com.novelapp.model.User;
import com.novelapp.util.PasswordUtil;

import java.util.List;

public class AuthService {
    
    private final UserDAO userDAO = new UserDAO();
    
    /**
     * Đăng ký tài khoản mới
     * @return null nếu thành công, ngược lại trả về thông báo lỗi
     */
    public String register(String username, String email, String password, 
                           String confirmPassword, String fullName) {
        
        // Validate cơ bản
        if (username == null || username.trim().isEmpty()) {
            return "Username không được để trống";
        }
        if (email == null || email.trim().isEmpty()) {
            return "Email không được để trống";
        }
        if (password == null || password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        if (!password.equals(confirmPassword)) {
            return "Mật khẩu xác nhận không khớp";
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Họ tên không được để trống";
        }
        
        // Kiểm tra trùng
        if (userDAO.isUsernameExists(username.trim())) {
            return "Username đã tồn tại";
        }
        if (userDAO.isEmailExists(email.trim())) {
            return "Email đã tồn tại";
        }
        
        // Tạo user
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setFullName(fullName.trim());
        user.setStatus("ACTIVE");
        
        boolean success = userDAO.register(user);
        
        if (success) {
            return null; // thành công
        } else {
            return "Đăng ký thất bại, vui lòng thử lại";
        }
    }
    
    /**
     * Đăng nhập
     * @return User nếu thành công, null nếu thất bại
     */
    public User login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return null;
        }
        
        User user = userDAO.findByUsernameOrEmail(usernameOrEmail.trim());
        
        if (user == null) {
            return null; // không tìm thấy tài khoản
        }
        
        // Kiểm tra trạng thái
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null; // tài khoản bị khóa
        }
        
        // Kiểm tra mật khẩu
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return null; // sai mật khẩu
        }
        
        // Lấy roles
        List<String> roles = userDAO.getRolesByUserId(user.getUserId());
        user.setRoles(roles);
        
        // Cập nhật last_login
        userDAO.updateLastLogin(user.getUserId());
        
        return user;
    }
}
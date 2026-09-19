package com.novelapp.service;

import com.novelapp.dao.UserDAO;
import com.novelapp.model.User;
import com.novelapp.util.PasswordUtil;

import java.util.List;

public class AuthService {
    
    private final UserDAO userDAO = new UserDAO();
    
    /**
     * Đăng ký tài khoản mới bao gồm Ngày sinh
     * @return null nếu thành công, ngược lại trả về thông báo lỗi
     */
    public String register(String username, String email, String password, 
                           String confirmPassword, String fullName, String birthDate) {
        
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
        if (birthDate == null || birthDate.trim().isEmpty()) {
            return "Ngày sinh không được để trống";
        }
        
        // Kiểm tra trùng lặp
        if (userDAO.isUsernameExists(username.trim())) {
            return "Username đã tồn tại";
        }
        if (userDAO.isEmailExists(email.trim())) {
            return "Email đã tồn tại";
        }
        
        // Tạo đối tượng User
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setFullName(fullName.trim());
        user.setBirthDate(birthDate.trim());
        user.setStatus("ACTIVE");
        
        boolean success = userDAO.register(user);
        
        if (success) {
            return null; // Thành công
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
            return null; // Không tìm thấy tài khoản
        }
        
        // Kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null; // Tài khoản bị khóa / không hoạt động
        }
        
        // Kiểm tra mật khẩu
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            return null; // Sai mật khẩu
        }
        
        // Lấy danh sách quyền (roles)
        List<String> roles = userDAO.getRolesByUserId(user.getUserId());
        user.setRoles(roles);
        
        // Cập nhật thời gian đăng nhập gần nhất
        userDAO.updateLastLogin(user.getUserId());
        
        return user;
    }
}
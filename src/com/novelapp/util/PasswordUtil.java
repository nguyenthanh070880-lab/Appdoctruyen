package com.novelapp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    // Độ mạnh hash (10-12 là ổn)
    private static final int LOG_ROUNDS = 10;
    
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }
    
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
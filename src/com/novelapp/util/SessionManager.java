package com.novelapp.util;

import com.novelapp.model.User;

public class SessionManager {
    
    private static User currentUser;
    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void logout() {
        currentUser = null;
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static boolean hasRole(String roleName) {
        return currentUser != null && currentUser.hasRole(roleName);
    }
}
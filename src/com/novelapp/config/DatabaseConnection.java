package com.novelapp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlserver://127.0.0.1:1433;"
            + "databaseName=NovelApp;user=sa;password=MatKhauCuaBan123;"
            + "encrypt=true;trustServerCertificate=true;";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy SQL Server JDBC Driver", e);
        }
    }
   
    public static void main(String[] args) {
        System.out.println("Đang kết nối lại sau khi kích hoạt cổng hệ thống...");
        try (Connection conn = getConnection()) {
            System.out.println(" KẾT NỐI SQL SERVER THÀNH CÔNG!");
        } catch (SQLException e) {
            System.err.println(" KẾT NỐI THẤT BẠI!");
            System.err.println("Chi tiết lỗi hệ thống:\n" + e.getMessage());
        }
    }
}

package com.novelapp.dao;

import com.novelapp.config.DatabaseConnection;
import com.novelapp.model.CoinPackage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoinPackageDAO {

    public List<CoinPackage> getActivePackages() {
        List<CoinPackage> list = new ArrayList<>();
        String sql = "SELECT * FROM coin_packages WHERE is_active = 1 ORDER BY sort_order ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                CoinPackage pkg = new CoinPackage();
                pkg.setPackageId(rs.getInt("package_id"));
                pkg.setPackageName(rs.getString("package_name"));
                pkg.setCoinAmount(rs.getInt("coin_amount"));
                pkg.setPriceVnd(rs.getLong("price_vnd"));
                pkg.setBonusCoin(rs.getInt("bonus_coin"));
                pkg.setActive(rs.getBoolean("is_active"));
                list.add(pkg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public CoinPackage findById(int packageId) {
        String sql = "SELECT * FROM coin_packages WHERE package_id = ? AND is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CoinPackage pkg = new CoinPackage();
                    pkg.setPackageId(rs.getInt("package_id"));
                    pkg.setPackageName(rs.getString("package_name"));
                    pkg.setCoinAmount(rs.getInt("coin_amount"));
                    pkg.setPriceVnd(rs.getLong("price_vnd"));
                    pkg.setBonusCoin(rs.getInt("bonus_coin"));
                    pkg.setActive(rs.getBoolean("is_active"));
                    return pkg;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
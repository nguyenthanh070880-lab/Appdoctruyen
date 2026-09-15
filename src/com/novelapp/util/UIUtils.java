package com.novelapp.util;

import javax.swing.*;
import java.awt.*;

public class UIUtils {

    // Màu chủ đạo
    public static final Color PRIMARY = new Color(0, 102, 204);
    public static final Color SUCCESS = new Color(0, 153, 76);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color WARNING = new Color(255, 193, 7);
    public static final Color LIGHT_BG = new Color(248, 249, 250);
    public static final Color CARD_BG = new Color(255, 255, 255);
    public static final Color BORDER = new Color(220, 220, 220);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JLabel createTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setGridColor(BORDER);
    }
}
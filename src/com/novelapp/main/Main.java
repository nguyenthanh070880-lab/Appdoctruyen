package com.novelapp.main;

import com.novelapp.view.LoginFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
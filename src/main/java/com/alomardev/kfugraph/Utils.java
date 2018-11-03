package com.alomardev.kfugraph;

import java.awt.Component;
import javax.swing.JOptionPane;

public class Utils {

    public static boolean confirm(Component root, String message) {
        return JOptionPane.showConfirmDialog(root, message, "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
    
    public static void error(Component root, String message) {
        JOptionPane.showMessageDialog(root, message, "Error!", JOptionPane.ERROR_MESSAGE);
    }

}

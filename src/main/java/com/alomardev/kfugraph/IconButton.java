package com.alomardev.kfugraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class IconButton extends JButton {
    
    private static final Color HOVER_COLOR = new Color(0, 0, 0, .15f);
    private static final Color PRESS_COLOR = new Color(0, 0, 0, .3f);
    
    public IconButton(String src) {
        this(src, 10);
    }
    public IconButton(String src, int padding) {
        URL iconUrl = R.getURL(src);
        if (iconUrl != null) super.setIcon(new ImageIcon(iconUrl));
        super.setBorderPainted(false);
        super.setContentAreaFilled(false);
        super.setFocusPainted(false);
        super.setOpaque(false);
        if (iconUrl != null) super.setPreferredSize(new Dimension(getIcon().getIconWidth() + padding,
            getIcon().getIconHeight() + padding));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if (getModel().isPressed()) {
            g.setColor(PRESS_COLOR);
        } else if (getModel().isRollover()) {
            g.setColor(HOVER_COLOR);
        } else {
            g.setColor(getBackground());
        }
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
        super.paintComponent(g);
    }
}

package com.alomardev.kfugraph;

import java.awt.Font;
import java.awt.GridBagConstraints;

public class LayoutUtils {
    
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    
    public static final int GRID_PADDING = 5;
    
    public static void gbc(GridBagConstraints c, int x, int y, int width, int height, int weightx, int weighty,
        int anchor, boolean fillHorizontal, boolean fillVertical, int left, int top, int right, int bottom) {
        c.gridx = x == -1 ? c.gridx : x;
        c.gridy = y == -1 ? c.gridy : y;
        c.gridwidth = width == -1 ? c.gridwidth : width;
        c.gridheight = height == -1 ? c.gridheight : height;
        c.weightx = weightx == -1 ? c.weightx : weightx;
        c.weighty = weighty == -1 ? c.weighty : weighty;
        c.anchor = anchor == -1 ? c.anchor : anchor;
        c.insets.left = left == -1 ? c.insets.left : left * GRID_PADDING;
        c.insets.top = top == -1 ? c.insets.top : top * GRID_PADDING;
        c.insets.right = right == -1 ? c.insets.right : right * GRID_PADDING;
        c.insets.bottom = bottom == -1 ? c.insets.bottom : bottom * GRID_PADDING;

        if (fillVertical && fillHorizontal) {
            c.fill = GridBagConstraints.BOTH;
        } else if (fillVertical) {
            c.fill = GridBagConstraints.VERTICAL;
        } else if (fillHorizontal) {
            c.fill = GridBagConstraints.HORIZONTAL;
        } else {
            c.fill = GridBagConstraints.NONE;
        }
    }
    
    public static int getPad(float multi) {
        return (int) (GRID_PADDING * multi);
    }
}

package com.example;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class MyMouseListener implements MouseListener {
    private long lastMousePress = -1;
    private long lastMouseRelease = -1;
    private final long startTime;

    private final ClickLoggerPlugin plugin;

    public MyMouseListener(ClickLoggerPlugin plugin, long startTime) {
        this.plugin = plugin;
        this.startTime = startTime;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePress = e.getWhen() - startTime;
        plugin.getLog().info("Mouse pressed at (" + e.getX() + ", " + e.getY() + "); time " + lastMousePress + "; # clicks: " + e.getClickCount());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMouseRelease = e.getWhen() - startTime;
        plugin.getLog().info("Mouse released at (" + e.getX() + ", " + e.getY() + "); time " + lastMouseRelease + "; # clicks: " + e.getClickCount());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        plugin.getLog().info("Mouse clicked at (" + e.getX() + ", " + e.getY() + "); time " + (e.getWhen() - startTime) + "; # clicks: " + e.getClickCount() + "; debounce " + (lastMouseRelease - lastMousePress) + "ms");
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

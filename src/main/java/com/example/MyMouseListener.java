package com.example;

import net.runelite.client.config.Config;

import javax.inject.Inject;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class MyMouseListener implements MouseListener {
    private long lastMousePress = -1;
    private long lastMouseRelease = -1;
    private final long startTime;

    private final ClickLoggerPlugin plugin;

    @Inject
    private ClickLoggerConfig config;

    public MyMouseListener(ClickLoggerPlugin plugin, long startTime) {
        this.plugin = plugin;
        this.startTime = startTime;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePress = e.getWhen() - startTime;
        if (config.priority()){
            plugin.getLog().info("Mouse pressed at ({}, {}); time {}; # clicks: {}", e.getX(), e.getY(), lastMousePress, e.getClickCount());
        }
        else {
            plugin.getLog().debug("Mouse pressed at ({}, {}); time {}; # clicks: {}", e.getX(), e.getY(), lastMousePress, e.getClickCount());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lastMouseRelease = e.getWhen() - startTime;
        if (config.priority()){
            plugin.getLog().info("Mouse released at ({}, {}); time {}; # clicks: {}", e.getX(), e.getY(), lastMouseRelease, e.getClickCount());
        }
        else {
            plugin.getLog().debug("Mouse released at ({}, {}); time {}; # clicks: {}", e.getX(), e.getY(), lastMouseRelease, e.getClickCount());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (config.priority()){
            plugin.getLog().info("Mouse clicked at ({}, {}); time {}; # clicks: {}; debounce {}ms", e.getX(), e.getY(), e.getWhen() - startTime, e.getClickCount(), lastMouseRelease - lastMousePress);
            }
        else {
            plugin.getLog().debug("Mouse clicked at ({}, {}); time {}; # clicks: {}; debounce {}ms", e.getX(), e.getY(), e.getWhen() - startTime, e.getClickCount(), lastMouseRelease - lastMousePress);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

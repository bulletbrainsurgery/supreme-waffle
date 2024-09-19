package com.example;

import net.runelite.client.config.Config;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class MyKeyListener implements KeyListener {
    private long lastKeyPress;
    private long lastKeyRelease;
    private final long startTime;

    private final ClickLoggerPlugin plugin;
    private ClickLoggerConfig config;

    public MyKeyListener(ClickLoggerPlugin plugin, long startTime) {
        this.plugin = plugin;
        this.startTime = startTime;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        lastKeyPress = e.getWhen() - startTime;
        if (config.priority()) {
            plugin.getLog().info("Key press: keycode {} at {}", e.getKeyCode(), lastKeyPress);
        }
        else {
            plugin.getLog().debug("Key press: keycode {} at {}", e.getKeyCode(), lastKeyPress);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        lastKeyRelease = e.getWhen() - startTime;
        if (config.priority()) {
            plugin.getLog().info("Key release: keycode {} at {}", e.getKeyCode(), lastKeyRelease);
        }
        else {
            plugin.getLog().debug("Key release: keycode {} at {}", e.getKeyCode(), lastKeyRelease);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (config.priority()) {
            plugin.getLog().info("Key typed: char {} at {}; debounce {}ms", e.getKeyChar(), e.getWhen() - startTime, lastKeyRelease - lastKeyPress);
        }
        else {
            plugin.getLog().debug("Key typed: char {} at {}; debounce {}ms", e.getKeyChar(), e.getWhen() - startTime, lastKeyRelease - lastKeyPress);
        }
    }
}

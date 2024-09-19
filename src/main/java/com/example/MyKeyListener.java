package com.example;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class MyKeyListener implements KeyListener {
    private long lastKeyPress;
    private long lastKeyRelease;
    private final long startTime;

    private final ClickLogger plugin;

    public MyKeyListener(ClickLogger plugin, long startTime) {
        this.plugin = plugin;
        this.startTime = startTime;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        lastKeyPress = e.getWhen() - startTime;
        plugin.getLog().info("Key press: keycode " + e.getKeyCode() + " at " + lastKeyPress);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        lastKeyRelease = e.getWhen() - startTime;
        plugin.getLog().info("Key release: keycode " + e.getKeyCode() + " at " + lastKeyRelease);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        plugin.getLog().info("Key typed: key char " + e.getKeyChar() + " at " + (e.getWhen() - startTime) + "; debounce " + (lastKeyRelease - lastKeyPress) + "ms");
    }
}

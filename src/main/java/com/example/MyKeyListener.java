package com.example;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class MyKeyListener implements KeyListener {
    private long lastKeyPress;
    private long lastKeyRelease;
    private final long startTime;
    private final int startTick;

    private final ClickLoggerPlugin plugin;

    public MyKeyListener(ClickLoggerPlugin plugin, long startTime, int startTick) {
        this.plugin = plugin;
        this.startTime = startTime;
        this.startTick = startTick;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        lastKeyPress = e.getWhen() - startTime;
        String msg = String.format("Key press: keycode %s at %d", e.getKeyCode(),lastKeyPress);
        plugin.addEvent(new LogEvent(e.getWhen(), plugin.getGameTick() - startTick, msg));
        plugin.debugMsg(msg);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        lastKeyRelease = e.getWhen() - startTime;
        String msg = String.format("Key release: keycode %s at %d",e.getKeyCode(),lastKeyRelease);
        plugin.addEvent(new LogEvent(e.getWhen(), plugin.getGameTick() - startTick, msg));
        plugin.debugMsg(msg);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        String msg = String.format("Key typed: char %s at %d; debounce %dms", e.getKeyChar(), e.getWhen() - startTime, lastKeyRelease - lastKeyPress);
        plugin.addEvent(new LogEvent(e.getWhen(), plugin.getGameTick() - startTick, msg));
        plugin.debugMsg(msg);
    }
}

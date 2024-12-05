package com.example;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class MyMouseListener implements MouseListener {
    private long lastMousePress = -1;
    private long lastMouseRelease = -1;
    private final long startTime;
    private final int startTick;

    private final ClickLoggerPlugin plugin;

    public MyMouseListener(ClickLoggerPlugin plugin, long startTime, int startTick) {
        this.plugin = plugin;
        this.startTime = startTime;
        this.startTick = startTick;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        lastMousePress = e.getWhen() - startTime;
        String msg = String.format("Mouse pressed at (%d, %d); time %d; clicks %d", e.getX(), e.getY(), lastMousePress, e.getClickCount());
        plugin.addEvent(new LogEvent(e.getWhen(), plugin.getGameTick() - startTick, msg));
        plugin.debugMsg(msg);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        lastMouseRelease = e.getWhen() - startTime;
        String msg = String.format("Mouse released at (%d, %d); time %d; clicks %d", e.getX(), e.getY(), lastMouseRelease, e.getClickCount());
        plugin.addEvent(new LogEvent(e.getWhen(), plugin.getGameTick() - startTick, msg));
        plugin.debugMsg(msg);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        String msg = String.format("Mouse clicked at (%d, %d); time %d; clicks %d; debounce %dms", e.getX(), e.getY(), e.getWhen() - startTime, e.getClickCount(), lastMouseRelease - lastMousePress);
        plugin.addEvent(new LogEvent(e.getWhen(), plugin.getGameTick() - startTick, msg));
        plugin.debugMsg(msg);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}

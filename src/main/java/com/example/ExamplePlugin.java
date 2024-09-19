package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Slf4j
@PluginDescriptor(
	name = "Click Tracker",
	enabledByDefault = false
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	private MyKeyListener myKeyListener;
	private MyMouseListener myMouseListener;

	@Override
	protected void startUp()
	{
		log.info("Click tracker starting!");
		long startTime = System.currentTimeMillis();

		myKeyListener = new MyKeyListener(log, startTime);
		myMouseListener = new MyMouseListener(log, startTime);

		client.getCanvas().addKeyListener(myKeyListener);
		client.getCanvas().addMouseListener(myMouseListener);
	}

	@Override
	protected void shutDown()
	{
		log.info("Click tracker shutting down!");
		client.getCanvas().removeKeyListener(myKeyListener);
		client.getCanvas().removeMouseListener(myMouseListener);

	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e){
		MenuEntry menuEntry = e.getMenuEntry();
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}

class MyKeyListener implements KeyListener{
	private long lastKeyPress;
	private long lastKeyRelease;
	private final long startTime;

	private final Logger log;

	public MyKeyListener(Logger logger, long startTime){
		this.log = logger;
		this.startTime = startTime;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		lastKeyPress = e.getWhen() - startTime;
//		System.out.println("Key press: keycode "+e.getKeyCode()+ " at "+lastKeyPress);
		log.info("Key press: keycode "+e.getKeyCode()+ " at "+lastKeyPress);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		lastKeyRelease = e.getWhen() - startTime;
//		System.out.println("Key release: keycode "+e.getKeyCode()+ " at "+lastKeyRelease);
		log.info("Key release: keycode "+e.getKeyCode()+ " at "+lastKeyRelease);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		log.info("Key typed: key char "+e.getKeyChar()+ " at "+(e.getWhen() - startTime)+"; debounce "+(lastKeyRelease - lastKeyPress)+"ms");
	}
}

class MyMouseListener implements MouseListener{
	private long lastMousePress = -1;
	private long lastMouseRelease = -1;
	private long startTime;

	private final Logger log;

	public MyMouseListener(Logger logger, long startTime){
		this.log = logger;
		this.startTime = startTime;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastMousePress = e.getWhen() - startTime;
		log.info("Mouse pressed at ("+e.getX()+", "+e.getY()+"); time " + lastMousePress + "; # clicks: " + e.getClickCount());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastMouseRelease = e.getWhen() - startTime;
		log.info("Mouse released at ("+e.getX()+", "+e.getY()+"); time "+lastMouseRelease+"; # clicks: " + e.getClickCount());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		log.info("Mouse clicked at ("+e.getX()+", "+e.getY()+"); time "+(e.getWhen() - startTime)+"; # clicks: "+ e.getClickCount() + "; debounce "+(lastMouseRelease - lastMousePress)+"ms");
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
}
package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Slf4j
@PluginDescriptor(
	name = "Click Tracker"
)
public class ExamplePlugin extends Plugin implements MouseListener, KeyListener
{
	private long lastMousePress;
	private long lastMouseRelease;

	private long lastKeyPress;
	private long lastKeyRelease;

	private long startupDeltaTime;

	@Override
	protected void startUp()
	{
		startupDeltaTime = System.currentTimeMillis();

		lastMousePress = -1;
		lastMouseRelease = -1;
		lastKeyPress = -1;
		lastKeyRelease = -1;
	}

	@Override
	protected void shutDown()
	{
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastMousePress = e.getWhen() - startupDeltaTime;
		log.info("Mouse pressed at ("+e.getX()+", "+e.getY()+"); time " + lastMousePress + "; # clicks: " + e.getClickCount());


	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastMouseRelease = e.getWhen() - startupDeltaTime;
		log.info("Mouse released at ("+e.getX()+", "+e.getY()+"); time "+lastMouseRelease+"; # clicks: " + e.getClickCount());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		log.info("Mouse clicked at ("+e.getX()+", "+e.getY()+"); time "+(e.getWhen() - startupDeltaTime)+"; # clicks: "+ e.getClickCount() + "; debounce "+(lastMouseRelease - lastMousePress)+"ms");
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

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

	@Override
	public void keyPressed(KeyEvent e) {
		lastKeyPress = e.getWhen() - startupDeltaTime;
		log.info("Key press: keycode "+e.getKeyCode()+ " at "+lastKeyPress);

	}

	@Override
	public void keyReleased(KeyEvent e) {
		lastKeyRelease = e.getWhen() - startupDeltaTime;
		log.info("Key release: keycode "+e.getKeyCode()+ " at "+lastKeyRelease);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		log.info("Key typed: key char "+e.getKeyChar()+ " at "+(e.getWhen() - startupDeltaTime)+"; debounce "+(lastKeyRelease - lastKeyPress)+"ms");
	}
}

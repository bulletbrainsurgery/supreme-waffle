package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
@PluginDescriptor(
	name = "Click Tracker",
	enabledByDefault = false
)
public class ClickLogger extends Plugin
{
	@Inject
	private Client client;

	private MyKeyListener myKeyListener;
	private MyMouseListener myMouseListener;

	private final ArrayList<LogEntry> buffer = new ArrayList<>();

	@Override
	protected void startUp()
	{
		log.info("Click tracker starting!");
		long startTime = System.currentTimeMillis();

		myKeyListener = new MyKeyListener(this, startTime);
		myMouseListener = new MyMouseListener(this, startTime);

		client.getCanvas().addKeyListener(myKeyListener);
		client.getCanvas().addMouseListener(myMouseListener);
	}

	@Override
	protected void shutDown()
	{
		log.info("Click tracker shutting down!");
		client.getCanvas().removeKeyListener(myKeyListener);
		client.getCanvas().removeMouseListener(myMouseListener);
		writeLog();
	}

	Logger getLog() {
		return log;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e){
		MenuEntry menuEntry = e.getMenuEntry();
	}

	@Provides
	ClickLogConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClickLogConfig.class);
	}

	void appendLog(String log){

		if (buffer.size() >= 50){
			writeLog();
		}
	}

	void writeLog()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
			return;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		String time = formatter.format(LocalDateTime.now());

		toFile(player.getName(), fileName(time, true));

		buffer.clear();
	}

	private void toFile(String username, String filename, String contents) {
		try {
			Path path = Files.createDirectories(Paths.get(
					RUNELITE_DIR.getPath(), "click-logger", username));
			Files.write(path.resolve(filename), contents.getBytes());
		} catch (IOException ex) {
			log.debug("Error writing file: {}", ex.getMessage());
		}
	}
}

class LogEntry {
	String timestamp;
	int gameTick;
	String entry;
}

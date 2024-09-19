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
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;

import java.io.*;
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
public class ClickLoggerPlugin extends Plugin
{
	@Inject
	private Client client;

	private MyKeyListener myKeyListener;
	private MyMouseListener myMouseListener;

	private final ArrayList<LogEntry> buffer = new ArrayList<>();

	private BufferedWriter bw;

	@Override
	protected void startUp()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		String time = formatter.format(LocalDateTime.now());

		try {
			Path path = Files.createDirectories(Paths.get(
					RUNELITE_DIR.getPath(), "click-logger"));
			File file = new File(String.valueOf(path.resolve(time+".log")));
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			bw = new BufferedWriter(writer);
		}
		catch (IOException ex){
			log.warn("Error creating log file?: {}", ex.getMessage());
		}

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
		try {
			bw.close();
		} catch (IOException ex) {
			log.warn("[shutdown] error writing file: {}", ex.getMessage());
		}
	}

	@Inject
	protected void onClientShutdown(ClientShutdown event){
		log.info("Client close!");
		writeLog();
		try {
			bw.close();
		} catch (IOException ex) {
			log.warn("[clientclose] error writing file: {}", ex.getMessage());
		}
	}

	Logger getLog() {
		return log;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e){
		MenuEntry menuEntry = e.getMenuEntry();
	}

	@Provides
	ClickLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClickLoggerConfig.class);
	}

	void appendLog(LogEntry logEntry){
		buffer.add(logEntry);

		if (buffer.size() >= 100){
			writeLog();
		}
	}

	void writeLog()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
			return;

		try
		{
			for (LogEntry entry : buffer) {
				String toWrite = entry.gameTick + " ["+ entry.timestamp+"]: "+entry.event;
				bw.write(toWrite);
			}
			buffer.clear();
		} catch (IOException ex) {
			log.warn("Error writing file: {}", ex.getMessage());
		}

	}
}

class LogEntry {
	String timestamp;
	int gameTick;
	String event;
}

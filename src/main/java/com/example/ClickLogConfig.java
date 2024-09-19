package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("clicklogger")
public interface ClickLogConfig extends Config
{
	@ConfigItem(
		keyName = "priority",
		name = "Debug priority",
		description = "false = debug, true = info"
	)
	default boolean priority()
	{
		return false;
	}
}

package com.osrsevents;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("osrsevents")
public interface EventsConfig extends Config
{
	@ConfigItem(
		keyName = "runelite-token",
		name = "runelite-token",
		description = "Unique api token to provided"
	)
	default String runeliteToken()
	{
		return null;
	}

	@ConfigItem(
			keyName = "endpoint",
			name = "endpoint",
			description = "Endpoint to send data to"
	)
	default String apiEndpoint() {return null; }

	@ConfigItem
	(
		position = 1,
		keyName = "enableSending",
		name = "Enable Event Sending",
		description = "Master switch to enable the sending of data to 3rd party api service"
	)
	default boolean enableSending() { return true; }

	@ConfigItem
	(
		position = 2,
		keyName = "enableMonsterKills",
		name = "Enable Monster Kills",
		description = "If on, will send notifications about monster kills"
	)
	default boolean enableMonsterKill() { return true; }

	@ConfigItem(
			position = 3,
			keyName = "emitPlayerInfo",
			name = "Emit Player Info",
			description = "An optional addition of player information to each request"
	)
	default boolean emitAttachPlayerInfo() { return false; }


}

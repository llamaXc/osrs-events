package com.osrsevents;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.osrsevents.notifications.NpcKillNotification;

@Slf4j
@PluginDescriptor(
	name = "OSRSEvents"
)
public class EventsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EventsConfig config;

	private ApiManager apiManager;

	@Override
	protected void startUp() throws Exception
	{
		apiManager = new ApiManager(config, client);
	}

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived)
	{
		NpcKillNotification notification = new NpcKillNotification(npcLootReceived);
		apiManager.send(notification);
	}

	@Provides
	EventsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EventsConfig.class);
	}
}

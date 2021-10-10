package com.osrsevents;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.osrsevents.interfaces.ApiConnectable;
import com.osrsevents.interfaces.EventsConfig;
import com.osrsevents.notifications.*;
import com.osrsevents.pojos.BankItem;
import com.osrsevents.enums.MESSAGE_EVENT;
import com.osrsevents.pojos.QuestInfo;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.*;

@Slf4j
@PluginDescriptor(name = "OSRSEvents")
public class EventsPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private EventsConfig config;

	@Inject
	private ItemManager itemManager;

	private final int[] lastSkillLevels = new int[Skill.values().length - 1];
	private boolean hasTicked = false;
	private Quest[] lastQuestState;
	private boolean lastBankOpenStatus = false;
	private ItemContainer lastBankContainer;
	private MessageHandler messageHandler;

	@Override
	protected void startUp() {
		ApiConnectable apiManager = new ApiManager(config, client);
		messageHandler = new MessageHandler(apiManager);
	}

	@Subscribe
	public void onGameTick(final GameTick event) {
		if(!hasTicked){
			hasTicked = true;
			this.sendInitialLoginEvents();
		}

		//Detect any events which cause messages to be sent
		this.detectQuestEvents();
		this.detectBankWindowClosing();

		//Update the message handler to fire messages
		this.messageHandler.processGameTicks();
	}

	//Handle level/quest changes when playing on mobile, then re-logging into RuneLite.
	private void sendInitialLoginEvents(){
		this.createAndQueueLevelNotification(null, null);
	}

	private void detectQuestEvents(){
		if(lastQuestState == null){
			lastQuestState = Quest.values();
			this.createAndQueueQuestStatusNotification(null, null);
			return;
		}

		Quest[] curQuestState = Quest.values();
		for (int i = 0; i < curQuestState.length; i++){
			if(lastQuestState[i].getState(client) != curQuestState[i].getState(client)){
				String questName =curQuestState[i].getName();
				QuestState questState = curQuestState[i].getState(client);
				this.createAndQueueQuestStatusNotification(questName, questState);
			}
		}

		lastQuestState = curQuestState;
	}

	private void detectBankWindowClosing(){
		Widget con = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if(con != null) {
			lastBankOpenStatus = true;
			lastBankContainer = client.getItemContainer(InventoryID.BANK);
		}else if(lastBankOpenStatus){
			lastBankOpenStatus = false;
			this.createAndQueueBankNotification();
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event) {
		if (event.getItemContainer() == client.getItemContainer(InventoryID.EQUIPMENT)) {
			this.createAndQueueEquipmentNotification(event.getItemContainer().getItems());
		}

		if(event.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY)){
			this.createAndQueueInvoNotification(event.getItemContainer().getItems());
		}
	}

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived) {
		NpcKillNotification notification = new NpcKillNotification(npcLootReceived);
		messageHandler.insertMessage(MESSAGE_EVENT.LOOT, notification);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		Skill updatedSkill = statChanged.getSkill();
		int skillIdx = updatedSkill.ordinal();
		int lastLevel = lastSkillLevels[skillIdx];
		int currentLevel = client.getRealSkillLevel(updatedSkill);

		lastSkillLevels[skillIdx] = currentLevel;
		boolean didLevel = lastLevel != 0 && currentLevel > lastLevel;

		if(didLevel && hasTicked){
			this.createAndQueueLevelNotification(updatedSkill.getName(), currentLevel);
		}
	}

	private void createAndQueueBankNotification(){
		//Ensure bankContainer is valid
		if(lastBankContainer == null){
			return;
		}

		//Extract items and build List of bankItems for the API
		Item[] bankItems = lastBankContainer.getItems();
		List<BankItem> items = new ArrayList<>();
		int totalPrice = 0;

		for(int i = 0; i < bankItems.length; i++){
			Item curItem = bankItems[i];

			//Don't add empty/removed items, we only want the current items in the bank to be sent
			if(curItem.getId() <= 0){
				continue;
			}
			int quantity = curItem.getQuantity();
			int id = curItem.getId();
			int gePrice = quantity * itemManager.getItemComposition(id).getPrice();
			totalPrice += gePrice;
			BankItem item = new BankItem(i, quantity, id);
			items.add(item);
		}

		BankNotification bankNotification = new BankNotification(items, totalPrice);
		messageHandler.insertMessage(MESSAGE_EVENT.BANK, bankNotification);
	}

	private void createAndQueueLevelNotification(String name, Integer level){
		Map<String, Integer> levelMap = this.getLevelMap();
		LevelChangeNotification levelEvent = new LevelChangeNotification(name, level, levelMap);
		messageHandler.insertMessage(MESSAGE_EVENT.LEVEL, levelEvent);
	}

	private void createAndQueueQuestStatusNotification(String quest, QuestState state){
		List<QuestInfo> quests = this.getQuestInfoList();
		QuestChangeNotification questEvent = new QuestChangeNotification(quest, state, quests, client.getVar(VarPlayer.QUEST_POINTS));
		messageHandler.insertMessage(MESSAGE_EVENT.QUEST, questEvent);
	}

	private void createAndQueueInvoNotification(Item[] invoItems){
		Map<Integer, Item> items = new HashMap<>();
		for (int i = 0; i < invoItems.length; i++){
			if(invoItems[i].getId() > 0 && invoItems[i].getQuantity() > 0) {
				items.put(i, invoItems[i]);
			}
		}

		InventorySlotsNotification invoEvent = new InventorySlotsNotification(items);
		messageHandler.insertMessage(MESSAGE_EVENT.INVO, invoEvent);
	}

	private void createAndQueueEquipmentNotification(Item[] equippedItems){
		EquipmentInventorySlot[] slots = EquipmentInventorySlot.values();
		Map<EquipmentInventorySlot, Item> equipped = new HashMap<>();

		//Extract each slot and item being worn in the slot
		for(int i = 0; i < slots.length ; i++){
			if(slots[i].getSlotIdx() >= equippedItems.length){
				continue;
			}

			Item item = equippedItems[slots[i].getSlotIdx()];

			//Ensure there is a valid item being worn, else don't attach it
			if(item.getId() > 0 && item.getQuantity() > 0) {
				equipped.put(slots[i], item);
			}
		}

		EquipSlotsNotification equipEvent = new EquipSlotsNotification(equipped);
		messageHandler.insertMessage(MESSAGE_EVENT.EQUIPMENT, equipEvent);
	}

	private Map<String, Integer> getLevelMap(){
		Map<String, Integer> levelMap = new HashMap<>();
		Skill[] skills = Skill.values();
		for (Skill iskill : skills){
			levelMap.put(iskill.getName(), client.getRealSkillLevel(iskill));
		}
		return levelMap;
	}

	private List<QuestInfo> getQuestInfoList(){
		ArrayList<QuestInfo> questInfo = new ArrayList<>();
		Quest[] quests = Quest.values();
		for (Quest quest : quests){
			questInfo.add(new QuestInfo(quest.getName(), quest.getId(), Quest.valueOf(quest.toString()).getState(client)));
		}
		return questInfo;
	}

	@Provides
	EventsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(EventsConfig.class);
	}
}

package com.osrsevents;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.osrsevents.enums.LOGIN_STATE;
import com.osrsevents.interfaces.ApiConnectable;
import com.osrsevents.interfaces.EventsConfig;
import com.osrsevents.notifications.*;
import com.osrsevents.enums.MESSAGE_EVENT;
import com.osrsevents.pojos.BankItem;
import com.osrsevents.pojos.QuestInfo;
import com.osrsevents.utils.CommonUtility;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Slf4j
@PluginDescriptor(name = "OSRSEvents")
public class EventsPlugin extends Plugin {

	@Inject
	private OkHttpClient httpClient;

	@Inject
	private Client client;

	@Inject
	private EventsConfig config;

	@Inject
	private ItemManager itemManager;

	private int[] lastSkillLevels;
	private boolean hasTicked;
	private boolean lastBankOpenStatus ;
	private boolean hasLoggedIn = false;
	private QuestState[] lastQuestStates;
	private Quest[] quests;
	private Item[] lastInvoItems;
	private ItemContainer lastBankContainer;

	private MessageHandler messageHandler;

	private static final Logger logger = LoggerFactory.getLogger(EventsPlugin.class);

	@Override
	protected void startUp() {
		logger.debug("Starting up");
		ApiConnectable apiManager = new ApiManager(config, client, httpClient);
		messageHandler = new MessageHandler(apiManager);

		this.initializeSessionVariables();
	}

	@Subscribe
	public void onGameTick(final GameTick event) {
		if(!hasTicked){
			logger.debug("First game tick detected, handle initial events");
			hasTicked = true;
			this.populateCurrentQuests();
			this.sendInitialLoginEvents();
		}

		this.detectQuestEvents();
		this.detectBankWindowClosing();

		this.messageHandler.processGameTicks();
	}

	private void initializeSessionVariables(){
		lastSkillLevels = new int[Skill.values().length - 1];
		hasTicked = false;
		lastQuestStates = new QuestState[Quest.values().length];
		lastBankOpenStatus = false;
		quests = Quest.values();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged state){
		if(state.getGameState() == GameState.LOGIN_SCREEN){
			if(hasLoggedIn == true){
				LoginNotification loggedOut = new LoginNotification(LOGIN_STATE.LOGGED_OUT);
				messageHandler.sendEventNow(MESSAGE_EVENT.LOGIN, loggedOut);
				this.hasLoggedIn = false;
			}

			logger.debug("Player is on login screen, setting up session variables");
			this.initializeSessionVariables();
		}

		if(state.getGameState() == GameState.LOGGED_IN){
			this.hasLoggedIn = true;
			LoginNotification loggedIn = new LoginNotification((LOGIN_STATE.LOGGED_IN));
			messageHandler.sendEventNow(MESSAGE_EVENT.LOGIN, loggedIn);
		}
	}

	private void sendInitialLoginEvents(){
		logger.debug("Sending initial level and quest notifications");
		this.createAndSendLevelNotification(null, null);
		this.createAndSendQuestNotification(null, null);
	}

	private void populateCurrentQuests(){
		logger.debug("Populating quest states with latest player states");
		for (int i = 0; i < quests.length; i++){
			QuestState currentQuestState = quests[i].getState(client);
			lastQuestStates[i] = currentQuestState;
		}
	}

	private void detectQuestEvents(){
		boolean questChangeFound = false;
		for (int i = 0; i < quests.length; i++){
			if(lastQuestStates[i] != quests[i].getState(client)){
				String questName =quests[i].getName();
				QuestState currentQuestState = quests[i].getState(client);
				lastQuestStates[i] = currentQuestState;
				if(questChangeFound == false) {
					logger.debug("Detected new quest state: " + questName + " is now: " + currentQuestState.toString());
					this.createAndSendQuestNotification(questName, currentQuestState);
					questChangeFound = true;
				}
			}
		}
	}

	private void detectBankWindowClosing(){
		Widget con = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if(con != null) {
			lastBankOpenStatus = true;
			lastBankContainer = client.getItemContainer(InventoryID.BANK);
		}else if(lastBankOpenStatus){
			lastBankOpenStatus = false;
			logger.debug("Detected closing of bank window. Preparing bank notification");
			this.createAndQueueBankNotification();
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event) {
		if (event.getItemContainer() == client.getItemContainer(InventoryID.EQUIPMENT)) {
			logger.debug("Detected ItemContainer change for EQUIPMENT, preparing to queue event");
			this.createAndQueueEquipmentNotification(event.getItemContainer().getItems());
		}

		if(event.getItemContainer() == client.getItemContainer(InventoryID.INVENTORY)){

			//When first logging in, set the inventory container and fire off an event to load first inventory
			if(lastInvoItems == null){
				logger.debug("First time seeing inventory event, prepare to create first Inventory Notification");
				lastInvoItems = event.getItemContainer().getItems();
				this.createAndQueueInvoNotification(lastInvoItems);
				return;
			}

			ItemContainer currentContainer = event.getItemContainer();
			boolean containersEqual = CommonUtility.areItemContainerEqual(lastInvoItems, currentContainer.getItems());
			if(containersEqual == false) {
				logger.debug("Detected changed inventory, prepare to create latest Inventory Notification");
				this.createAndQueueInvoNotification(event.getItemContainer().getItems());
				lastInvoItems = currentContainer.getItems();
			}
		}
	}

	@Subscribe
	public void onNpcLootReceived(final NpcLootReceived npcLootReceived) {
		logger.debug("Detected npcLootReceived from npc with id: " + npcLootReceived.getNpc().getId() +" , preparing to send event");
		this.createAndSendLootNotification(npcLootReceived);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		Skill updatedSkill = statChanged.getSkill();
		int skillIdx = updatedSkill.ordinal();
		int lastLevel = lastSkillLevels[skillIdx];
		int currentLevel = client.getRealSkillLevel(updatedSkill);

		lastSkillLevels[skillIdx] = currentLevel;
		boolean didLevel = lastLevel != 0 && currentLevel > lastLevel;

		//Only send level stat changes after initial events have sent
		if(didLevel && hasTicked){
			logger.debug("Level up detected, preparing to queue event: " + updatedSkill.getName() + " lvl: " +  currentLevel);
			this.createAndSendLevelNotification(updatedSkill.getName(), currentLevel);
		}
	}

	private void createAndSendLootNotification(NpcLootReceived lootReceived){
		int geTotalPrice = 0;
		int npcId = lootReceived.getNpc().getId();
		List<Item> items = new ArrayList<>();
		for(ItemStack item : lootReceived.getItems()){
			items.add(new Item(item.getId(), item.getQuantity()));
			geTotalPrice += item.getQuantity() * itemManager.getItemComposition(item.getId()).getPrice();
		}
		NpcKillNotification notification = new NpcKillNotification(npcId, items, geTotalPrice);
		messageHandler.sendEventNow(MESSAGE_EVENT.LOOT, notification);
	}

	private void createAndQueueBankNotification(){
		//Ensure bankContainer is valid
		if(lastBankContainer == null){
			return;
		}

		Item[] bankItems = lastBankContainer.getItems();
		List<BankItem> items = new ArrayList<>();
		int totalPrice = 0;

		for(Item bankItem : bankItems){
			int id = bankItem.getId();

			// Skip invalid item ids
			if (id <= -1){
				continue;
			}

			int quantity = bankItem.getQuantity();
			ItemComposition itemComp = itemManager.getItemComposition(id);

			// Handle placeholder quantity
			boolean isPlaceholder = itemComp.getPlaceholderTemplateId() == 14401;
			if (isPlaceholder) {
				quantity = 0;
			}

			int gePrice = quantity * itemComp.getPrice();
			totalPrice += gePrice;

			BankItem bankItemToAdd = new BankItem(id, quantity);
			items.add(bankItemToAdd);
		}

		logger.debug("Preparing to send bank notification to message handler");
		BankNotification bankNotification = new BankNotification(items, totalPrice);
		messageHandler.updateLatestEvent(MESSAGE_EVENT.BANK, bankNotification);
	}

	private void createAndSendLevelNotification(String name, Integer level){
		logger.debug("Preparing to send level notification to message handler");
		Map<String, Integer> levelMap = this.getLevelMap();
		LevelChangeNotification levelEvent = new LevelChangeNotification(name, level, levelMap);
		messageHandler.sendEventNow(MESSAGE_EVENT.LEVEL, levelEvent);
	}

	private void createAndSendQuestNotification(String quest, QuestState state){
		logger.debug("Preparing to send quest status to message handler");
		List<QuestInfo> quests = this.getQuestInfoList();
		int QuestPointsVarp = VarPlayer.QUEST_POINTS.getId();
		QuestChangeNotification questEvent = new QuestChangeNotification(quest, state, quests, client.getVarpValue(QuestPointsVarp));
		messageHandler.sendEventNow(MESSAGE_EVENT.QUEST, questEvent);
	}

	private void createAndQueueInvoNotification(Item[] invoItems){
		logger.debug("Preparing to send inventory notification to message handler");
		List<Item> inventoryItems = new ArrayList<>();
		final int MAX_INVO_SLOTS = 28;
		int geTotalPrice = 0;
		for (int i = 0; i < MAX_INVO_SLOTS; i++){
			if(i > invoItems.length - 1){
				inventoryItems.add(new Item(0,0));
			}else if(invoItems[i].getId() > 0 && invoItems[i].getQuantity() > 0) {
				geTotalPrice += invoItems[i].getQuantity() * itemManager.getItemComposition(invoItems[i].getId()).getPrice();
				inventoryItems.add(invoItems[i]);
			}else{
				inventoryItems.add(new Item(0, 0));
			}
		}

		InventorySlotsNotification invoEvent = new InventorySlotsNotification(inventoryItems, geTotalPrice);
		messageHandler.updateLatestEvent(MESSAGE_EVENT.INVO, invoEvent);
	}

	private void createAndQueueEquipmentNotification(Item[] equippedItems){
		logger.debug("Preparing to send equipment notification to message handler");
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
		messageHandler.updateLatestEvent(MESSAGE_EVENT.EQUIPMENT, equipEvent);
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
		for (Quest quest : quests){
			questInfo.add(new QuestInfo(quest.getName(), quest.getId(), quest.getState(client)));
		}
		return questInfo;
	}

	@Provides
	EventsConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(EventsConfig.class);
	}
}

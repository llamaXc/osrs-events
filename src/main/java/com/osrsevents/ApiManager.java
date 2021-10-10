package com.osrsevents;

import com.osrsevents.interfaces.ApiConnectable;
import com.osrsevents.interfaces.EventsConfig;
import com.osrsevents.interfaces.Sendable;
import com.osrsevents.notifications.*;
import net.runelite.api.Client;

import okhttp3.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ApiManager implements ApiConnectable {

    private static final Logger logger = LoggerFactory.getLogger(ApiManager.class);
    public static final String NPC_KILL_ENDPOINT = "npc_kill/";
    public static final String LEVEL_CHANGE_ENDPOINT = "level_change/";
    public static final String QUEST_POINT_ENDPOINT = "quest_change/";
    public static final String EQUIPPED_ITEMS_ENDPOINT = "equipped_items/";
    public static final String INVENTORY_SLOT_ENDPOINT = "inventory_items/";
    public static final String BANK_ENDPOINT = "bank/";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private EventsConfig config;
    private final Client client;

    public ApiManager(EventsConfig config, Client client){
        this.config = config;
        this.client = client;
    }

    private void send(Sendable event){

        if (!canSend(event)){
            return;
        }

        EventWrapper eventWrapper = event.getEventWrapper();

        //Extract displayName and combatLevel to send with the event
        if(config.emitAttachPlayerInfo() && client.getLocalPlayer() != null){
            eventWrapper.setPlayerInfo(client.getLocalPlayer().getName(), client.getLocalPlayer().getCombatLevel(), client.getLocalPlayer().getWorldLocation());
        }

        logger.info(eventWrapper.getJsonPayload()) ;

        OkHttpClient client = new OkHttpClient();
        Request getRequest = new Request.Builder()
                .url(config.apiEndpoint() + event.getApiEndpoint())
                .header("Authorization", "Bearer: " + config.bearerToken())
                .post(RequestBody.create(JSON, eventWrapper.getJsonPayload()))
                .build();

        client.newCall(getRequest).enqueue(new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               e.printStackTrace();
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
               logger.info("Got response");
           }
       });
    }

    public boolean canSend(Sendable event){
        String endpoint = config.apiEndpoint();
        String token = config.bearerToken();
        if(endpoint == null){
            logger.warn("Failed to send, please check endpoint: " + endpoint + " and token: " + token);
            return false;
        }

        if (event instanceof BankNotification){
            return config.emitBankItems();
        }

        if (event instanceof InventorySlotsNotification){
            return config.emitInventory();
        }

        if (event instanceof EquipSlotsNotification){
            return config.emitEquippedItems();
        }

        if (event instanceof LevelChangeNotification){
            return config.enableLevelChange();
        }

        if (event instanceof NpcKillNotification){
            return config.enableMonsterKill();
        }

        if (event instanceof QuestChangeNotification){
            return config.emitQuestInfo();
        }

        return true;
    }

    @Override
    public void init() {
        System.out.println("Initing sendable interface");
    }

    @Override
    public void send(List<Sendable> messages) {
        for(Sendable message : messages){
            this.send(message);
        }
    }
}

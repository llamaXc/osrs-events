package com.osrsevents;

import com.osrsevents.interfaces.ApiConnectable;
import com.osrsevents.interfaces.EventsConfig;
import com.osrsevents.interfaces.Sendable;
import com.osrsevents.notifications.*;
import net.runelite.api.Client;
import java.util.UUID;

import net.runelite.api.VarPlayer;
import okhttp3.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class ApiManager implements ApiConnectable {

    private @Inject OkHttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(ApiManager.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private EventsConfig config;
    private final Client client;

    public ApiManager(EventsConfig config, Client client, OkHttpClient httpClient){
        this.config = config;
        this.client = client;
        this.httpClient = httpClient;
        logger.debug("Created ApiConnectable ApiManager");
    }

    private void sendHttpPost(Sendable event){

        if (!canSend(event)){
            return;
        }

        EventWrapper eventWrapper = event.getEventWrapper();

        if(config.emitAttachPlayerInfo() && client.getLocalPlayer() != null){
            eventWrapper.setPlayerInfo(client.getLocalPlayer().getName(), client.getLocalPlayer().getCombatLevel(), client.getVarpValue(VarPlayer.MEMBERSHIP_DAYS), client.getLocalPlayer().getWorldLocation());
        }

        UUID uuid = UUID.randomUUID();
        logger.debug("Sending POST request to: " + event.getApiEndpoint());
        logger.debug("UUID: " + uuid.toString());
        logger.debug("Bearer: " + config.bearerToken());
        logger.debug("JSON of event: " + eventWrapper.getJsonPayload());

        Request getRequest = new Request.Builder()
                .url(config.apiEndpoint() + event.getApiEndpoint())
                .header("Authorization", "Bearer: " + config.bearerToken())
                .header("X-Request-Id", uuid.toString())
                .post(RequestBody.create(JSON, eventWrapper.getJsonPayload()))
                .build();

        this.httpClient.newCall(getRequest).enqueue(new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {
               e.printStackTrace();
           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {
               logger.debug("Got response from: " + event.getApiEndpoint());
               logger.debug(response.body().toString());
               response.close();
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

        if (event instanceof LoginNotification){
            return config.emitLoginState();
        }

        return true;
    }

    @Override
    public void init() {
    }

    @Override
    public void send(Sendable message){
        this.sendHttpPost(message);
    }

    @Override
    public void send(List<Sendable> messages) {
        for(Sendable message : messages){
            this.sendHttpPost(message);
        }
    }
}

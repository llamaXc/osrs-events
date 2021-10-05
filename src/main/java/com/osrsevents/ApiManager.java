package com.osrsevents;

import net.runelite.api.Client;
import com.osrsevents.notifications.EventWrapper;
import com.osrsevents.notifications.NpcKillNotification;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.osrsevents.notifications.Sendable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiManager {

    private static final Logger logger = LoggerFactory.getLogger(ApiManager.class);
    public static final String NPC_KILL_ENDPOINT = "npc_kill/";

    private EventsConfig config;

    private final Client client;

    public ApiManager(EventsConfig config, Client client){
        this.config = config;
        this.client = client;
    }

    // Async send event to api for given notification
    public void send(Sendable event){

        if (!canSend(event)){
            return;
        }

        //EventWrapper to represent the sendable event
        EventWrapper eventWrapper = event.getEventWrapper();

        //Extract displayName and combatLevel to send with the event
        if(config.emitAttachPlayerInfo() && client.getLocalPlayer() != null){
            eventWrapper.setPlayerInfo(client.getLocalPlayer().getName(), client.getLocalPlayer().getCombatLevel());
        }

        //Get the JSON version of the event to send
        String jsonPayload = eventWrapper.getJsonPayload();

        //Build and send request
        URI apiUri = buildUri(event.getApiEndpoint());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(apiUri)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("runelite-token", config.runeliteToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();


        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println);
    }

    public URI buildUri(String eventEndpoint){
        return URI.create(config.apiEndpoint() + eventEndpoint);
    }

    public boolean canSend(Sendable event){
        String endpoint = config.apiEndpoint();
        String token = config.runeliteToken();
        if(token == null || endpoint == null){
            logger.warn("Failed to send, please check endpoint: " + endpoint + " and token: " + token);
            return false;
        }

        if (event instanceof NpcKillNotification){
            if(config.enableMonsterKill() == false){
                logger.debug("Npc kills are disabled, not sending this message");
                return false;
            }
        }

        return true;
    }

}

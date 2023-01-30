package com.osrsevents.notifications;

import com.google.gson.Gson;
import com.osrsevents.pojos.PlayerInfo;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import java.time.Instant;

public class EventWrapper {

    @Inject
    private Gson gson;

    //Fields included in serialized event being transmitted to API
    public PlayerInfo playerInfo;
    public Object data;
    public long timestamp;

    public EventWrapper(Object data){
        this.data = data;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public String getJsonPayload(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public void setPlayerInfo(String displayName, int combatLevel, WorldPoint pos){
        playerInfo = new PlayerInfo(displayName, combatLevel, pos);
    }
}


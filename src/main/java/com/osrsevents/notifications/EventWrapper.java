package com.osrsevents.notifications;

import com.google.gson.Gson;
import com.osrsevents.pojos.PlayerInfo;
import net.runelite.api.coords.WorldPoint;

import java.time.Instant;

public class EventWrapper {

    protected static Gson gson = new Gson();

    //Fields included in serialized event being transmitted to API
    public PlayerInfo playerInfo;
    public long timestamp;
    public Object data;

    public EventWrapper(Object data){
        this.data = data;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public String getJsonPayload(){
        return gson.toJson(this);
    }

    public void setPlayerInfo(String displayName, int combatLevel, WorldPoint pos){
        playerInfo = new PlayerInfo(displayName, combatLevel, pos);
    }
}


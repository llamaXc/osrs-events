package com.osrsevents.notifications;

import com.google.gson.Gson;

import com.osrsevents.pojos.PlayerInfo;
import net.runelite.api.coords.WorldPoint;
import net.runelite.http.api.RuneLiteAPI;

import java.time.Instant;

public class EventWrapper {
    public PlayerInfo playerInfo;
    public Object data;
    public long timestamp;

    public EventWrapper(Object data) {
        this.data = data;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public String getJsonPayload() {
        return RuneLiteAPI.GSON.toJson(this);
    }

    public void setPlayerInfo(String displayName, int combatLevel, int membershipDays, WorldPoint pos){
        playerInfo = new PlayerInfo(displayName, combatLevel, membershipDays, pos);
    }
}


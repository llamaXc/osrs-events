package com.osrsevents.notifications;

import com.osrsevents.ApiManager;
import com.osrsevents.interfaces.Sendable;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Item;

import java.util.Map;

public class InventorySlotsNotification implements Sendable {
    private static final String API_ENDPOINT = ApiManager.INVENTORY_SLOT_ENDPOINT;

    public InventorySlotsNotification(Map<Integer, Item> items){
        setInventory(items);
    }

    @Getter
    @Setter
    private Map<Integer, Item> inventory;
    @Override
    public EventWrapper getEventWrapper() {
        return new EventWrapper(this);
    }

    @Override
    public String getApiEndpoint() {
        return API_ENDPOINT;
    }
}

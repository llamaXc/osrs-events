package com.osrsevents.notifications;

import com.osrsevents.ApiManager;
import com.osrsevents.interfaces.Sendable;
import com.osrsevents.utils.Endpoint;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Item;

import java.util.List;
import java.util.Map;

public class InventorySlotsNotification implements Sendable {
    private static final String API_ENDPOINT = Endpoint.INVENTORY_SLOT_ENDPOINT;

    public InventorySlotsNotification(List<Item> items, int gePrice){
        setInventory(items);
        setGePrice(gePrice);
    }

    @Getter
    @Setter
    private int gePrice;

    @Getter
    @Setter
    private List<Item> inventory;
    @Override
    public EventWrapper getEventWrapper() {
        return new EventWrapper(this);
    }

    @Override
    public String getApiEndpoint() {
        return API_ENDPOINT;
    }
}

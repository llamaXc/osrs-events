package com.osrsevents.notifications;

import com.osrsevents.ApiManager;
import com.osrsevents.interfaces.Sendable;
import com.osrsevents.utils.Endpoint;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Item;

import java.util.List;

public class BankNotification implements Sendable {

    private static final String API_ENDPOINT = Endpoint.BANK_ENDPOINT;

    public BankNotification(List<Item> items, int totalPrice){
        setItems(items);
        setValue(totalPrice);
    }

    @Getter
    @Setter
    private int value;

    @Getter
    @Setter
    private List<Item> items;

    @Override
    public EventWrapper getEventWrapper() {
        return new EventWrapper(this);
    }

    @Override
    public String getApiEndpoint() {
        return API_ENDPOINT;
    }
}
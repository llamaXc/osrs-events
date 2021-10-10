package com.osrsevents.notifications;

import com.osrsevents.ApiManager;
import com.osrsevents.interfaces.Sendable;
import com.osrsevents.pojos.BankItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class BankNotification implements Sendable {

    private static final String API_ENDPOINT = ApiManager.BANK_ENDPOINT;

    public BankNotification(List<BankItem> items, int totalPrice){
        setItems(items);
        setValue(totalPrice);
    }

    @Getter
    @Setter
    private int value;

    @Getter
    @Setter
    private List<BankItem> items;

    @Override
    public EventWrapper getEventWrapper() {
        return new EventWrapper(this);
    }

    @Override
    public String getApiEndpoint() {
        return API_ENDPOINT;
    }
}

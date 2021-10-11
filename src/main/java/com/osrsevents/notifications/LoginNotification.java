package com.osrsevents.notifications;

import com.osrsevents.ApiManager;
import com.osrsevents.enums.LOGIN_STATE;
import com.osrsevents.interfaces.Sendable;
import com.osrsevents.utils.Endpoint;
import lombok.Getter;
import lombok.Setter;

public class LoginNotification implements Sendable {
    private static final String API_ENDPOINT = Endpoint.LOGIN_NOTIFICATION;

    public LoginNotification(LOGIN_STATE state){
        this.setState(state);
    }

    @Getter
    @Setter
    private LOGIN_STATE state;

    @Override
    public EventWrapper getEventWrapper() {
        return new EventWrapper(this);
    }

    @Override
    public String getApiEndpoint() {
        return API_ENDPOINT;
    }
}

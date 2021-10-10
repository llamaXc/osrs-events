package com.osrsevents.interfaces;

import com.osrsevents.notifications.EventWrapper;

public interface Sendable {
    public abstract EventWrapper getEventWrapper();
    public abstract String getApiEndpoint();
}

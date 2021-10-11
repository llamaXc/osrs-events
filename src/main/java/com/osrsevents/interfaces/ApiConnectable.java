package com.osrsevents.interfaces;

import com.osrsevents.interfaces.Sendable;

import java.util.List;

public interface ApiConnectable {
    void init();
    void send(List<Sendable> messages);
    void send(Sendable message);
}

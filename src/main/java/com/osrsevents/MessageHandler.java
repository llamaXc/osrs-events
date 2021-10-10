package com.osrsevents;

import com.osrsevents.enums.MESSAGE_EVENT;
import com.osrsevents.interfaces.ApiConnectable;
import com.osrsevents.interfaces.Sendable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler{
    private Map<MESSAGE_EVENT, MessageQueue> eventQueue;
    private ApiConnectable apiConnection;

    public MessageHandler(ApiConnectable apiConnection){
        this.eventQueue = new HashMap<>();
        this.apiConnection = apiConnection;

        //Events which occur frequently and should care about latest
        eventQueue.put(MESSAGE_EVENT.BANK, new MessageQueue(true, 20));
        eventQueue.put(MESSAGE_EVENT.INVO, new MessageQueue(true, 20));
        eventQueue.put(MESSAGE_EVENT.EQUIPMENT, new MessageQueue(true, 20));

        //Events which are important to state and are sent when ever they happen
        eventQueue.put(MESSAGE_EVENT.LEVEL, new MessageQueue(false, 0));
        eventQueue.put(MESSAGE_EVENT.QUEST, new MessageQueue(false, 0));
        eventQueue.put(MESSAGE_EVENT.LOOT, new MessageQueue(false, 0));
    }

    private List<Sendable> getMessagesFromQueues(){
        List<Sendable> messages = new ArrayList<>();
        for(MessageQueue queue : eventQueue.values()){
            messages.addAll(queue.getMessagesToSend());
        }
        if(messages.size() > 0){
            System.out.println("== getMessagesFromQueues== messages length: " + messages.size());
        }

        return messages;
    }
    public void processGameTicks(){
        for(MessageQueue queue : eventQueue.values()){
            queue.processGameTick();
        }

        List<Sendable> messagesToSend = getMessagesFromQueues();
        if(messagesToSend.size() > 0){
            apiConnection.send(messagesToSend);
        }
    }

    public void insertMessage(MESSAGE_EVENT eventType, Sendable event){
        System.out.println("Inserting: " + eventType.toString() + " into message queue");
        try {
           eventQueue.get(eventType).addMessage(event);
        }catch(Exception ex){
            System.out.println("ERROR");
            System.out.println(ex.getMessage());
        }
    }
}

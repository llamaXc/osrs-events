package com.osrsevents;

import com.osrsevents.interfaces.Sendable;

import java.util.ArrayList;
import java.util.List;

public class MessageQueue {

    private boolean canSend = false;
    private boolean trackLatest = false;
    private List<Sendable> messages = new ArrayList<>();
    private int gameTicksBetweenFrequentActions = 0;
    private int gameTicksSinceSending = 0;

    public MessageQueue(boolean trackLatest, int gameTicksBetweenFrequentActions){
        this.trackLatest = trackLatest;
        this.gameTicksBetweenFrequentActions = gameTicksBetweenFrequentActions;
        this.canSend = true;
    }

    public void processGameTick(){
        //Update send clock if this queue is only tracking the latest queue message
        if(gameTicksBetweenFrequentActions > 0 && gameTicksSinceSending < gameTicksBetweenFrequentActions){
            gameTicksSinceSending++;
        }

        //Update send clock if this queue is only tracking the latest queue message
        if(gameTicksBetweenFrequentActions > 0 && canSend == false && gameTicksSinceSending > gameTicksBetweenFrequentActions){
            this.canSend = true;
            gameTicksSinceSending = 0;
        }else if(gameTicksBetweenFrequentActions == 0){  /* If this is a normal message queue, we can always send!*/
            this.canSend = true;
        }

    }

    public void addMessage(Sendable message){
        if(trackLatest){
            System.out.println("\tinserting into first position, only tracking 1");
            if(messages.size() == 0){
                messages.add(message);
            }else {
                messages.set(0, message);
            }
        }else{
            System.out.println("\tadding to all messages");
            messages.add(message);
        }
    }

    public List<Sendable> getMessagesToSend(){
        List<Sendable> messagesToSend = new ArrayList<>();
        if(messages.size() > 0){
            System.out.println("== Messages are present to send: canSend = " + this.canSend + " ticks= " + this.gameTicksSinceSending);
        }
        if(canSend || trackLatest == false){
            while(messages.size() > 0){
                messagesToSend.add(messages.remove(0));
            }

            //Mark these message as sent, so need to wait for game ticks to pass for frequent actions to be sent.
            if(messagesToSend.size() > 0) {
                canSend = false;
                this.gameTicksSinceSending = 0;
            }
        }
        System.out.println("Getting messages to send: found: " + messagesToSend.size());
        return messagesToSend;
    }



}

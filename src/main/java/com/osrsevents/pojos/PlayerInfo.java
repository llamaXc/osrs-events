package com.osrsevents.pojos;

import lombok.Getter;
import lombok.Setter;

public class PlayerInfo {
    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private int combatLevel;

    public PlayerInfo(String username, int combatLevel){
        this.username = username;
        this.combatLevel = combatLevel;
    }

}

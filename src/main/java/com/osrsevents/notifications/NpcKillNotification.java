package com.osrsevents.notifications;

import com.osrsevents.ApiManager;
import com.osrsevents.interfaces.Sendable;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import com.osrsevents.pojos.LootItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NpcKillNotification implements Sendable {

    private final static String API_ENDPOINT = ApiManager.NPC_KILL_ENDPOINT;

    public NpcKillNotification(NpcLootReceived npcLoot){
        super();

        setNpcId(npcLoot.getNpc().getId());
        Collection<ItemStack> droppedItems = npcLoot.getItems();
        items = new ArrayList<>();
        for(ItemStack stack : droppedItems){
            LootItem loot = new LootItem(stack.getId(), stack.getQuantity());
            items.add(loot);
        }
    }

    public String getApiEndpoint(){
        return API_ENDPOINT;
    }
    public EventWrapper getEventWrapper(){ return new EventWrapper(this); }

    @Getter
    @Setter
    private int npcId;

    @Getter
    @Setter
    private List<LootItem> items;
}

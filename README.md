# OSRSEvents
Easily export your game data to an api of your choice. View the supported endpoints below and get started today.

##Security & Privacy
This plugin is setup by the user. You choose where your data goes, and have varying levels of configuration.
#### Outline of data being shared
- `Loot stacks when killing npcs`: Npc info and item information
- `Your displayName and combatLevel`: only if the Checkbox `Attach Player Info` is checked
- You have the ability to turn off all/or any out-going messages.

## How to addon/extend this plugin
1. Think of an event you want to export
2. Create a new class in com.osrsevents

## Config Options Panel : In Runelite
#### Input Boxes
- `runelite-token` an optional token sent in the header to be used by a server
- `endpoint` the base endpoint for the server (Example: `http://localhost:5000/api/`) *required*
        - *Note*: The HttpRequest uses `URI.create()` to generate the endpoint. Ensure it is a proper URI or else no data will be sent. 
        
#### Checkboxes
- `Enable Event Sending`: Master toggle, when off no events will send.
- `Enable Npc Kills`: When on, Npc Kill Events will be sent.
- `Attach Player Info`: When on, displayName and combatLevel will be added to each event

## Supported Endpoints
*Each notification has its own constant endpoint defined in `ApiManager.java`*
 - `NpcKillNotification` sends to  `/npc_kill/`
 
 - TODO: Possible events: level ups, quest completes, login states, map positions.
 - TODO: more events coming soon. Make PR for any event you want to see.
## Event Payload Structure
####Header Fields
- `runelite-token` an optional header token to represent a user.

#### Body Fields
   Every event will be an EventWrapper.java class that is serialized. Below is an example of a `npc_kill` that is serialized and being sent.
   - `playerInfo` is an optional supplement and will be null if `Attach Player Info` is not checked in the Config Panel
   - `timestamp` unix epoch time of when the event was sent
   - `data` is required and contains the serialized json of the event (NpcKillNotification, etc)
   
```
//Event for Npc Kill with attachPlayerInfo option true.
{
    //When attachPlayerInfo is false, no playerInfo data is added
    playerInfo: {
        username: String of in-game name,
        combatLevel: Integer of current combat level
    },
    
    //Unix epoch seconds of the event
    timestamp: 4503938493

    //Each event will have a data dictonary, which is the serialized event. This example shows an NpcKill event
    data: {
        npcId: Integer of RuneScape monster id,
        items: [
            {
                itemId: Integer of Runescape item id,
                quantity: Integer of quantity dropped
            }
        ]
    }
}
```
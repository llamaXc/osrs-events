# OSRSEvents
Easily export your game data to an api of your choice. View the supported endpoints below and get started today.

## Security & Privacy
This plugin is setup by the user. You choose where your data goes, and have varying levels of configuration.
#### Outline of data being shared
- `Loot stacks when killing npcs`: Npc info and item information
- `Your displayName and combatLevel`: only if the Checkbox `Attach Player Info` is checked
- `Level gains`: includes all levels and any level gains
- You have the ability to turn off all/or any out-going messages.

## How to addon/extend this plugin
1. Think of an event you want to export
2. Create a new class in com.osrsevents that implments the Sendable interface
3. Define the fields you want to send (See NpcKillNotification.java for an example)
4. In the EventPlugin.java, subscribe to some RuneLite event or detect a change in the gameTick() loop.
5. When ready, instante the new Notification.java class you made, and call api.send(MY_NOTIFICATION)

## Config Options Panel : In Runelite
#### Input Boxes
- `bearerToken` an optional token sent in the header to be used by a server
- `endpoint` the base endpoint for the server (Example: `http://localhost:5000/api/`) *required*
        
#### Checkboxes
- `Enable Event Sending`: Master toggle, when off no events will send.
- `Enable Npc Kills`: When on, Npc Kill Events will send.
- `Enable Level Gains`: When on, Level events will send
- `Attach Player Info`: When on, displayName and combatLevel will be added to each event

## Supported Endpoints
*Each notification has its own constant endpoint defined in `ApiManager.java`*
 - `NpcKillNotification` sends to  `<ENDPOINT>/npc_kill/`
 - `LevelChangeNotification` sends to  `<ENDPOINT>/level_change/`

 - TODO: Possible events: quest completes, login states, map positions.
## Event Payload Structure
#### Header Fields
- `Authorization` an optional Bearer Token (Configure on config page) attached to each POST request
    - Example: `Authorization Bearer: 8df-32lnp-4839a`

#### Event Body Fields
   Every event will be an EventWrapper.java class that is serialized. Below is an example of a `npc_kill` that is serialized and being sent.
   - `playerInfo` is an optional supplement and will be null if `Attach Player Info` is not checked in the Config Panel
   - `timestamp` unix epoch time of when the event was sent
   - `data` is required and contains the serialized json of the event (NpcKillNotification, etc)
   
## Loot Drop Notification 
* Endpoint: `POST <BASE_ENDPOINT_FROM_CONFG>/npc_kill/`
* Class: `NpcKillNotification`
* Data Fields:
    - `npcId: 45` (Always attached)
    - `items: [{itemId: 4000, quantity: 3},...]` (Attached upon level up)
    
* Triggered: when loot is dropped by an NPC
```
{
    "data": {
        "npcId": 44,
        "items": [
            {
                "itemId": 4000,
                "quantity": 3
            }
        ]
    },
    "playerInfo": {
        "username": "Zezima",
        "combatLevel": 126
    },
    "timestamp": 1633662492,
}
```


## Level Change Message 
* Endpoint: `<BASE_ENDPOINT_FROM_CONFG>/level_change/`
* Class: `LevelChangeNotification`
* Data Fields:
    - `levels: ["Fishing": 33, "Strength": 29, ... "Farming": 1]` (Always attached)
    - `updatedSkillLevel: 29` (Attached upon level up)
    - `updatedSkillName: "Strength"` (Attach upon level up)
    
* Triggered: level gain and on login

```
{
  "data": {
    "updatedSkillName": "Agility",  (Only attached on a level gain)
    "updatedSkillLevel": 40,        (Only attached on a level gain)
    "levels": {
      "Agility": 40, 
      "Attack": 1, 
      "Construction": 1, 
      "Cooking": 15, 
      "Crafting": 8, 
      "Defence": 1, 
      "Farming": 1, 
      "Firemaking": 33, 
      "Fishing": 24, 
      "Fletching": 1, 
      "Herblore": 1, 
      "Hitpoints": 10, 
      "Hunter": 1, 
      "Magic": 4, 
      "Mining": 4, 
      "Overall": 1, 
      "Prayer": 9, 
      "Ranged": 1, 
      "Runecraft": 1, 
      "Slayer": 1, 
      "Smithing": 1, 
      "Strength": 3, 
      "Thieving": 20, 
      "Woodcutting": 50
    }
  }, 
  "playerInfo": {
    "combatLevel": 5, 
    "username": "int GIM"
  }, 
  "timestamp": 1633662492
}
```
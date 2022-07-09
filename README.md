# OSRSEvents


## How to use???
- Install via Plugin Hub in RuneLite
- Navigate to Settings for `OSRSEvents`
- Enter a valid `Base Endpoint` which can receive these POST HTTP Requests
- Optionally: Enter a Bearer Token, used to identify sender of data without using sensitive information

## Security & Privacy
This plugin is setup by the user. You choose where your data goes, and have varying levels of configuration.
#### Outline of data being emitted to API
- `OSRS in-game name, combat leve, osrs map position`: only if the Checkbox `Attach Player Info` is checked
- `Loot received`: Npc info and loot information
- `Level gains`: includes all levels and any level gains
- `Bank Items`: Bank value and item data
- `Inventory Items`: Inventory value and item data
- `Equipment Items`: Current armour eqipped
- `Quest States`: Quest Points and Quest States (IN_PROGRESS, COMPLETED, etc)
- You have the ability to turn off all/or any out-going messages.

## How to addon/extend this plugin
1. Think of an event you want to export
2. Create a new class in com.osrsevents.notifications that implements the Sendable interface
3. Define the fields you want to send (See NpcKillNotification.java for an example)
4. In the EventPlugin.java, subscribe to some RuneLite event or detect a change in the gameTick() loop.
5. When ready, instantiate the new Notification.java class you made, and call 
    - `messageHandler.sendEventNow(MESSAGE_TYPE.LOOT, event)` To send an urgent/immediate message
    - `messageHandler.updateLatest(MESSAGE_TYPE.BANK, event)` To update the latest of a given message

## Config Options Panel : In Runelite
#### Input Boxes
- `Bearer Token` an optional token sent in the header to be used by a server
- `Base Endpoint` the base endpoint for the server (Example: `http://localhost:5000/api/`) *required*
        
#### Checkboxes
- `Enable Event Sending`: Master toggle, when off no events will send.
- `Attach Player Info`: When on, ingame name, combat level, osrs position are attached to each request

###### Events which are sent immediately 
- `Emit Npc Kills`: When on, npc loot drop will be sent
- `Emit Level Gains`: When on, level gains will be sent
- `Emit Quests`: When on, quests are sent
- `Emit Login`: When on, login state is sent

###### Events which can be throttle on frequent updates
- `Emit Inventory`: When on, inventory is sent when updated (or every 20 seconds if frequent updates occur)
- `Emit Equipment`: When on, current armour is sent when updated, (or every 20 seconds if frequent updates occur)
- `Emit Bank`: When on, bank is sent when X'ing out of bank, (or every 20 seconds if frequent updates occur)

## Supported Endpoints
*Each notification has its own constant endpoint defined in `ApiManager.java`*
 - `NpcKillNotification` => `<ENDPOINT>/npc_kill/`
 - `LevelChangeNotification` => `<ENDPOINT>/level_change/`
 - `BankNotification` => `<ENDPOINT>/bank/`
 - `EquipSlotsNotification` => `<ENDPOINT>/equipped_items/`
 - `InventorySlotsNotification` => `<ENDPOINT>/inventory_items/`
 - `LoginNotification` => `<ENDPOINT>/login_state/`
 - `QuestChangeNotification` => `<ENDPOINT>/quest_change/`

## Event Payload Structure
#### Header Fields
- `Authorization` an optional Bearer Token (Configure on config page) attached to each POST request
    - Example: `Authorization Bearer: 8df-32lnp-4839a`
- `X-Request-Id` a unique UUID of each request

#### Event Body Fields
   Every event will be an EventWrapper.java class that is serialized. Below is an example of a `npc_kill` that is serialized and being sent.
   - `playerInfo` is an optional supplement and will be null if `Attach Player Info` is not checked in the Config Panel
        * JSON Of `playerInfo`
        ``` 
        "playerInfo": {
                   "combatLevel": 41,
                   "position": {
                       "plane": 0,
                       "x": 3216,
                       "y": 3215
                   },
                   "username": "int GIM"
        },
     ```
   - `timestamp` unix epoch time of when the event was sent
   - `data` is required and contains the serialized json of the event (NpcKillNotification, etc)

## Login Notification
* Endpoint: `POST <BASE_ENDPOINT_FROM_CONFIG>/login_state/`
* Class: `LoginNotification.java`
* Data Fields:
    - `state` LOGGED_IN, LOGGED_OUT depending on player state
* Triggered: when login state changes

```
{
    "data": {
        "state": "LOGGED_IN"
    },
    "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
        "username": "int GIM"
    },
    "timestamp": 1633920014 
}
```

## Loot Drop Notification 
* Endpoint: `POST <BASE_ENDPOINT_FROM_CONFIG>/npc_kill/`
* Class: `NpcKillNotification`
* Data Fields:
    - `npcId: 45` (Always attached)
    - `items: [{itemId: 4000, quantity: 3},...]` (Attached if items drop)
    - `gePrice`: integer of ge price.
* Triggered: when loot is dropped by an NPC
```
{
    "data": {
        "npcId": 44,
        "items": [
            {
                "itemId": 4000,
                "quantity": 3
            }, 
            //....
        ],
        "gePrice": 45334
    },
    "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
        "username": "int GIM"
    },
    "timestamp": 1633662492,
}
```


## Level Change Message 
* Endpoint: `<BASE_ENDPOINT_FROM_CONFIG>/level_change/`
* Class: `LevelChangeNotification.java`
* Data Fields:
    - `levels: ["Fishing": 33, "Strength": 29, ... "Farming": 1]` (Always attached)
    - `totalLevel`: (Always attached)
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
    },
    "totalLevel": 546,
  }, 
  "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
        "username": "int GIM"
  }, 
  "timestamp": 1633662492
}
```


## Bank Notification
* Endpoint: `<BASE_ENDPOINT_FROM_CONFIG>/bank/`
* Class: `BankNotification.java`
* Data Fields:
    - `items: [{id: 1, quantity: 1}, ....]` (Always attached, in order of players bank)
    -  `quantity` is 0 if the item is a placeholder. Thus price is also 0 while the item is out of the bank.
    - `value: 68802` (Always attached, GE Price bank value)
   
* Triggered: when bank is X'ed out of in game, sent every 20 seconds when frequent update detected.
```
{
    "data": {
        "items": [
            {
                "id": 1205,
                "quantity": 1
            },
            {
                "id": 555,
                "quantity": 516
            },
            {
                "id": 559,
                "quantity": 2
            },
            {
                "id": 1438,
                "quantity": 1
            },
            {
                "id": 26170,
                "quantity": 1
            },
            {
                "id": 952,
                "quantity": 2
            },
            //......
        ],
        "value": 68802
    },
    "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
        "username": "int GIM"
    },
    "timestamp": 1633920125
}
```

## Inventory Notification
* Endpoint: `<BASE_ENDPOINT_FROM_CONFIG>/inventory_items/`
* Class: `InventorySlotsNotification.java`
* Data Fields:
    - `inventory: [{id: 1, quantity: 1}, ....]` (Always attached, array of 28 Items in players inventory)
    - `gePrice` (Always attached, value of inventory in GE Price)
* Triggered: when inventory changes, or every 20 seconds if frequent updates are detected.

```
//Inventory
{
    "data": {
        "inventory": [
            {
                "id": 1929,
                "quantity": 1
            },
            {
                "id": 25840,
                "quantity": 1
            },
            {
                "id": 995,
                "quantity": 100
            },
            {
                "id": 13679,
                "quantity": 1
            },
            {
                "id": 201,
                "quantity": 1
            }, 
            //... Always 28 element in array, id = 0, quantity = 0 if empty
        ],
        "gePrice": 34500
    },
    "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
        "username": "int GIM"
    },
    "timestamp": 1633920064
}
```

## Quest Notification
* Endpoint: `<BASE_ENDPOINT_FROM_CONFIG>/quest_change/`
* Class: `QuestChangeNotification.java`
* Data Fields:
    - `quests: [{id: 1, name: "Dragon Slayer", state: "IN_PROGRESS"}, ....]` (Always attached, all quests included)
    - `qp` (Always attached, total Quest Points of player)
    - `quest` (Attached when a quest state changes, this is the name of the quest)
    - `state` (Attached when a quest state changes, this is the new state of the quest)
    
* Triggered: On login, or when a quest is started/completed

```
{
    "data": {
        "qp": 60,
        "quests": [
            {
                "id": 299,
                "name": "Black Knights' Fortress",
                "state": "IN_PROGRESS"
            },
            {
                "id": 300,
                "name": "Cook's Assistant",
                "state": "FINISHED"
            },
            {
                "id": 301,
                "name": "The Corsair Curse",
                "state": "NOT_STARTED"
            },
            //.... About 160 more quests
        ]
    },
    "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
    },
    "timestamp": 1633920347
}
```

## Equipment Notification
* Endpoint: `<BASE_ENDPOINT_FROM_CONFIG>/equipped_items/`
* Class: `EquipSlotsNotification.java`
* Data Fields:
    - `equippedItems: {{AMMO: {id: 1, quantity: 4000}}` (Always attached, *slot data has data if item exists*)
        * equippedItem keys: 	HEAD,
                             	CAPE,
                             	AMULET,
                             	WEAPON,
                             	BODY,
                             	SHIELD,
                             	LEGS,
                             	GLOVES,
                             	BOOTS,
                             	RING,
                             	AMMO
    
* Triggered: When equipment changes, every 20 seconds if frequent updates detected
```
{
    "data": {
        "equippedItems": {
            "AMMO": {
                "id": 877,
                "quantity": 4
            },
            "AMULET": {
                "id": 1478,
                "quantity": 1
            },
            "CAPE": {
                "id": 13679,
                "quantity": 1
            },
            "SHIELD": {
                "id": 13660,
                "quantity": 1
            },
            "WEAPON": {
                "id": 35,
                "quantity": 1
            }
        }
    },
    "playerInfo": {
        "combatLevel": 41,
        "position": {
            "plane": 2,
            "x": 3208,
            "y": 3220
        },
        "username": "int GIM"
    },
    "timestamp": 1633920083
}
```

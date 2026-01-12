# Boss Jull Plugin - The False Prophet

A complete, working Minecraft Paper/Spigot plugin for version 1.20.x that implements a custom boss called "The False Prophet" - a corrupted Villager with unique immunity and vulnerability mechanics.

## Overview

The False Prophet is a challenging custom boss fight that features a unique mechanic: the boss is **immune to damage** when protected by minions (False Villagers), and becomes **vulnerable** only when all minions are defeated. This creates a strategic two-phase combat loop where players must:

1. Eliminate all False Villager minions
2. Damage the boss during a brief vulnerable window (6 seconds)
3. Repeat until the boss is defeated

## Features

- ✨ **Custom Boss Entity**: Villager-based boss with 500 HP
- 🛡️ **Immunity Mechanic**: Boss cannot be damaged while minions are alive
- ⚔️ **Vulnerable Windows**: 6-second damage window after killing all minions
- 😈 **Enraged Phase**: Boss becomes more aggressive at 30% HP
- 👥 **False Villager Minions**: Spawn in waves to protect the boss
- 📊 **Boss Bar**: Dynamic health display with color-coded states
- 🎯 **Custom Summon Item**: Totem of False Faith
- 🌍 **Summon Requirements**: Must be in Village biome on Emerald Block

## Summon Requirements

To summon The False Prophet, you must:

1. **Obtain the Totem of False Faith**
   - Use the command: `/givefalsefaith`
   - Item: Enchanted Totem of Undying with custom name and lore

2. **Find a Village Biome**
   - Valid biomes: Plains, Sunflower Plains, Desert, Savanna, Taiga, Snowy Plains

3. **Place an Emerald Block**
   - Place a single Emerald Block on the ground

4. **Stand on the Block**
   - Stand on top of the Emerald Block

5. **Right-Click the Totem**
   - Right-click while holding the Totem of False Faith
   - The totem will be consumed
   - The boss will spawn with dramatic effects

## Boss Mechanics

### State Machine

The boss operates on a finite state machine with the following states:

#### 1. SPAWNING (Yellow Boss Bar)
- Duration: 2 seconds
- Boss spawns with lightning and sound effects
- Transitions to IMMUNE state after spawning

#### 2. IMMUNE (Red Boss Bar)
- Boss has 3 False Villagers protecting him
- **Boss cannot take damage**
- Attacking the boss applies Weakness effect to the player
- Message: "The False Prophet is protected by his followers!"

#### 3. VULNERABLE (Green Boss Bar)
- All False Villagers have been killed
- **Boss can take damage**
- Duration: 6 seconds (120 ticks)
- Boss glows during this window
- Bell sound plays when entering this state
- Happy Villager particles appear
- Message: "The False Prophet is exposed!"

#### 4. ENRAGED (Purple Boss Bar)
- Triggered when boss HP drops below 30%
- Spawns 5 False Villagers (instead of 3)
- Vulnerable window reduced to 4 seconds
- Boss movement speed increases
- Message: "The False Prophet is enraged!"

#### 5. DEAD (Blue Boss Bar)
- Boss has been defeated
- Victory message broadcast to nearby players
- Lightning effect plays
- All minions are removed

### Combat Flow

**Normal Phase:**
1. Boss spawns with 3 False Villagers
2. Players kill all 3 minions
3. Boss becomes VULNERABLE for 6 seconds
4. Players deal damage during the window
5. After 6 seconds, boss spawns 3 new minions
6. Boss returns to IMMUNE state
7. Repeat until boss HP < 30%

**Enraged Phase (< 30% HP):**
1. Boss spawns 5 False Villagers
2. Players kill all 5 minions
3. Boss becomes VULNERABLE for 4 seconds
4. Players deal damage during the window
5. After 4 seconds, boss spawns 5 new minions
6. Repeat until boss is defeated

### False Villagers (Minions)

- **Entity Type**: Villager (Nitwit profession)
- **Name**: §7False Villager
- **Spawn Pattern**: Circle around boss (5-8 blocks radius)
- **Behavior**: Basic AI, wanders around
- **Drops**: No items or XP
- **Purpose**: Protect the boss from damage

## Commands

### /givefalsefaith [player] [amount]

Gives the Totem of False Faith summon item.

**Permission**: `bossjull.falseprophet` (default: op)

**Usage Examples**:
- `/givefalsefaith` - Give yourself 1 totem
- `/givefalsefaith PlayerName` - Give PlayerName 1 totem
- `/givefalsefaith PlayerName 5` - Give PlayerName 5 totems

**Help**: `/givefalsefaith help` - Shows detailed boss mechanics

## Installation

### For Server Administrators

1. **Download** the plugin JAR file
2. **Place** the JAR in your server's `plugins/` folder
3. **Restart** your server
4. **Verify** the plugin loaded:
   ```
   [Server] INFO FalseProphetPlugin has been enabled!
   ```

### Requirements

- **Server Type**: Paper or Spigot
- **Minecraft Version**: 1.20.x (tested on 1.20.6)
- **Java Version**: 17 or higher

## Building from Source

### Prerequisites

- Java JDK 17 or higher
- Maven 3.6 or higher

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/FikJul/boss-jull-plugin.git
   cd boss-jull-plugin
   ```

2. **Build with Maven**:
   ```bash
   mvn clean package
   ```

3. **Locate the JAR**:
   ```
   target/boss-jull-plugin-1.1.0.jar
   ```

4. **Install** the JAR to your server's `plugins/` folder

## Troubleshooting

### Boss Won't Summon

**Problem**: "You must stand on an Emerald Block"
- **Solution**: Place an Emerald Block and stand directly on top of it

**Problem**: "You must be in a Village biome"
- **Solution**: Use `/biome` (if available) or find a Plains, Desert, Savanna, Taiga, or Snowy Plains biome

### Boss Not Taking Damage

**Problem**: Boss is immune and won't take damage
- **Solution**: This is intentional! Kill all False Villagers first
- The boss will only take damage during the 6-second vulnerable window

### Boss Bar Not Showing

**Problem**: Boss bar is not visible
- **Solution**: The boss bar only shows to players within 50 blocks
- Move closer to the boss to see the health bar

## Technical Details

### Architecture

The plugin uses a clean, event-driven architecture:

```
FalseProphetPlugin (Main)
├── BossManager (State Machine & Logic)
├── BossListener (Event Handlers)
├── FalseProphetCommand (Command Handler)
├── FalseProphetBoss (Boss Entity Wrapper)
├── BossState (Enum - FSM States)
└── SummonItem (Item Factory)
```

### State Machine Diagram

```
    [SPAWNING]
        │
        │ (2 seconds)
        ▼
    [IMMUNE] ◄──────────┐
        │               │
        │ (all minions  │
        │  killed)      │
        ▼               │
  [VULNERABLE]          │
        │               │
        │ (6 seconds    │
        │  OR new       │
        │  minions)     │
        └───────────────┘
        
    (HP < 30%)
        │
        ▼
   [ENRAGED]
    (same loop,
     5 minions,
     4 sec window)
        
    (HP = 0)
        │
        ▼
     [DEAD]
```

### Entity Tagging

The plugin uses Bukkit's PersistentDataContainer for entity identification:

- **Boss**: Key `"false_prophet_boss"`, Type: BOOLEAN, Value: true
- **Minions**: Key `"false_villager"`, Type: STRING, Value: Boss UUID

### Performance

- **State Checks**: Every 10 ticks (0.5 seconds)
- **Boss Bar Updates**: Every state check
- **Player Range**: 50 blocks for boss bar and messages
- **Non-blocking**: All tasks use BukkitRunnable

## API Compatibility

- Uses Paper/Spigot 1.20.x API
- No deprecated methods
- Uses modern Attribute enum
- Compatible with PersistentDataContainer API

## Credits

- **Author**: FikJul
- **Plugin Name**: BossJullPlugin
- **Version**: 1.1.0
- **License**: MIT (or your license)

## Support

For bugs, feature requests, or questions:
- Open an issue on GitHub
- Contact the plugin author

---

**May you have the courage to face The False Prophet!** ⚔️
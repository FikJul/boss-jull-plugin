# VillagerFalseProphet - Custom Boss Plugin

A production-ready Spigot/Paper plugin featuring "The False Prophet", a corrupted villager boss with unique immunity mechanics.

## Overview

The False Prophet is a custom boss fight that challenges players to manage minions while dealing damage during vulnerability windows. The boss alternates between immune and vulnerable states, creating a dynamic and strategic combat experience.

## Features

- **Custom Boss Entity**: Villager-based boss with custom attributes and behavior
- **Immunity Mechanic**: Boss is protected by False Followers and must be made vulnerable
- **State Machine**: Five distinct boss states (SPAWNING, IMMUNE, VULNERABLE, ENRAGED, DEAD)
- **Dynamic Difficulty**: Enraged phase at 30% HP with increased minion count and reduced vulnerability window
- **Visual Feedback**: Boss bar, particles, and glowing effects indicate current state
- **No External Dependencies**: Pure Spigot/Paper API implementation (1.20.x compatible)

## Summon Item

### Totem of False Faith

- **Material**: Totem of Undying
- **Display Name**: §6Totem of False Faith
- **Lore**:
  - §7A sacred relic worshipped by the masses
  - §7Only the chosen may reveal the truth
- **Enchantment**: Glowing effect (Unbreaking I, hidden)

### Obtaining the Totem

Use the command:
```
/givefalsefaith [player] [amount]
```

## Summon Conditions

To summon The False Prophet, players must:

1. **Stand on an Emerald Block** - The block beneath your feet must be an emerald block
2. **Be in a Village Biome** - Valid biomes include:
   - Plains and variants
   - Desert
   - Savanna and variants
   - Taiga
   - Snowy Plains and variants
3. **Right-click the Totem** - Use the Totem of False Faith

### Summon Effects

When summoned successfully:
- Lightning strike effect (cosmetic, no damage)
- Bell sound and thunder
- Green particles and villager happy particles
- Global broadcast message

## Boss Mechanics

### Boss Attributes

- **Health**: 500 HP
- **Type**: Villager (Cleric profession)
- **Name**: §4The False Prophet
- **Knockback Resistance**: 0.8
- **Movement Speed**: 0.15 (slower than normal)

### Combat Cycle

#### Phase 1: Normal (HP > 30%)

1. **IMMUNE State**
   - Boss cannot take damage
   - Red particles surround the boss
   - 3 False Followers are alive
   - Attacking applies Weakness effect to players
   - Message: "§c§lThe False Prophet is protected by his followers!"

2. **Kill all False Followers**
   - Transition to VULNERABLE state
   - Boss glows green
   - Villager happy particles appear
   - Bell sound plays
   - 6-second vulnerability window starts

3. **VULNERABLE State**
   - Boss can take damage
   - Boss bar turns green
   - After 6 seconds: returns to IMMUNE state
   - New False Followers spawn

#### Phase 2: Enraged (HP ≤ 30%)

When boss health drops below 150 HP:
- Transitions to ENRAGED state
- Purple dragon breath particles
- Boss bar turns purple
- Spawns 5 False Followers (instead of 3)
- Vulnerability window reduced to 3 seconds (instead of 6)
- Global warning message

### False Followers

- **Entity**: Villager (Nitwit profession)
- **Name**: §7False Follower
- **Behavior**: Normal villager AI
- **Purpose**: Protect the boss while alive
- **Spawn Pattern**: Random positions 3-5 blocks from boss
- **Death**: No drops, triggers boss vulnerability check

### State Machine

```
SPAWNING (2s)
    ↓
IMMUNE (False Followers alive)
    ↓ (All False Followers killed)
VULNERABLE (6s window / 3s if enraged)
    ↓ (Timer expires)
IMMUNE (New False Followers spawn)
    
    [HP < 30% → ENRAGED state overlay]
    
    [HP = 0 → DEAD]
```

## Boss Bar

The boss bar shows:
- Boss name: §4The False Prophet
- Current health percentage
- Color-coded state:
  - **Yellow**: Spawning
  - **Red**: Immune
  - **Green**: Vulnerable
  - **Purple**: Enraged
  - **White**: Dead

## Commands

### /givefalsefaith

Give the Totem of False Faith to yourself or another player.

**Usage:**
```
/givefalsefaith              # Give 1 totem to yourself
/givefalsefaith <player>     # Give 1 totem to player
/givefalsefaith <amount>     # Give amount totems to yourself
/givefalsefaith <player> <amount>  # Give amount totems to player
```

**Permission:** `falseprophet.give` (default: op)

**Examples:**
```
/givefalsefaith              → You get 1 totem
/givefalsefaith Steve        → Steve gets 1 totem
/givefalsefaith 5            → You get 5 totems
/givefalsefaith Steve 3      → Steve gets 3 totems
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `falseprophet.give` | Allows using /givefalsefaith command | op |

## Installation

1. Download the latest JAR from releases
2. Place in your server's `plugins/` folder
3. Restart the server
4. Plugin is ready to use!

## Building from Source

### Requirements

- Java 17 or higher
- Maven 3.6 or higher

### Build Steps

```bash
# Clone the repository
git clone https://github.com/FikJul/boss-jull-plugin.git
cd boss-jull-plugin

# Build with Maven
mvn clean package

# JAR will be in target/villager-false-prophet-1.0.0.jar
```

## Testing Guide

### Quick Test

1. Give yourself the totem:
   ```
   /givefalsefaith
   ```

2. Find a village biome

3. Place an emerald block

4. Stand on the emerald block

5. Right-click the totem

6. Boss should spawn with effects!

### Testing Boss Mechanics

1. **Test Immunity**:
   - Attack the boss before killing False Followers
   - Should see: "§c§lThe False Prophet is protected by his followers!"
   - Should receive Weakness effect

2. **Test Vulnerability**:
   - Kill all 3 False Followers
   - Boss should glow green
   - Boss should take damage
   - After 6 seconds, boss becomes immune again

3. **Test Enraged State**:
   - Reduce boss HP below 150
   - Should see purple particles
   - Should spawn 5 False Followers (instead of 3)
   - Vulnerability window should be 3 seconds (instead of 6)

4. **Test Victory**:
   - Defeat the boss
   - Should see victory broadcast
   - Boss bar should disappear

## Troubleshooting

### Boss doesn't spawn

- **Check biome**: Must be in a valid village biome
- **Check block**: Must stand on emerald block
- **Check item**: Must use the Totem of False Faith (use /givefalsefaith)
- **Check existing boss**: Only one boss can be active at a time

### Boss won't take damage

- **Check False Followers**: All False Followers must be killed first
- **Check state**: Boss must be in VULNERABLE state (green glow)
- **Wait for window**: Attack during the 6-second (or 3-second) vulnerability period

### False Followers not spawning

- **Check boss state**: Boss must transition from VULNERABLE to IMMUNE
- **Check boss death**: Boss must still be alive
- **Wait for transition**: Spawning happens after vulnerability timer expires

### Performance issues

- The plugin uses efficient async tasks for particles and mechanics
- Particle spawn rate is limited to every 0.5 seconds
- Mechanic checks run every 0.25 seconds
- All tasks are properly cleaned up on boss death

## Technical Details

### Architecture

- **Event-Driven**: Uses Bukkit event system for all interactions
- **State Machine**: Enum-based state management
- **PDC Tagging**: PersistentDataContainer for entity identification
- **Task Management**: Proper cleanup of all scheduled tasks
- **No Blocking**: All operations are async-safe

### Entity Identification

The plugin uses Minecraft's PersistentDataContainer (PDC) to mark entities:

- **Boss**: `FalseProphetBoss` → byte(1)
- **False Follower**: `FalseVillager` → boss UUID (string)

This ensures reliable entity tracking across server restarts and chunk loading.

### Performance Optimization

- Efficient particle spawning (limited count and frequency)
- Smart false follower tracking (removes dead entities)
- Proper task cancellation on cleanup
- Minimal event handlers with priority optimization

## Credits

- **Author**: FikJul
- **Version**: 1.0.0
- **API**: Spigot/Paper 1.20.4-R0.1-SNAPSHOT
- **License**: Not specified

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Contact the author

---

**May you reveal the truth behind The False Prophet!**
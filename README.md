# Sentinel of the Ring - Boss Fight Plugin

A custom Spigot/Paper plugin featuring a challenging 2-player cooperative boss fight designed to be impossible to defeat solo.

## Overview

**Sentinel of the Ring** is a complete, production-ready boss plugin that requires precise coordination between two players. The boss utilizes a unique ring-based positioning mechanic and punishing slam attacks to create an engaging cooperative experience.

## Features

- ✅ **Pure Spigot/Paper API** - No external dependencies (MythicMobs, MMOItems, etc.)
- ✅ **Event-driven architecture** - Clean, maintainable code
- ✅ **Dynamic boss bar** - Color changes based on phase
- ✅ **Visual particle rings** - Clear positioning indicators
- ✅ **Dual-phase combat** - Rings swap at 50% HP
- ✅ **Slam attack mechanic** - Punishes player movement
- ✅ **State machine** - Robust state management
- ✅ **Non-blocking async design** - No server lag

## Boss Mechanics

### Arena Layout

The boss fight takes place in two concentric rings:

```
         ╔═══════════════════════════════╗
         ║    OUTER RING (8 blocks)      ║
         ║   ╔═══════════════════╗       ║
         ║   ║ INNER RING (4 blk)║       ║
         ║   ║       [BOSS]      ║       ║
         ║   ║                   ║       ║
         ║   ╚═══════════════════╝       ║
         ║                               ║
         ╚═══════════════════════════════╝
```

### Phase 1: Normal Positioning

**Ring Indicators:**
- **Inner Ring** (≤4 blocks): Purple particles (ENCHANTED_HIT)
- **Outer Ring** (>4 and ≤8 blocks): Red particles (FLAME)

**Damage Rules:**
- Exactly 2 players must be in the arena
- One player in the INNER ring
- One player in the OUTER ring
- Boss bar: RED

**Slam Attack:**
- Interval: Every 10 seconds
- Damage: 10 HP
- Warning: "SENTINEL CHARGES A SLAM! STAND STILL!"
- Charge time: 2 seconds
- Players who move >0.5 blocks take damage

### Phase 2: Swapped Rings (at 50% HP)

**Ring Indicators:**
- **Inner Ring** (≤4 blocks): Red particles (FLAME)
- **Outer Ring** (>4 and ≤8 blocks): Purple particles (ENCHANTED_HIT)

**Changes:**
- Ring colors swap - players must switch positions
- Slam damage increases to 15 HP
- Boss bar: PURPLE
- Warning: "PHASE 2: THE RINGS HAVE SWAPPED! SWITCH POSITIONS!"

### Fail States

**Combat Reset:**
If player count drops below 2 during combat:
- Boss returns to IDLE state
- HP resets to maximum (500)
- Message: "The Sentinel returns to dormancy..."

**Invalid Damage Attempts:**
Damage is blocked if:
- Not in a combat phase (IDLE, TRANSITION, DEAD)
- Less than 2 players in arena
- Players not positioned correctly (one in each ring)

## Commands

| Command | Permission | Description | Default |
|---------|-----------|-------------|---------|
| `/sentinel give [player] [amount]` | `bossjull.sentinel.give` | Give summon item(s) | OP |
| `/sentinel summon` | `bossjull.sentinel.summon` | Direct summon (admin) | OP |
| `/sentinel help` | `bossjull.sentinel` | Show help menu | OP |

### Command Examples

```bash
# Give yourself 1 summon item
/sentinel give

# Give Steve 5 summon items
/sentinel give Steve 5

# Directly summon the boss at your location
/sentinel summon

# Display help menu
/sentinel help
```

## Summon Item

**Item:** Nether Star  
**Name:** §c§lSentinel Summoning Star  
**Enchantment:** Unbreaking I (for glow effect)

**Usage:**
1. Obtain via `/sentinel give` command
2. Right-click while in Survival or Adventure mode
3. Boss spawns at your location
4. Item is consumed (single-use)

**Lore:**
```
§7§o[Sentinel Summon]

§7Right-click to summon the
§c§lSentinel of the Ring

§e⚠ Requires 2 players to defeat
§e⚠ Single-use item

§6§lMechanics:
§7• One player in §dINNER RING §7(4 blocks)
§7• One player in §cOUTER RING §7(8 blocks)
§7• Stand still during §4SLAM ATTACKS

§c§lGood luck...
```

## Installation

1. **Download** the latest release JAR file
2. **Place** the JAR in your server's `plugins/` folder
3. **Restart** your server
4. **Verify** the plugin loaded: `/plugins`
5. **Give summon items** to players: `/sentinel give <player>`

## How to Play

### Step-by-Step Guide

1. **Obtain a summon item**
   ```
   /sentinel give YourName
   ```

2. **Find a suitable arena location**
   - Open area with at least 16x16 blocks
   - Flat terrain recommended

3. **Gather 2 players**
   - Solo attempts will fail
   - Both players must be in Survival or Adventure mode

4. **Summon the boss**
   - Right-click the Nether Star
   - Boss spawns with particle rings visible

5. **Position yourselves**
   - Player 1: Stand in INNER ring (purple particles in Phase 1)
   - Player 2: Stand in OUTER ring (red particles in Phase 1)

6. **Attack the boss**
   - Only valid when both players are correctly positioned
   - Watch for slam attack warnings

7. **During slam attacks**
   - Message appears: "SENTINEL CHARGES A SLAM! STAND STILL!"
   - Stay completely still for 2 seconds
   - Moving >0.5 blocks = damage

8. **At 50% HP (Phase 2)**
   - Rings swap colors
   - Players must switch positions
   - Inner ring turns RED
   - Outer ring turns PURPLE

9. **Defeat the boss**
   - Reduce HP to 0
   - Victory message appears
   - Boss despawns

## Building from Source

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/FikJul/boss-jull-plugin.git
cd boss-jull-plugin

# Build with Maven
mvn clean package

# Output JAR location
target/boss-jull-plugin-1.0.0.jar
```

## Project Structure

```
boss-jull-plugin/
├── pom.xml                          # Maven configuration
├── README.md                        # This file
├── .gitignore                       # Git ignore rules
└── src/main/
    ├── resources/
    │   └── plugin.yml               # Plugin metadata
    └── java/com/fikjul/bossjull/
        ├── BossJullPlugin.java      # Main plugin class
        ├── BossState.java           # State machine enum
        ├── SentinelBoss.java        # Boss entity wrapper
        ├── BossManager.java         # Core logic controller
        ├── BossListener.java        # Event handlers
        ├── SentinelCommand.java     # Command executor
        └── SummonItem.java          # Summon item manager
```

## Architecture

### State Machine

```
        ┌──────┐
        │ IDLE │◄─────────────┐
        └──┬───┘              │
           │ 2 players enter  │ <2 players
           ▼                  │
    ┌─────────────┐           │
    │ PHASE_1     │───────────┤
    │ COMBAT      │           │
    └──────┬──────┘           │
           │ HP ≤ 50%         │
           ▼                  │
    ┌─────────────┐           │
    │ PHASE_2     │           │
    │ TRANSITION  │           │
    └──────┬──────┘           │
           │ After 2s         │
           ▼                  │
    ┌─────────────┐           │
    │ PHASE_2     │───────────┘
    │ COMBAT      │
    └──────┬──────┘
           │ HP ≤ 0
           ▼
        ┌──────┐
        │ DEAD │
        └──────┘
```

### Event Flow

```
Player Right-Clicks Summon Item
         │
         ▼
   [BossListener]
         │
         ├─► Validate item & game mode
         ├─► Check if boss active
         ├─► Summon boss via BossManager
         └─► Consume item
         
Player Attacks Boss
         │
         ▼
   [BossListener]
         │
         ├─► Check if entity is boss
         ├─► Extract attacking player
         ├─► Call BossManager.canDamageBoss()
         │   │
         │   ├─► Validate state (combat phase)
         │   ├─► Count active players (must be 2)
         │   ├─► Check ring positions (1 in each)
         │   └─► Return true/false
         │
         └─► Cancel event if not allowed
```

### Task System

**Particle Task** (10 ticks / 0.5s)
- Spawns ring particles
- Updates visual indicators

**Mechanic Check Task** (5 ticks / 0.25s)
- Updates active player list
- Manages boss bar visibility
- Checks state transitions
- Handles HP thresholds

**Slam Task** (200 ticks / 10s)
- Executes slam attack sequence
- Only during combat phases

## Visual Indicators

| Phase | Inner Ring | Outer Ring | Boss Bar |
|-------|-----------|-----------|----------|
| Idle | Purple | Red | White |
| Phase 1 | Purple | Red | Red |
| Transition | Purple→Red | Red→Purple | Yellow |
| Phase 2 | Red | Purple | Purple |
| Dead | - | - | Green |

## Performance Notes

- **Particle Optimization:** 40 particles per ring, updated every 0.5s
- **No Blocking Operations:** All tasks use BukkitRunnable
- **Rate-Limited Feedback:** Player messages throttled to 3-second cooldown
- **Efficient Distance Checks:** Squared distance used where possible

## Troubleshooting

### Boss Not Taking Damage

**Problem:** Attacks don't damage the boss  
**Solutions:**
- Ensure exactly 2 players in arena
- Check positioning: 1 in inner ring, 1 in outer ring
- Verify combat phase (not IDLE or TRANSITION)
- Players must be in Survival/Adventure mode

### Boss Resets to Full HP

**Problem:** Boss heals back to maximum health  
**Cause:** Player count dropped below 2  
**Solution:** Keep both players in arena at all times

### Slam Attack Not Working

**Problem:** Players not taking slam damage  
**Checks:**
- Verify players are moving during charge phase
- Ensure boss is in combat phase
- Check for plugin errors in console

### Plugin Won't Load

**Problem:** Plugin fails to enable  
**Solutions:**
- Check Java version (requires 17+)
- Verify Paper/Spigot version (1.21+)
- Check console for error messages
- Ensure no conflicting plugins

## Custom Rewards

To add custom rewards when the boss dies, modify `BossListener.java`:

```java
@EventHandler(priority = EventPriority.MONITOR)
public void onEntityDeath(EntityDeathEvent event) {
    if (!bossManager.hasActiveBoss()) {
        return;
    }
    
    Entity entity = event.getEntity();
    if (!entity.equals(bossManager.getActiveBoss().getEntity())) {
        return;
    }
    
    // Clear default drops
    event.getDrops().clear();
    event.setDroppedExp(0);
    
    // ADD CUSTOM REWARDS HERE
    // Example: Drop diamonds
    event.getDrops().add(new ItemStack(Material.DIAMOND, 10));
    
    // Example: Give XP to nearby players
    for (Player player : event.getEntity().getWorld().getNearbyPlayers(
        event.getEntity().getLocation(), 
        SentinelBoss.OUTER_RING_RADIUS
    )) {
        player.giveExp(100);
        player.sendMessage("§a+100 XP for defeating the Sentinel!");
    }
}
```

## License

This project is licensed under the MIT License. Feel free to use, modify, and distribute.

## Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Style

- Use 4 spaces for indentation
- Follow Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods focused and single-purpose

## Credits

**Author:** FikJul  
**Plugin:** BossJullPlugin  
**Version:** 1.0.0

---

**Enjoy the challenge of the Sentinel of the Ring!**
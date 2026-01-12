# VillagerFalseProphet - The False Prophet Boss Fight

A complete Minecraft Paper/Spigot plugin (Java 17) for version 1.20.x implementing a custom boss called "The False Prophet" - a corrupted Villager with unique mechanics.

## 🎮 Overview

The False Prophet is a challenging boss fight featuring:
- **State-based immunity system** - Boss alternates between protected and vulnerable phases
- **Dynamic minion summoning** - False Villagers that protect their master
- **Enraged phase** - Boss becomes more aggressive at low health
- **Custom summon conditions** - Players must meet specific requirements to summon the boss
- **Visual and audio effects** - Particles, sounds, and boss bar for immersive experience

**Important:** This plugin uses ONLY vanilla Spigot/Paper API - no external plugins or dependencies required!

## 📋 Features

### Boss Mechanics

#### The False Prophet
- **Entity Type:** Corrupted Villager (Cleric)
- **Health:** 500 HP (25 hearts)
- **Custom Attributes:**
  - High knockback resistance (0.8)
  - Slower movement speed (0.15, increases to 0.25 when enraged)
  - Always visible name tag
  - Persistent (won't despawn)

#### State System

The boss fight uses a sophisticated state machine:

```
    SPAWNING (3 seconds)
         ↓
    ═══ IMMUNE ═══════════════════════════════════════════════
    │  Boss is invulnerable                                  │
    │  Protected by False Followers                          │
    │  Attacking applies WEAKNESS to player                  │
    │  Red particles and boss bar                            │
    └──→ All minions defeated → VULNERABLE ──────────────────┘
                                     ↓
                              ══ VULNERABLE ════════════════════
                              │ Boss can take damage          │
                              │ Glowing effect active         │
                              │ Green particles and sounds    │
                              │ Duration: 6s (4s if enraged)  │
                              └─→ Timer expires → IMMUNE ─────┘
                                     ↓
                              HP < 30% (150 HP)
                                     ↓
                              ══ ENRAGED ════════════════════════
                              │ Same as IMMUNE/VULNERABLE     │
                              │ Spawns MORE minions (6 vs 4)  │
                              │ Shorter vulnerable time (4s)  │
                              │ Increased speed               │
                              │ Purple particles              │
                              └───────────────────────────────┘
                                     ↓
                              HP ≤ 0 → DEAD
```

### False Followers (Minions)

- **Entity Type:** Villagers with random professions
- **Name:** "§7False Follower"
- **Health:** 20 HP (normal villager)
- **Behavior:** Attack nearby players
- **Count:** 4 per wave (6 when boss is enraged)
- **Spawning:** Circular pattern around boss (5 block radius)
- **Drops:** None (no items or XP)

When all False Followers are defeated, the boss enters VULNERABLE state for a limited time!

### Summon Item: Totem of False Faith

**Item Details:**
- **Material:** Totem of Undying (with enchanted glow)
- **Display Name:** `§6Totem of False Faith`
- **Lore:**
  ```
  §7A sacred relic worshipped by the masses
  §7Only the chosen may reveal the truth
  ```

**Summon Conditions (ALL must be met):**
1. ✅ Player must RIGHT-CLICK while holding the totem
2. ✅ Player must be standing on an EMERALD BLOCK
3. ✅ Player must be in a VILLAGE biome (Plains, Desert, Savanna, Taiga, Snowy Plains, Sunflower Plains)

**Summon Effects:**
- Lightning strike (visual only, no damage)
- Bell and thunder sounds
- Explosion and villager particles
- Item is consumed from inventory

## 🎯 Combat Guide

### Strategy Tips

1. **Preparation:**
   - Gather friends - this is a tough fight!
   - Bring good armor and weapons
   - Stock up on healing potions
   - Build an arena with emerald block spawn point in a village biome

2. **During the Fight:**
   - **IMMUNE Phase:** Focus on killing False Followers quickly
   - **VULNERABLE Phase:** ALL OUT ATTACK on the boss!
   - **ENRAGED Phase:** Boss summons more minions - prioritize them!
   - Watch the boss bar color:
     - 🔴 **RED** = Immune (don't waste time attacking boss)
     - 🟢 **GREEN** = Vulnerable (ATTACK NOW!)
     - 🟣 **PURPLE** = Enraged (be careful!)

3. **Common Mistakes:**
   - Attacking boss while protected (wastes time and gives you WEAKNESS)
   - Ignoring False Followers (they keep boss immune!)
   - Not coordinating DPS during vulnerable windows

### Rewards

Upon defeating The False Prophet:
- **16-32 Emeralds**
- **1-2 Totems of Undying**
- Satisfaction of victory! 🎉

## 🛠️ Installation

### Requirements
- Minecraft Server: Paper or Spigot 1.20.x
- Java: 17 or higher

### Setup

1. **Download the plugin:**
   - Download `villager-false-prophet-1.0.0.jar` from releases
   - Or build from source (see below)

2. **Install:**
   ```bash
   # Place the JAR in your server's plugins folder
   cp villager-false-prophet-1.0.0.jar /path/to/server/plugins/
   ```

3. **Start server:**
   ```bash
   # Start or restart your Minecraft server
   java -jar paper-1.20.6.jar
   ```

4. **Verify installation:**
   ```
   # In server console or in-game
   /plugins
   # Should show: VillagerFalseProphet v1.0.0 in green
   ```

## 📦 Building from Source

### Prerequisites
- Java 17 JDK
- Maven 3.6+
- Git

### Build Steps

```bash
# Clone repository
git clone https://github.com/FikJul/boss-jull-plugin.git
cd boss-jull-plugin

# Build with Maven
mvn clean package

# Output JAR will be in target/
ls -l target/villager-false-prophet-1.0.0.jar
```

### Development Setup

```bash
# For IDE development (IntelliJ IDEA, Eclipse, VS Code)
mvn idea:idea    # IntelliJ
mvn eclipse:eclipse    # Eclipse

# Import as Maven project in your IDE
```

## 📖 Commands

### `/falseprophet give [player] [amount]`
Give the Totem of False Faith summon item.

- **Permission:** `falseprophet.give`
- **Aliases:** `/fp give`, `/prophet give`
- **Examples:**
  ```
  /falseprophet give              # Give 1 totem to yourself
  /falseprophet give Steve        # Give 1 totem to Steve
  /falseprophet give Steve 5      # Give 5 totems to Steve
  ```

### `/falseprophet summon`
Directly summon the boss at your location (bypasses normal summon conditions).

- **Permission:** `falseprophet.summon`
- **Aliases:** `/fp summon`, `/prophet summon`
- **Usage:** Admin/testing only
- **Example:**
  ```
  /falseprophet summon
  ```

### `/falseprophet help`
Display command help and summon instructions.

- **Permission:** `falseprophet.admin`
- **Aliases:** `/fp help`, `/prophet help`

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `falseprophet.admin` | Access to all commands | OP |
| `falseprophet.give` | Give summon items | OP |
| `falseprophet.summon` | Directly summon boss | OP |

### Permission Configuration

Add to your permissions plugin (LuckPerms, PermissionsEx, etc.):

```yaml
# Example for LuckPerms
/lp group admin permission set falseprophet.admin true
/lp group moderator permission set falseprophet.give true
```

## 🐛 Troubleshooting

### Boss doesn't summon when using totem

**Check:**
1. ✅ Are you standing ON an emerald block? (not next to it)
2. ✅ Are you in a village biome? Use `/minecraft:locate biome minecraft:plains`
3. ✅ Are you right-clicking (not left-clicking)?
4. ✅ Check server console for errors

**Debug:**
```bash
# Enable debug logging
/falseprophet summon  # Try direct summon to test if boss mechanics work
```

### Boss is stuck in IMMUNE state

**Cause:** False Followers may have wandered far or gotten stuck
**Fix:** 
- Kill all visible False Followers
- Check nearby caves/buildings for hidden minions
- As admin: restart the fight with `/falseprophet summon`

### Boss bar not showing

**Cause:** Player is too far from boss (>50 blocks)
**Fix:** Move closer to the boss

### Plugin doesn't load

**Check:**
1. Server version is 1.20.x (Paper or Spigot)
2. Java version is 17 or higher
3. Check server logs for errors: `logs/latest.log`
4. Verify JAR file isn't corrupted

### Performance issues during fight

**Solutions:**
- Reduce particle effects (edit source, rebuild)
- Limit number of nearby entities
- Ensure server has adequate resources
- Fight in smaller groups

## 🔧 Configuration

Currently, the plugin uses hardcoded values. To customize:

1. **Boss Health:** Edit `FalseProphetBoss.MAX_HEALTH` (default: 500.0)
2. **Minion Count:** Edit `BossManager.NORMAL_MINION_COUNT` (default: 4)
3. **Vulnerable Duration:** Edit `BossManager.NORMAL_VULNERABLE_DURATION` (default: 120 ticks = 6s)
4. **Enrage Threshold:** Edit `FalseProphetBoss.ENRAGED_THRESHOLD` (default: 30% = 150 HP)

After editing, rebuild with `mvn clean package`.

## 🎨 Technical Details

### Architecture

- **BossState.java** - Enum defining boss fight states
- **FalseProphetPlugin.java** - Main plugin class (initialization)
- **FalseProphetBoss.java** - Boss entity wrapper and attributes
- **BossManager.java** - State management, minion spawning, mechanics
- **BossListener.java** - Event handling (damage, death, interaction)
- **SummonItemManager.java** - Summon item creation and validation
- **FalseProphetCommand.java** - Command execution and tab completion

### Memory Safety

The plugin properly cleans up resources:
- All BukkitRunnable tasks are cancelled on boss death
- Entity references are cleared
- Boss bar is removed and unregistered
- Event listeners remain registered (singleton pattern)

### Performance Considerations

- Particle updates: Every 10 ticks (0.5s)
- State checking: Every 5 ticks (0.25s)
- Boss bar updates: Every 5 ticks
- All tasks cancelled when boss dies

## 📄 License

This project is provided as-is for educational and entertainment purposes.

## 👤 Author

**FikJul**

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

### Development Guidelines

1. Follow existing code style
2. Add comments for complex logic
3. Test changes thoroughly on Paper 1.20.6
4. No deprecated API usage
5. Maintain memory safety (cleanup all resources)

## 📝 Changelog

### Version 1.0.0 (Initial Release)
- ✅ Complete boss fight implementation
- ✅ State-based immunity system (SPAWNING, IMMUNE, VULNERABLE, ENRAGED, DEAD)
- ✅ Dynamic minion summoning
- ✅ Custom summon item with validation
- ✅ Boss bar with color coding
- ✅ Particle and sound effects
- ✅ Commands and permissions
- ✅ Proper cleanup and memory management
- ✅ No external dependencies (pure Spigot/Paper API)

## 🎓 Credits

Built with:
- Paper API 1.20.6
- Java 17
- Maven 3.11.0

---

**Enjoy the fight against The False Prophet! May your blade strike true during the vulnerable window! ⚔️**
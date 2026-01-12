package com.fikjul.falseprophet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Wrapper class for The False Prophet boss entity.
 * Manages the boss's state, health, and visual effects.
 */
public class FalseProphetBoss {
    
    // Constants
    public static final double MAX_HEALTH = 500.0;
    public static final long VULNERABLE_DURATION = 120L; // 6 seconds in ticks
    public static final long ENRAGED_VULNERABLE_DURATION = 60L; // 3 seconds in ticks
    public static final double ENRAGED_THRESHOLD = 150.0; // 30% of 500
    public static final int FALSE_VILLAGERS_SPAWN_COUNT = 3;
    public static final int FALSE_VILLAGERS_SPAWN_COUNT_ENRAGED = 5;
    
    private final Villager entity;
    private final BossBar bossBar;
    private final NamespacedKey bossKey;
    private BossState state;
    
    /**
     * Creates a new False Prophet boss at the specified location.
     * 
     * @param plugin The plugin instance
     * @param location The spawn location
     */
    public FalseProphetBoss(FalseProphetPlugin plugin, Location location) {
        this.bossKey = new NamespacedKey(plugin, "FalseProphetBoss");
        
        // Spawn the boss entity
        this.entity = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        
        // Configure the villager
        entity.setProfession(Villager.Profession.CLERIC);
        entity.setCustomName("§4The False Prophet");
        entity.setCustomNameVisible(true);
        entity.setAI(true);
        entity.setRemoveWhenFarAway(false);
        
        // Set attributes
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        entity.setHealth(MAX_HEALTH);
        entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8);
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
        
        // Mark as boss using PDC
        entity.getPersistentDataContainer().set(bossKey, PersistentDataType.BYTE, (byte) 1);
        
        // Create boss bar
        this.bossBar = Bukkit.createBossBar(
            "§4The False Prophet",
            BarColor.YELLOW,
            BarStyle.SEGMENTED_10
        );
        
        // Initialize state
        this.state = BossState.SPAWNING;
        entity.setInvulnerable(true);
    }
    
    /**
     * Gets the current state of the boss.
     * 
     * @return The current BossState
     */
    public BossState getState() {
        return state;
    }
    
    /**
     * Sets the boss state and updates visual effects accordingly.
     * 
     * @param state The new state
     */
    public void setState(BossState state) {
        this.state = state;
        updateBossBarColor();
        
        // Update invulnerability based on state
        if (state == BossState.VULNERABLE) {
            entity.setInvulnerable(false);
        } else if (state == BossState.IMMUNE) {
            entity.setInvulnerable(true);
        }
    }
    
    /**
     * Checks if the boss is currently vulnerable to damage.
     * 
     * @return true if the boss can take damage
     */
    public boolean isVulnerable() {
        return state == BossState.VULNERABLE;
    }
    
    /**
     * Transitions the boss to the vulnerable state.
     */
    public void enterVulnerableState() {
        setState(BossState.VULNERABLE);
        entity.setGlowing(true);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
    }
    
    /**
     * Exits the vulnerable state and returns to immune.
     */
    public void exitVulnerableState() {
        entity.setGlowing(false);
        entity.removePotionEffect(PotionEffectType.GLOWING);
        
        if (state != BossState.ENRAGED) {
            setState(BossState.IMMUNE);
        }
    }
    
    /**
     * Transitions the boss to the enraged state.
     */
    public void enterEnragedState() {
        setState(BossState.ENRAGED);
    }
    
    /**
     * Updates the boss bar color based on the current state.
     */
    private void updateBossBarColor() {
        switch (state) {
            case SPAWNING:
                bossBar.setColor(BarColor.YELLOW);
                break;
            case IMMUNE:
                bossBar.setColor(BarColor.RED);
                break;
            case VULNERABLE:
                bossBar.setColor(BarColor.GREEN);
                break;
            case ENRAGED:
                bossBar.setColor(BarColor.PURPLE);
                break;
            case DEAD:
                bossBar.setColor(BarColor.WHITE);
                break;
        }
    }
    
    /**
     * Updates the boss bar progress based on current health.
     */
    public void updateBossBar() {
        if (entity.isDead()) {
            bossBar.setProgress(0.0);
            return;
        }
        
        double healthPercentage = entity.getHealth() / MAX_HEALTH;
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, healthPercentage)));
    }
    
    /**
     * Adds a player to the boss bar.
     * 
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }
    
    /**
     * Removes a player from the boss bar.
     * 
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        bossBar.removePlayer(player);
    }
    
    /**
     * Gets the boss entity.
     * 
     * @return The Villager entity
     */
    public Villager getEntity() {
        return entity;
    }
    
    /**
     * Gets the boss location.
     * 
     * @return The current location
     */
    public Location getLocation() {
        return entity.getLocation();
    }
    
    /**
     * Gets the boss UUID.
     * 
     * @return The entity's UUID
     */
    public UUID getUniqueId() {
        return entity.getUniqueId();
    }
    
    /**
     * Gets the boss bar.
     * 
     * @return The BossBar instance
     */
    public BossBar getBossBar() {
        return bossBar;
    }
    
    /**
     * Cleans up the boss bar and removes it from all players.
     */
    public void cleanup() {
        bossBar.removeAll();
    }
}

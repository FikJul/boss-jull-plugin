package com.fikjul.falseprophet;

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

import java.util.UUID;

/**
 * Wrapper class for The False Prophet boss entity.
 * Handles boss entity configuration and attribute management.
 */
public class FalseProphetBoss {
    
    private final FalseProphetPlugin plugin;
    private final Villager boss;
    private final BossBar bossBar;
    private final UUID bossId;
    private final NamespacedKey bossKey;
    
    public static final double MAX_HEALTH = 500.0;
    public static final double ENRAGED_THRESHOLD = MAX_HEALTH * 0.3; // 30% HP = 150
    
    public FalseProphetBoss(FalseProphetPlugin plugin, Location location) {
        this.plugin = plugin;
        this.bossId = UUID.randomUUID();
        this.bossKey = new NamespacedKey(plugin, "boss_id");
        
        // Spawn villager entity
        this.boss = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        
        // Configure boss entity
        configureBoss();
        
        // Create boss bar
        this.bossBar = plugin.getServer().createBossBar(
            "§4§lThe False Prophet",
            BarColor.RED,
            BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);
    }
    
    /**
     * Configures the boss entity with custom attributes and settings.
     */
    private void configureBoss() {
        // Set custom name
        boss.setCustomName("§4The False Prophet");
        boss.setCustomNameVisible(true);
        
        // Prevent despawning
        boss.setRemoveWhenFarAway(false);
        boss.setPersistent(true);
        
        // Set villager properties
        boss.setProfession(Villager.Profession.CLERIC);
        boss.setVillagerType(Villager.Type.PLAINS);
        boss.setAdult();
        
        // Mark as boss using PersistentDataContainer
        boss.getPersistentDataContainer().set(bossKey, PersistentDataType.STRING, bossId.toString());
        
        // Set custom attributes
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        boss.setHealth(MAX_HEALTH);
        boss.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.8);
        boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
    }
    
    /**
     * Gets the villager entity representing the boss.
     * @return The boss entity
     */
    public Villager getEntity() {
        return boss;
    }
    
    /**
     * Gets the boss bar.
     * @return The boss bar
     */
    public BossBar getBossBar() {
        return bossBar;
    }
    
    /**
     * Gets the unique ID of this boss instance.
     * @return The boss UUID
     */
    public UUID getBossId() {
        return bossId;
    }
    
    /**
     * Checks if the boss is still valid (alive and loaded).
     * @return True if the boss is valid
     */
    public boolean isValid() {
        return boss != null && boss.isValid() && !boss.isDead();
    }
    
    /**
     * Gets the current health of the boss.
     * @return Current health
     */
    public double getHealth() {
        return isValid() ? boss.getHealth() : 0;
    }
    
    /**
     * Gets the current health percentage (0.0 to 1.0).
     * @return Health percentage
     */
    public double getHealthPercentage() {
        return getHealth() / MAX_HEALTH;
    }
    
    /**
     * Checks if the boss is in enraged state (HP < 30%).
     * @return True if enraged
     */
    public boolean isEnraged() {
        return getHealth() < ENRAGED_THRESHOLD && getHealth() > 0;
    }
    
    /**
     * Updates the boss bar color based on current state.
     * @param state The current boss state
     */
    public void updateBossBarColor(BossState state) {
        BarColor color;
        switch (state) {
            case VULNERABLE:
                color = BarColor.GREEN;
                break;
            case ENRAGED:
                color = BarColor.PURPLE;
                break;
            default:
                color = BarColor.RED;
                break;
        }
        bossBar.setColor(color);
    }
    
    /**
     * Updates the boss bar progress based on current health.
     */
    public void updateBossBarProgress() {
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, getHealthPercentage())));
    }
    
    /**
     * Adds a player to see the boss bar.
     * @param player The player to add
     */
    public void addBossBarPlayer(Player player) {
        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }
    }
    
    /**
     * Removes a player from seeing the boss bar.
     * @param player The player to remove
     */
    public void removeBossBarPlayer(Player player) {
        bossBar.removePlayer(player);
    }
    
    /**
     * Removes the boss and cleans up resources.
     */
    public void remove() {
        // Remove boss bar
        bossBar.removeAll();
        plugin.getServer().removeBossBar(bossBar.getKey());
        
        // Remove entity
        if (boss != null && boss.isValid()) {
            boss.remove();
        }
    }
    
    /**
     * Gets the location of the boss.
     * @return Boss location or null if invalid
     */
    public Location getLocation() {
        return isValid() ? boss.getLocation() : null;
    }
}

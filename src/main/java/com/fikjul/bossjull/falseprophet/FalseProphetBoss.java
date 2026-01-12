package com.fikjul.bossjull.falseprophet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Represents the False Prophet boss entity and manages its state and behavior.
 */
public class FalseProphetBoss {
    
    private final FalseProphetPlugin plugin;
    private final Villager bossEntity;
    private final BossBar bossBar;
    private BossState currentState;
    private final Location spawnLocation;
    private int vulnerableTicksRemaining;
    
    /**
     * Creates a new False Prophet boss instance.
     * 
     * @param plugin The plugin instance
     * @param location The location to spawn the boss
     */
    public FalseProphetBoss(FalseProphetPlugin plugin, Location location) {
        this.plugin = plugin;
        this.spawnLocation = location.clone();
        this.currentState = BossState.SPAWNING;
        this.vulnerableTicksRemaining = 0;
        
        // Spawn the villager entity
        this.bossEntity = location.getWorld().spawn(location, Villager.class);
        
        // Configure boss entity
        configureBossEntity();
        
        // Create boss bar
        this.bossBar = Bukkit.createBossBar(
            "§4The False Prophet",
            BarColor.YELLOW,
            BarStyle.SEGMENTED_10
        );
        
        updateBossBar();
    }
    
    /**
     * Configures the boss entity with appropriate attributes and settings.
     */
    private void configureBossEntity() {
        // Set custom name
        bossEntity.setCustomName("§4The False Prophet");
        bossEntity.setCustomNameVisible(true);
        
        // Set profession
        bossEntity.setProfession(Villager.Profession.CLERIC);
        
        // Make persistent
        bossEntity.setRemoveWhenFarAway(false);
        
        // Set attributes
        if (bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
            bossEntity.setHealth(500.0);
        }
        
        if (bossEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
            bossEntity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        }
        
        if (bossEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            bossEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.15);
        }
        
        // Set AI
        bossEntity.setAI(true);
        
        // Tag entity with persistent data
        NamespacedKey key = new NamespacedKey(plugin, "false_prophet_boss");
        bossEntity.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
    }
    
    /**
     * Gets the boss entity.
     * 
     * @return The Villager entity
     */
    public Villager getEntity() {
        return bossEntity;
    }
    
    /**
     * Gets the boss UUID.
     * 
     * @return The UUID of the boss entity
     */
    public UUID getUUID() {
        return bossEntity.getUniqueId();
    }
    
    /**
     * Gets the current boss state.
     * 
     * @return The current BossState
     */
    public BossState getState() {
        return currentState;
    }
    
    /**
     * Sets the boss state and updates the boss bar.
     * 
     * @param state The new state
     */
    public void setState(BossState state) {
        this.currentState = state;
        updateBossBar();
    }
    
    /**
     * Gets the spawn location of the boss.
     * 
     * @return The spawn location
     */
    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }
    
    /**
     * Checks if the boss is in immune state.
     * 
     * @return true if immune
     */
    public boolean isInImmuneState() {
        return currentState == BossState.IMMUNE || 
               (currentState == BossState.ENRAGED && plugin.getBossManager().getFalseVillagerCount(this) > 0);
    }
    
    /**
     * Checks if the boss is in vulnerable state.
     * 
     * @return true if vulnerable
     */
    public boolean isInVulnerableState() {
        return currentState == BossState.VULNERABLE && vulnerableTicksRemaining > 0;
    }
    
    /**
     * Enters the vulnerable state and starts the timer.
     * 
     * @param durationTicks The duration of vulnerability in ticks
     */
    public void enterVulnerableState(int durationTicks) {
        setState(BossState.VULNERABLE);
        this.vulnerableTicksRemaining = durationTicks;
        playVulnerableEffects();
        
        // Make boss glow
        bossEntity.setGlowing(true);
    }
    
    /**
     * Exits the vulnerable state.
     */
    public void exitVulnerableState() {
        setState(BossState.IMMUNE);
        this.vulnerableTicksRemaining = 0;
        
        // Remove glow
        bossEntity.setGlowing(false);
    }
    
    /**
     * Decrements the vulnerable timer.
     */
    public void tickVulnerableTimer() {
        if (vulnerableTicksRemaining > 0) {
            vulnerableTicksRemaining--;
        }
    }
    
    /**
     * Gets the remaining vulnerable ticks.
     * 
     * @return The number of ticks remaining
     */
    public int getVulnerableTicksRemaining() {
        return vulnerableTicksRemaining;
    }
    
    /**
     * Updates the boss bar based on current state and health.
     */
    public void updateBossBar() {
        // Update health progress
        double maxHealth = bossEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double currentHealth = bossEntity.getHealth();
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, currentHealth / maxHealth)));
        
        // Update color based on state
        switch (currentState) {
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
                bossBar.setColor(BarColor.BLUE);
                break;
        }
    }
    
    /**
     * Adds a player to the boss bar.
     * 
     * @param player The player to add
     */
    public void addPlayerToBossBar(Player player) {
        bossBar.addPlayer(player);
    }
    
    /**
     * Removes a player from the boss bar.
     * 
     * @param player The player to remove
     */
    public void removePlayerFromBossBar(Player player) {
        bossBar.removePlayer(player);
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
     * Plays effects when entering vulnerable state.
     */
    public void playVulnerableEffects() {
        Location loc = bossEntity.getLocation();
        
        // Bell sound
        loc.getWorld().playSound(loc, Sound.BLOCK_BELL_USE, 2.0f, 1.0f);
        
        // Happy villager particles
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 30, 0.5, 0.5, 0.5);
    }
    
    /**
     * Plays effects when boss spawns.
     */
    public void playSpawnEffects() {
        Location loc = bossEntity.getLocation();
        
        // Lightning effect (no damage)
        loc.getWorld().strikeLightningEffect(loc);
        
        // Sounds
        loc.getWorld().playSound(loc, Sound.BLOCK_BELL_USE, 2.0f, 0.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        
        // Particles
        loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 50, 1.0, 1.0, 1.0);
        loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc, 50, 1.0, 1.0, 1.0);
    }
    
    /**
     * Checks if the boss entity is dead or removed.
     * 
     * @return true if dead or removed
     */
    public boolean isDead() {
        return bossEntity.isDead() || !bossEntity.isValid() || currentState == BossState.DEAD;
    }
    
    /**
     * Removes the boss and cleans up.
     */
    public void remove() {
        setState(BossState.DEAD);
        bossBar.removeAll();
        if (!bossEntity.isDead()) {
            bossEntity.remove();
        }
    }
}

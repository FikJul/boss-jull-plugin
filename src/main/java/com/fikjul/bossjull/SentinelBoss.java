package com.fikjul.bossjull;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Ravager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Wrapper class for the Sentinel boss entity with ring mechanics and particle visualization.
 */
public class SentinelBoss {
    
    public static final double INNER_RING_RADIUS = 4.0;
    public static final double OUTER_RING_RADIUS = 8.0;
    public static final double MAX_HEALTH = 500.0;
    public static final double PHASE_2_THRESHOLD = 250.0;
    
    private static final String BOSS_NAME = "§c§lSentinel of the Ring";
    private static final int PARTICLES_PER_CIRCLE = 40;
    
    private final Ravager entity;
    private final BossBar bossBar;
    private final Location spawnLocation;
    private BossState currentState;
    private final Map<UUID, Location> recordedPositions;
    
    /**
     * Creates a new Sentinel boss at the specified location.
     * 
     * @param location The location to spawn the boss
     * @param bossBar The boss bar to use for this boss
     */
    public SentinelBoss(Location location, BossBar bossBar) {
        this.spawnLocation = location.clone();
        this.bossBar = bossBar;
        this.currentState = BossState.IDLE;
        this.recordedPositions = new HashMap<>();
        
        // Spawn the Ravager entity
        this.entity = (Ravager) location.getWorld().spawnEntity(location, EntityType.RAVAGER);
        this.entity.setCustomName(BOSS_NAME);
        this.entity.setCustomNameVisible(true);
        this.entity.setAI(false);
        this.entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(MAX_HEALTH);
        this.entity.setHealth(MAX_HEALTH);
        this.entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        
        // Apply permanent slowness to prevent any movement
        this.entity.addPotionEffect(new PotionEffect(
            PotionEffectType.SLOWNESS, 
            Integer.MAX_VALUE, 
            255, 
            false, 
            false
        ));
        
        // Configure boss bar
        this.bossBar.setTitle(BOSS_NAME);
        this.bossBar.setStyle(BarStyle.SEGMENTED_10);
        updateBossBarColor();
    }
    
    /**
     * Gets the boss entity.
     * 
     * @return The Ravager entity
     */
    public Ravager getEntity() {
        return entity;
    }
    
    /**
     * Gets the boss bar.
     * 
     * @return The boss bar
     */
    public BossBar getBossBar() {
        return bossBar;
    }
    
    /**
     * Gets the spawn location.
     * 
     * @return The spawn location
     */
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    /**
     * Gets the current state.
     * 
     * @return The current boss state
     */
    public BossState getCurrentState() {
        return currentState;
    }
    
    /**
     * Sets the current state and updates the boss bar color.
     * 
     * @param state The new state
     */
    public void setCurrentState(BossState state) {
        this.currentState = state;
        updateBossBarColor();
    }
    
    /**
     * Updates the boss bar color based on the current state.
     */
    private void updateBossBarColor() {
        switch (currentState) {
            case IDLE:
                bossBar.setColor(BarColor.WHITE);
                break;
            case PHASE_1_COMBAT:
                bossBar.setColor(BarColor.RED);
                break;
            case PHASE_2_TRANSITION:
                bossBar.setColor(BarColor.YELLOW);
                break;
            case PHASE_2_COMBAT:
                bossBar.setColor(BarColor.PURPLE);
                break;
            case DEAD:
                bossBar.setColor(BarColor.GREEN);
                break;
        }
    }
    
    /**
     * Updates the boss bar progress based on current health.
     */
    public void updateBossBar() {
        double progress = entity.getHealth() / MAX_HEALTH;
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
    }
    
    /**
     * Checks if a player is within the inner ring.
     * 
     * @param player The player to check
     * @return true if the player is within the inner ring
     */
    public boolean isInInnerRing(Player player) {
        if (!player.getWorld().equals(entity.getWorld())) {
            return false;
        }
        double distance = player.getLocation().distance(spawnLocation);
        return distance <= INNER_RING_RADIUS;
    }
    
    /**
     * Checks if a player is within the outer ring but not the inner ring.
     * 
     * @param player The player to check
     * @return true if the player is in the outer ring (but not inner)
     */
    public boolean isInOuterRing(Player player) {
        if (!player.getWorld().equals(entity.getWorld())) {
            return false;
        }
        double distance = player.getLocation().distance(spawnLocation);
        return distance > INNER_RING_RADIUS && distance <= OUTER_RING_RADIUS;
    }
    
    /**
     * Checks if a player is within the arena (outer ring or closer).
     * 
     * @param player The player to check
     * @return true if the player is within the arena
     */
    public boolean isInArena(Player player) {
        if (!player.getWorld().equals(entity.getWorld())) {
            return false;
        }
        double distance = player.getLocation().distance(spawnLocation);
        return distance <= OUTER_RING_RADIUS;
    }
    
    /**
     * Spawns ring particles based on the current phase.
     */
    public void spawnRingParticles() {
        Particle innerParticle;
        Particle outerParticle;
        
        // Determine particle types based on phase
        if (currentState == BossState.PHASE_2_COMBAT) {
            // Phase 2: Inner = red, Outer = purple
            innerParticle = Particle.FLAME;
            outerParticle = Particle.ENCHANTED_HIT;
        } else {
            // Phase 1: Inner = purple, Outer = red
            innerParticle = Particle.ENCHANTED_HIT;
            outerParticle = Particle.FLAME;
        }
        
        // Spawn inner ring particles
        spawnCircleParticles(INNER_RING_RADIUS, innerParticle);
        
        // Spawn outer ring particles
        spawnCircleParticles(OUTER_RING_RADIUS, outerParticle);
    }
    
    /**
     * Spawns particles in a circle at the specified radius.
     * 
     * @param radius The radius of the circle
     * @param particle The particle type to spawn
     */
    private void spawnCircleParticles(double radius, Particle particle) {
        Location center = spawnLocation.clone();
        
        for (int i = 0; i < PARTICLES_PER_CIRCLE; i++) {
            double angle = 2 * Math.PI * i / PARTICLES_PER_CIRCLE;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            Location particleLocation = new Location(center.getWorld(), x, center.getY() + 0.1, z);
            center.getWorld().spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Plays the slam charge effects (particles and sound).
     */
    public void playSlamChargeEffects() {
        entity.getWorld().spawnParticle(
            Particle.EXPLOSION, 
            spawnLocation.clone().add(0, 1, 0), 
            10, 
            1, 1, 1, 
            0
        );
        entity.getWorld().playSound(spawnLocation, Sound.ENTITY_RAVAGER_ROAR, 2.0f, 0.8f);
    }
    
    /**
     * Plays the slam execute effects (particles and sound).
     */
    public void playSlamExecuteEffects() {
        entity.getWorld().spawnParticle(
            Particle.EXPLOSION_EMITTER, 
            spawnLocation.clone().add(0, 1, 0), 
            5, 
            0.5, 0.5, 0.5, 
            0
        );
        entity.getWorld().playSound(spawnLocation, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
    }
    
    /**
     * Records a player's current position for slam attack validation.
     * 
     * @param player The player whose position to record
     */
    public void recordPlayerPosition(Player player) {
        recordedPositions.put(player.getUniqueId(), player.getLocation().clone());
    }
    
    /**
     * Gets a player's recorded position.
     * 
     * @param player The player
     * @return The recorded location, or null if not recorded
     */
    public Location getRecordedPosition(Player player) {
        return recordedPositions.get(player.getUniqueId());
    }
    
    /**
     * Clears all recorded player positions.
     */
    public void clearRecordedPositions() {
        recordedPositions.clear();
    }
    
    /**
     * Removes the boss entity and cleans up resources.
     */
    public void remove() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
        if (bossBar != null) {
            bossBar.removeAll();
        }
        recordedPositions.clear();
    }
}

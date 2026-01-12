package com.fikjul.falseprophet;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages The False Prophet boss fight mechanics, including state transitions,
 * minion spawning, and phase management.
 */
public class BossManager {
    
    private final FalseProphetPlugin plugin;
    private final NamespacedKey minionKey;
    
    private FalseProphetBoss activeBoss;
    private BossState currentState;
    private final Set<UUID> falseVillagers;
    private BukkitTask stateTask;
    private BukkitTask particleTask;
    private BukkitTask vulnerableTimer;
    
    private static final int NORMAL_MINION_COUNT = 4;
    private static final int ENRAGED_MINION_COUNT = 6;
    private static final int NORMAL_VULNERABLE_DURATION = 120; // 6 seconds (in ticks)
    private static final int ENRAGED_VULNERABLE_DURATION = 80; // 4 seconds (in ticks)
    
    public BossManager(FalseProphetPlugin plugin) {
        this.plugin = plugin;
        this.minionKey = new NamespacedKey(plugin, "minion");
        this.falseVillagers = new HashSet<>();
        this.currentState = BossState.DEAD;
    }
    
    /**
     * Spawns The False Prophet boss at the specified location.
     * @param location The spawn location
     */
    public void spawnBoss(Location location) {
        // Clean up any existing boss
        if (activeBoss != null && activeBoss.isValid()) {
            cleanup();
        }
        
        // Create new boss
        activeBoss = new FalseProphetBoss(plugin, location);
        currentState = BossState.SPAWNING;
        falseVillagers.clear();
        
        // Play spawn effects
        playSpawnEffects(location);
        
        // Start spawning state (3 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBoss != null && activeBoss.isValid()) {
                    // Summon initial wave
                    summonFalseVillagers(NORMAL_MINION_COUNT);
                    // Transition to IMMUNE state
                    transitionToState(BossState.IMMUNE);
                }
            }
        }.runTaskLater(plugin, 60); // 3 seconds
        
        // Start periodic tasks
        startPeriodicTasks();
        
        plugin.getLogger().info("The False Prophet has been summoned!");
    }
    
    /**
     * Plays visual and sound effects when the boss spawns.
     * @param location The spawn location
     */
    private void playSpawnEffects(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        // Lightning effect (visual only)
        world.strikeLightningEffect(location);
        
        // Sounds
        world.playSound(location, Sound.BLOCK_BELL_USE, 2.0f, 0.5f);
        world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        
        // Particles
        world.spawnParticle(Particle.VILLAGER_HAPPY, location, 50, 2, 2, 2, 0);
        world.spawnParticle(Particle.EXPLOSION_LARGE, location, 5, 0.5, 0.5, 0.5, 0);
    }
    
    /**
     * Transitions the boss to a new state.
     * @param newState The new state
     */
    public void transitionToState(BossState newState) {
        if (currentState == newState) return;
        
        plugin.getLogger().info("Boss state transition: " + currentState + " -> " + newState);
        
        BossState oldState = currentState;
        currentState = newState;
        
        // Cancel vulnerable timer if active
        if (vulnerableTimer != null) {
            vulnerableTimer.cancel();
            vulnerableTimer = null;
        }
        
        // Handle state-specific logic
        switch (newState) {
            case IMMUNE:
                handleImmuneState();
                break;
            case VULNERABLE:
                handleVulnerableState(oldState == BossState.ENRAGED);
                break;
            case ENRAGED:
                handleEnragedState();
                break;
            case DEAD:
                handleDeathState();
                break;
        }
        
        // Update boss bar color
        if (activeBoss != null) {
            activeBoss.updateBossBarColor(newState);
        }
    }
    
    /**
     * Handles IMMUNE state logic.
     */
    private void handleImmuneState() {
        // Boss is protected, nothing special to do
    }
    
    /**
     * Handles VULNERABLE state logic.
     * @param isEnraged Whether the boss is enraged
     */
    private void handleVulnerableState(boolean isEnraged) {
        if (activeBoss == null || !activeBoss.isValid()) return;
        
        // Apply glowing effect
        activeBoss.getEntity().addPotionEffect(
            new PotionEffect(PotionEffectType.GLOWING, 200, 0, false, false)
        );
        
        // Notify players
        Location bossLoc = activeBoss.getLocation();
        if (bossLoc != null) {
            for (Player player : bossLoc.getWorld().getPlayers()) {
                if (player.getLocation().distance(bossLoc) <= 50) {
                    player.sendMessage("§a§lThe False Prophet is exposed! ATTACK NOW!");
                }
            }
        }
        
        // Start vulnerable timer
        int duration = isEnraged ? ENRAGED_VULNERABLE_DURATION : NORMAL_VULNERABLE_DURATION;
        vulnerableTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBoss != null && activeBoss.isValid()) {
                    // Return to IMMUNE and summon new wave
                    int minionCount = activeBoss.isEnraged() ? ENRAGED_MINION_COUNT : NORMAL_MINION_COUNT;
                    summonFalseVillagers(minionCount);
                    transitionToState(activeBoss.isEnraged() ? BossState.ENRAGED : BossState.IMMUNE);
                }
            }
        }.runTaskLater(plugin, duration);
    }
    
    /**
     * Handles ENRAGED state logic.
     */
    private void handleEnragedState() {
        if (activeBoss == null || !activeBoss.isValid()) return;
        
        // Increase movement speed
        activeBoss.getEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);
        
        // Add speed effect
        activeBoss.getEntity().addPotionEffect(
            new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false)
        );
        
        // Notify players
        Location bossLoc = activeBoss.getLocation();
        if (bossLoc != null) {
            for (Player player : bossLoc.getWorld().getPlayers()) {
                if (player.getLocation().distance(bossLoc) <= 50) {
                    player.sendMessage("§5§lThe False Prophet enters a rage!");
                    player.playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.0f);
                }
            }
        }
    }
    
    /**
     * Handles DEAD state logic (boss defeated).
     */
    private void handleDeathState() {
        if (activeBoss == null) return;
        
        Location deathLoc = activeBoss.getLocation();
        if (deathLoc != null) {
            World world = deathLoc.getWorld();
            
            // Play death effects
            world.spawnParticle(Particle.EXPLOSION_HUGE, deathLoc, 10, 1, 1, 1, 0);
            world.playSound(deathLoc, Sound.ENTITY_VILLAGER_DEATH, 2.0f, 0.5f);
            world.strikeLightningEffect(deathLoc);
            
            // Drop rewards
            world.dropItemNaturally(deathLoc, new org.bukkit.inventory.ItemStack(Material.EMERALD, 16 + new Random().nextInt(17))); // 16-32
            world.dropItemNaturally(deathLoc, new org.bukkit.inventory.ItemStack(Material.TOTEM_OF_UNDYING, 1 + new Random().nextInt(2))); // 1-2
        }
        
        // Remove all false villagers
        removeAllMinions();
        
        // Clean up
        cleanup();
    }
    
    /**
     * Summons false villager minions around the boss.
     * @param count Number of minions to summon
     */
    private void summonFalseVillagers(int count) {
        if (activeBoss == null || !activeBoss.isValid()) return;
        
        Location bossLoc = activeBoss.getLocation();
        if (bossLoc == null) return;
        
        World world = bossLoc.getWorld();
        double radius = 5.0;
        
        // Summon villagers in a circle around the boss
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = bossLoc.getX() + radius * Math.cos(angle);
            double z = bossLoc.getZ() + radius * Math.sin(angle);
            Location spawnLoc = new Location(world, x, bossLoc.getY(), z);
            
            // Spawn villager
            Villager minion = (Villager) world.spawnEntity(spawnLoc, EntityType.VILLAGER);
            
            // Configure minion
            minion.setCustomName("§7False Follower");
            minion.setCustomNameVisible(true);
            minion.setProfession(Villager.Profession.values()[new Random().nextInt(Villager.Profession.values().length)]);
            minion.setAdult();
            
            // Mark as minion
            minion.getPersistentDataContainer().set(minionKey, PersistentDataType.STRING, activeBoss.getBossId().toString());
            
            // Track minion
            falseVillagers.add(minion.getUniqueId());
            
            // Spawn effects
            world.spawnParticle(Particle.VILLAGER_ANGRY, spawnLoc, 20, 0.5, 0.5, 0.5, 0);
            world.playSound(spawnLoc, Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 0.8f);
        }
        
        plugin.getLogger().info("Summoned " + count + " false villagers");
    }
    
    /**
     * Handles when a false villager dies.
     * @param minionUuid The UUID of the dead minion
     */
    public void onMinionDeath(UUID minionUuid) {
        falseVillagers.remove(minionUuid);
        
        plugin.getLogger().info("False villager died. Remaining: " + falseVillagers.size());
        
        // Check if all minions are dead
        if (falseVillagers.isEmpty() && activeBoss != null && activeBoss.isValid()) {
            // Transition to VULNERABLE state
            if (activeBoss.isEnraged()) {
                transitionToState(BossState.VULNERABLE);
                // Keep ENRAGED flag by immediately checking HP
                currentState = BossState.ENRAGED;
                handleVulnerableState(true);
            } else {
                transitionToState(BossState.VULNERABLE);
            }
        }
    }
    
    /**
     * Handles boss damage events.
     * @param damage The damage amount
     */
    public void onBossDamage(double damage) {
        if (activeBoss == null || !activeBoss.isValid()) return;
        
        // Update boss bar
        activeBoss.updateBossBarProgress();
        
        // Check for enraged state
        if (!activeBoss.isEnraged() && activeBoss.getHealth() < FalseProphetBoss.ENRAGED_THRESHOLD) {
            // Transition to ENRAGED (but keep current vulnerability if applicable)
            if (currentState == BossState.VULNERABLE) {
                currentState = BossState.ENRAGED;
                handleEnragedState();
            } else {
                transitionToState(BossState.ENRAGED);
            }
        }
        
        // Check for death
        if (activeBoss.getHealth() <= 0) {
            transitionToState(BossState.DEAD);
        }
    }
    
    /**
     * Checks if damage should be blocked based on current state.
     * @return True if damage should be blocked
     */
    public boolean shouldBlockDamage() {
        return currentState == BossState.SPAWNING || 
               currentState == BossState.IMMUNE ||
               (currentState == BossState.ENRAGED && !falseVillagers.isEmpty());
    }
    
    /**
     * Starts periodic tasks for particles and state checking.
     */
    private void startPeriodicTasks() {
        // Particle task (every 10 ticks)
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBoss == null || !activeBoss.isValid()) {
                    cancel();
                    return;
                }
                
                updateParticles();
            }
        }.runTaskTimer(plugin, 0, 10);
        
        // State checking task (every 5 ticks)
        stateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBoss == null || !activeBoss.isValid()) {
                    cancel();
                    return;
                }
                
                updateBossBar();
            }
        }.runTaskTimer(plugin, 0, 5);
    }
    
    /**
     * Updates particle effects based on current state.
     */
    private void updateParticles() {
        Location loc = activeBoss.getLocation();
        if (loc == null) return;
        
        World world = loc.getWorld();
        
        switch (currentState) {
            case IMMUNE:
                // Red particles circling
                world.spawnParticle(Particle.REDSTONE, loc.clone().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0,
                    new Particle.DustOptions(Color.RED, 1.0f));
                break;
            case VULNERABLE:
                // Green + villager happy particles
                world.spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.REDSTONE, loc.clone().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0,
                    new Particle.DustOptions(Color.GREEN, 1.0f));
                // Play bell sound periodically
                if (world.getGameTime() % 40 == 0) { // Every 2 seconds
                    world.playSound(loc, Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
                }
                break;
            case ENRAGED:
                // Purple + angry villager particles
                world.spawnParticle(Particle.VILLAGER_ANGRY, loc.clone().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.REDSTONE, loc.clone().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0,
                    new Particle.DustOptions(Color.PURPLE, 1.0f));
                break;
            case SPAWNING:
                // Explosion + portal particles
                world.spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                break;
        }
    }
    
    /**
     * Updates the boss bar for nearby players.
     */
    private void updateBossBar() {
        Location bossLoc = activeBoss.getLocation();
        if (bossLoc == null) return;
        
        // Add nearby players to boss bar
        for (Player player : bossLoc.getWorld().getPlayers()) {
            if (player.getLocation().distance(bossLoc) <= 50) {
                activeBoss.addBossBarPlayer(player);
            } else {
                activeBoss.removeBossBarPlayer(player);
            }
        }
        
        // Update boss bar progress
        activeBoss.updateBossBarProgress();
    }
    
    /**
     * Removes all false villager minions.
     */
    private void removeAllMinions() {
        for (UUID uuid : new HashSet<>(falseVillagers)) {
            org.bukkit.entity.Entity entity = plugin.getServer().getEntity(uuid);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        falseVillagers.clear();
    }
    
    /**
     * Gets the active boss instance.
     * @return The active boss or null
     */
    public FalseProphetBoss getActiveBoss() {
        return activeBoss;
    }
    
    /**
     * Gets the current boss state.
     * @return The current state
     */
    public BossState getCurrentState() {
        return currentState;
    }
    
    /**
     * Checks if an entity is the active boss.
     * @param entity The entity to check
     * @return True if it's the boss
     */
    public boolean isBoss(org.bukkit.entity.Entity entity) {
        return activeBoss != null && activeBoss.getEntity().equals(entity);
    }
    
    /**
     * Checks if an entity is a false villager minion.
     * @param entity The entity to check
     * @return True if it's a minion
     */
    public boolean isMinion(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof Villager)) return false;
        
        Villager villager = (Villager) entity;
        return villager.getPersistentDataContainer().has(minionKey, PersistentDataType.STRING);
    }
    
    /**
     * Cleans up the boss fight and all resources.
     */
    public void cleanup() {
        // Cancel tasks
        if (stateTask != null) {
            stateTask.cancel();
            stateTask = null;
        }
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        if (vulnerableTimer != null) {
            vulnerableTimer.cancel();
            vulnerableTimer = null;
        }
        
        // Remove minions
        removeAllMinions();
        
        // Remove boss
        if (activeBoss != null) {
            activeBoss.remove();
            activeBoss = null;
        }
        
        currentState = BossState.DEAD;
        plugin.getLogger().info("Boss fight cleaned up");
    }
}

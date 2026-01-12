package com.fikjul.falseprophet;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the boss instance, state transitions, and minion tracking.
 */
public class BossManager {
    
    private final FalseProphetPlugin plugin;
    private final NamespacedKey bossKey;
    private final NamespacedKey falseVillagerKey;
    
    private FalseProphetBoss boss;
    private Set<UUID> falseVillagers;
    private BukkitTask particleTask;
    private BukkitTask mechanicCheckTask;
    private BukkitTask vulnerableTimerTask;
    private boolean isEnraged = false;
    
    /**
     * Creates a new BossManager.
     * 
     * @param plugin The plugin instance
     */
    public BossManager(FalseProphetPlugin plugin) {
        this.plugin = plugin;
        this.bossKey = new NamespacedKey(plugin, "FalseProphetBoss");
        this.falseVillagerKey = new NamespacedKey(plugin, "FalseVillager");
        this.falseVillagers = new HashSet<>();
    }
    
    /**
     * Spawns a new boss at the specified location.
     * 
     * @param location The spawn location
     * @return The spawned boss instance
     */
    public FalseProphetBoss spawnBoss(Location location) {
        if (boss != null && !boss.getEntity().isDead()) {
            return null; // Boss already exists
        }
        
        // Create new boss
        boss = new FalseProphetBoss(plugin, location);
        falseVillagers.clear();
        isEnraged = false;
        
        // Add nearby players to boss bar
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= 100) {
                boss.addPlayer(player);
            }
        }
        
        // Start tasks
        startTasks();
        
        // Schedule transition to IMMUNE state after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss != null && !boss.getEntity().isDead()) {
                    boss.setState(BossState.IMMUNE);
                    spawnFalseVillagers(FalseProphetBoss.FALSE_VILLAGERS_SPAWN_COUNT);
                }
            }
        }.runTaskLater(plugin, 40L); // 2 seconds
        
        return boss;
    }
    
    /**
     * Starts all background tasks for the boss.
     */
    private void startTasks() {
        // Particle task - every 10 ticks (0.5s)
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss == null || boss.getEntity().isDead()) {
                    cancel();
                    return;
                }
                
                spawnStateParticles();
            }
        }.runTaskTimer(plugin, 0L, 10L);
        
        // Mechanic check task - every 5 ticks (0.25s)
        mechanicCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss == null || boss.getEntity().isDead()) {
                    cancel();
                    return;
                }
                
                checkMechanics();
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
    
    /**
     * Spawns particles based on the current boss state.
     */
    private void spawnStateParticles() {
        Location loc = boss.getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        
        switch (boss.getState()) {
            case IMMUNE:
                // Red particles
                world.spawnParticle(Particle.REDSTONE, loc, 5, 0.5, 0.5, 0.5, 
                    new Particle.DustOptions(Color.RED, 1.0f));
                break;
            case VULNERABLE:
                // Green particles
                world.spawnParticle(Particle.VILLAGER_HAPPY, loc, 5, 0.5, 0.5, 0.5);
                break;
            case ENRAGED:
                // Purple particles
                world.spawnParticle(Particle.DRAGON_BREATH, loc, 5, 0.5, 0.5, 0.5);
                break;
        }
    }
    
    /**
     * Checks boss mechanics and handles state transitions.
     */
    private void checkMechanics() {
        // Update boss bar
        boss.updateBossBar();
        
        // Check for enraged state transition
        if (!isEnraged && boss.getEntity().getHealth() <= FalseProphetBoss.ENRAGED_THRESHOLD) {
            isEnraged = true;
            boss.enterEnragedState();
            
            // Broadcast message
            for (Player player : boss.getEntity().getWorld().getPlayers()) {
                player.sendMessage("§c§l⚠ The False Prophet has become ENRAGED! ⚠");
            }
        }
        
        // Check false villager count
        if (boss.getState() == BossState.IMMUNE || boss.getState() == BossState.ENRAGED) {
            int aliveCount = countAliveFalseVillagers();
            
            if (aliveCount == 0) {
                // Transition to vulnerable
                enterVulnerableState();
            }
        }
    }
    
    /**
     * Counts the number of alive false villagers.
     * 
     * @return The count of alive false villagers
     */
    private int countAliveFalseVillagers() {
        int count = 0;
        Set<UUID> toRemove = new HashSet<>();
        
        for (UUID uuid : falseVillagers) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null || entity.isDead()) {
                toRemove.add(uuid);
            } else {
                count++;
            }
        }
        
        // Clean up dead villagers
        falseVillagers.removeAll(toRemove);
        
        return count;
    }
    
    /**
     * Transitions the boss to vulnerable state.
     */
    private void enterVulnerableState() {
        boss.enterVulnerableState();
        
        // Play sound effect
        boss.getLocation().getWorld().playSound(
            boss.getLocation(),
            Sound.BLOCK_BELL_USE,
            2.0f,
            0.8f
        );
        
        // Notify players
        for (Player player : boss.getEntity().getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= 100) {
                player.sendMessage("§a§lThe False Prophet is now VULNERABLE!");
            }
        }
        
        // Start vulnerable timer
        long duration = isEnraged ? FalseProphetBoss.ENRAGED_VULNERABLE_DURATION : FalseProphetBoss.VULNERABLE_DURATION;
        
        // Cancel existing timer if any
        if (vulnerableTimerTask != null) {
            vulnerableTimerTask.cancel();
        }
        
        vulnerableTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (boss != null && !boss.getEntity().isDead() && boss.isVulnerable()) {
                    exitVulnerableState();
                }
            }
        }.runTaskLater(plugin, duration);
    }
    
    /**
     * Exits the vulnerable state and returns to immune.
     */
    private void exitVulnerableState() {
        boss.exitVulnerableState();
        
        // Spawn new false villagers
        int count = isEnraged ? FalseProphetBoss.FALSE_VILLAGERS_SPAWN_COUNT_ENRAGED : FalseProphetBoss.FALSE_VILLAGERS_SPAWN_COUNT;
        spawnFalseVillagers(count);
        
        // Set state back to appropriate state
        if (isEnraged) {
            boss.setState(BossState.ENRAGED);
        } else {
            boss.setState(BossState.IMMUNE);
        }
        
        // Notify players
        for (Player player : boss.getEntity().getWorld().getPlayers()) {
            if (player.getLocation().distance(boss.getLocation()) <= 100) {
                player.sendMessage("§c§lThe False Prophet is protected again!");
            }
        }
    }
    
    /**
     * Spawns false villagers around the boss.
     * 
     * @param count Number of villagers to spawn
     */
    public void spawnFalseVillagers(int count) {
        Location bossLoc = boss.getLocation();
        World world = bossLoc.getWorld();
        
        for (int i = 0; i < count; i++) {
            // Random offset around boss (3-5 blocks)
            double angle = Math.random() * 2 * Math.PI;
            double radius = 3 + Math.random() * 2;
            double x = bossLoc.getX() + radius * Math.cos(angle);
            double z = bossLoc.getZ() + radius * Math.sin(angle);
            
            Location spawnLoc = new Location(world, x, bossLoc.getY(), z);
            
            // Ensure valid spawn location
            if (!spawnLoc.getBlock().getType().isSolid()) {
                spawnLoc = spawnLoc.getWorld().getHighestBlockAt(spawnLoc).getLocation().add(0, 1, 0);
            }
            
            Villager villager = (Villager) world.spawnEntity(spawnLoc, EntityType.VILLAGER);
            villager.setCustomName("§7False Follower");
            villager.setCustomNameVisible(true);
            villager.setProfession(Villager.Profession.NITWIT);
            villager.setAI(true);
            
            // Mark as false villager
            PersistentDataContainer pdc = villager.getPersistentDataContainer();
            pdc.set(falseVillagerKey, PersistentDataType.STRING, boss.getUniqueId().toString());
            
            // Track UUID
            falseVillagers.add(villager.getUniqueId());
            
            // Spawn effect
            world.spawnParticle(Particle.PORTAL, spawnLoc, 30, 0.5, 0.5, 0.5);
        }
    }
    
    /**
     * Handles damage to the boss.
     * 
     * @param attacker The attacking player
     * @return true if damage should be allowed, false if cancelled
     */
    public boolean handleBossDamage(Player attacker) {
        if (boss == null || boss.getEntity().isDead()) {
            return false;
        }
        
        if (boss.getState() == BossState.IMMUNE || boss.getState() == BossState.ENRAGED && !boss.isVulnerable()) {
            // Boss is immune
            attacker.sendMessage("§c§lThe False Prophet is protected by his followers!");
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
            return false;
        }
        
        // Update boss bar
        boss.updateBossBar();
        
        return true;
    }
    
    /**
     * Handles the death of a false villager.
     * 
     * @param uuid The UUID of the dead villager
     */
    public void handleFalseVillagerDeath(UUID uuid) {
        falseVillagers.remove(uuid);
    }
    
    /**
     * Handles the boss death.
     */
    public void handleBossDeath() {
        if (boss == null) {
            return;
        }
        
        boss.setState(BossState.DEAD);
        
        // Broadcast victory message
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§a§l  The False Prophet has been defeated!");
        Bukkit.broadcastMessage("§6§l========================================");
        
        // Cleanup after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanup();
            }
        }.runTaskLater(plugin, 100L);
    }
    
    /**
     * Cleans up the boss and all tasks.
     */
    private void cleanup() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        
        if (mechanicCheckTask != null) {
            mechanicCheckTask.cancel();
            mechanicCheckTask = null;
        }
        
        if (vulnerableTimerTask != null) {
            vulnerableTimerTask.cancel();
            vulnerableTimerTask = null;
        }
        
        if (boss != null) {
            boss.cleanup();
            boss = null;
        }
        
        falseVillagers.clear();
        isEnraged = false;
    }
    
    /**
     * Shuts down the manager and cleans up all resources.
     */
    public void shutdown() {
        cleanup();
    }
    
    /**
     * Gets the current boss instance.
     * 
     * @return The current boss, or null if no boss is active
     */
    public FalseProphetBoss getBoss() {
        return boss;
    }
    
    /**
     * Checks if a boss is currently active.
     * 
     * @return true if a boss exists and is alive
     */
    public boolean hasBoss() {
        return boss != null && !boss.getEntity().isDead();
    }
    
    /**
     * Gets the boss key for PDC checks.
     * 
     * @return The NamespacedKey for boss identification
     */
    public NamespacedKey getBossKey() {
        return bossKey;
    }
    
    /**
     * Gets the false villager key for PDC checks.
     * 
     * @return The NamespacedKey for false villager identification
     */
    public NamespacedKey getFalseVillagerKey() {
        return falseVillagerKey;
    }
}

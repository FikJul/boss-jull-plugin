package com.fikjul.bossjull.falseprophet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Manages all False Prophet boss instances and coordinates their behavior.
 */
public class BossManager {
    
    // Constants
    private static final long VULNERABLE_DURATION = 120L; // 6 seconds
    private static final long ENRAGED_VULNERABLE_DURATION = 80L; // 4 seconds
    private static final double ENRAGED_THRESHOLD = 0.3; // 30% HP
    private static final int MINION_SPAWN_COUNT_NORMAL = 3;
    private static final int MINION_SPAWN_COUNT_ENRAGED = 5;
    private static final long SPAWNING_DURATION = 40L; // 2 seconds
    
    private final FalseProphetPlugin plugin;
    private final Map<UUID, FalseProphetBoss> activeBosses;
    private final Map<UUID, UUID> falseVillagerToBoss; // Villager UUID -> Boss UUID
    private BukkitRunnable stateCheckTask;
    
    /**
     * Creates a new BossManager instance.
     * 
     * @param plugin The plugin instance
     */
    public BossManager(FalseProphetPlugin plugin) {
        this.plugin = plugin;
        this.activeBosses = new HashMap<>();
        this.falseVillagerToBoss = new HashMap<>();
        
        startStateCheckTask();
    }
    
    /**
     * Starts the periodic state check task.
     */
    private void startStateCheckTask() {
        stateCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (FalseProphetBoss boss : new ArrayList<>(activeBosses.values())) {
                    updateBossState(boss);
                }
            }
        };
        stateCheckTask.runTaskTimer(plugin, 10L, 10L); // Every 0.5 seconds
    }
    
    /**
     * Updates the state of a boss based on current conditions.
     * 
     * @param boss The boss to update
     */
    private void updateBossState(FalseProphetBoss boss) {
        if (boss.isDead()) {
            removeBoss(boss.getUUID());
            return;
        }
        
        Villager entity = boss.getEntity();
        double maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double currentHealth = entity.getHealth();
        double healthPercentage = currentHealth / maxHealth;
        
        BossState currentState = boss.getState();
        
        // Check for enraged state
        if (healthPercentage < ENRAGED_THRESHOLD && currentState != BossState.ENRAGED && currentState != BossState.DEAD) {
            if (currentState != BossState.SPAWNING) {
                boss.setState(BossState.ENRAGED);
                broadcastMessage(entity.getLocation(), "§5The False Prophet is enraged!");
                
                // Speed boost
                if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                    entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
                }
            }
        }
        
        // Handle spawning state
        if (currentState == BossState.SPAWNING) {
            // Transition to immune after spawning duration
            // This is handled by summonBoss method with a delayed task
            return;
        }
        
        // Handle vulnerable timer
        if (currentState == BossState.VULNERABLE) {
            boss.tickVulnerableTimer();
            
            if (boss.getVulnerableTicksRemaining() <= 0) {
                // Vulnerable window expired - spawn new minions
                boss.exitVulnerableState();
                int minionCount = boss.getState() == BossState.ENRAGED ? 
                    MINION_SPAWN_COUNT_ENRAGED : MINION_SPAWN_COUNT_NORMAL;
                spawnFalseVillagers(boss, minionCount);
                broadcastMessage(boss.getEntity().getLocation(), 
                    "§cThe False Prophet summons more followers!");
            }
        }
        
        // Check if should transition to vulnerable
        if ((currentState == BossState.IMMUNE || currentState == BossState.ENRAGED) && 
            !boss.isInVulnerableState()) {
            int minionCount = getFalseVillagerCount(boss);
            
            if (minionCount == 0 && boss.getVulnerableTicksRemaining() == 0) {
                // No minions left - enter vulnerable state
                int duration = currentState == BossState.ENRAGED ? 
                    (int) ENRAGED_VULNERABLE_DURATION : (int) VULNERABLE_DURATION;
                boss.enterVulnerableState(duration);
                broadcastMessage(boss.getEntity().getLocation(), 
                    "§aThe False Prophet is exposed!");
            }
        }
        
        // Update boss bar
        boss.updateBossBar();
        
        // Update boss bar for nearby players
        updateBossBarPlayers(boss);
    }
    
    /**
     * Updates the boss bar visibility for nearby players.
     * 
     * @param boss The boss
     */
    private void updateBossBarPlayers(FalseProphetBoss boss) {
        Location bossLoc = boss.getEntity().getLocation();
        double range = 50.0;
        
        // Add nearby players
        for (Player player : bossLoc.getWorld().getPlayers()) {
            if (player.getLocation().distance(bossLoc) <= range) {
                boss.addPlayerToBossBar(player);
            } else {
                boss.removePlayerFromBossBar(player);
            }
        }
    }
    
    /**
     * Validates summon conditions for the boss.
     * 
     * @param player The player attempting to summon
     * @param location The location where summon is attempted
     * @return Validation result with success status and message
     */
    public ValidationResult validateSummonConditions(Player player, Location location) {
        // Check if player is standing on emerald block
        Location blockBelow = location.clone().subtract(0, 1, 0);
        if (blockBelow.getBlock().getType() != Material.EMERALD_BLOCK) {
            return new ValidationResult(false, "§cYou must stand on an Emerald Block to summon The False Prophet!");
        }
        
        // Check if location is in a village biome
        Biome biome = location.getBlock().getBiome();
        if (!isVillageBiome(biome)) {
            return new ValidationResult(false, "§cYou must be in a Village biome to summon The False Prophet!");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Checks if a biome is a village biome.
     * 
     * @param biome The biome to check
     * @return true if it's a village biome
     */
    private boolean isVillageBiome(Biome biome) {
        return biome == Biome.PLAINS || 
               biome == Biome.SUNFLOWER_PLAINS ||
               biome == Biome.DESERT ||
               biome == Biome.SAVANNA ||
               biome == Biome.TAIGA ||
               biome == Biome.SNOWY_PLAINS;
    }
    
    /**
     * Summons a new False Prophet boss.
     * 
     * @param location The location to summon the boss
     * @return The created boss instance
     */
    public FalseProphetBoss summonBoss(Location location) {
        FalseProphetBoss boss = new FalseProphetBoss(plugin, location);
        activeBosses.put(boss.getUUID(), boss);
        
        // Play spawn effects
        boss.playSpawnEffects();
        
        // Transition from SPAWNING to IMMUNE after delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!boss.isDead() && boss.getState() == BossState.SPAWNING) {
                    boss.setState(BossState.IMMUNE);
                    spawnFalseVillagers(boss, MINION_SPAWN_COUNT_NORMAL);
                }
            }
        }.runTaskLater(plugin, SPAWNING_DURATION);
        
        return boss;
    }
    
    /**
     * Spawns false villagers around the boss.
     * 
     * @param boss The boss to spawn minions for
     * @param count The number of minions to spawn
     */
    public void spawnFalseVillagers(FalseProphetBoss boss, int count) {
        Location bossLoc = boss.getEntity().getLocation();
        NamespacedKey key = new NamespacedKey(plugin, "false_villager");
        
        for (int i = 0; i < count; i++) {
            // Calculate spawn position in a circle
            double angle = (2 * Math.PI * i) / count;
            double radius = 5.0 + (Math.random() * 3.0); // 5-8 blocks
            double x = bossLoc.getX() + (radius * Math.cos(angle));
            double z = bossLoc.getZ() + (radius * Math.sin(angle));
            
            Location spawnLoc = new Location(bossLoc.getWorld(), x, bossLoc.getY(), z);
            
            // Find a safe spawn location (don't destroy important blocks)
            while (spawnLoc.getBlock().getType().isSolid() && 
                   spawnLoc.getBlock().getType() != Material.AIR) {
                spawnLoc.add(0, 1, 0);
                // Prevent infinite loop if location is completely blocked
                if (spawnLoc.getY() > bossLoc.getY() + 10) {
                    spawnLoc = bossLoc.clone();
                    break;
                }
            }
            
            // Spawn villager
            Villager villager = bossLoc.getWorld().spawn(spawnLoc, Villager.class);
            villager.setCustomName("§7False Villager");
            villager.setCustomNameVisible(true);
            villager.setProfession(Villager.Profession.NITWIT);
            villager.setAI(true);
            villager.setRemoveWhenFarAway(false);
            
            // Tag with boss UUID
            villager.getPersistentDataContainer().set(key, PersistentDataType.STRING, boss.getUUID().toString());
            
            // Track the minion
            falseVillagerToBoss.put(villager.getUniqueId(), boss.getUUID());
        }
    }
    
    /**
     * Gets the count of alive false villagers for a boss.
     * 
     * @param boss The boss
     * @return The count of alive minions
     */
    public int getFalseVillagerCount(FalseProphetBoss boss) {
        return (int) falseVillagerToBoss.values().stream()
            .filter(bossUUID -> bossUUID.equals(boss.getUUID()))
            .count();
    }
    
    /**
     * Handles the death of a false villager.
     * 
     * @param villagerUUID The UUID of the dead villager
     */
    public void onFalseVillagerDeath(UUID villagerUUID) {
        UUID bossUUID = falseVillagerToBoss.remove(villagerUUID);
        
        if (bossUUID != null) {
            FalseProphetBoss boss = activeBosses.get(bossUUID);
            if (boss != null && !boss.isDead()) {
                int remainingMinions = getFalseVillagerCount(boss);
                
                if (remainingMinions == 0 && boss.getState() != BossState.VULNERABLE) {
                    // All minions dead - will transition to vulnerable in next state check
                }
            }
        }
    }
    
    /**
     * Checks if a player can damage the boss.
     * 
     * @param boss The boss
     * @param attacker The attacking player
     * @return true if damage is allowed
     */
    public boolean canDamageBoss(FalseProphetBoss boss, Player attacker) {
        if (boss.isInVulnerableState()) {
            return true;
        }
        
        // Boss is immune - apply weakness to attacker
        attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        attacker.sendMessage("§cThe False Prophet is protected by his followers!");
        
        return false;
    }
    
    /**
     * Gets a boss by its entity UUID.
     * 
     * @param uuid The entity UUID
     * @return The boss, or null if not found
     */
    public FalseProphetBoss getBoss(UUID uuid) {
        return activeBosses.get(uuid);
    }
    
    /**
     * Checks if an entity is a boss.
     * 
     * @param entity The entity to check
     * @return true if the entity is a boss
     */
    public boolean isBoss(Entity entity) {
        if (!(entity instanceof Villager)) {
            return false;
        }
        
        NamespacedKey key = new NamespacedKey(plugin, "false_prophet_boss");
        return entity.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
    }
    
    /**
     * Checks if an entity is a false villager.
     * 
     * @param entity The entity to check
     * @return true if the entity is a false villager
     */
    public boolean isFalseVillager(Entity entity) {
        if (!(entity instanceof Villager)) {
            return false;
        }
        
        NamespacedKey key = new NamespacedKey(plugin, "false_villager");
        return entity.getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }
    
    /**
     * Removes a boss.
     * 
     * @param bossUUID The boss UUID
     */
    public void removeBoss(UUID bossUUID) {
        FalseProphetBoss boss = activeBosses.remove(bossUUID);
        if (boss != null) {
            boss.remove();
            
            // Remove all associated false villagers
            falseVillagerToBoss.entrySet().removeIf(entry -> {
                if (entry.getValue().equals(bossUUID)) {
                    // Find and remove the villager entity
                    Entity entity = plugin.getServer().getEntity(entry.getKey());
                    if (entity != null && !entity.isDead()) {
                        entity.remove();
                    }
                    return true;
                }
                return false;
            });
        }
    }
    
    /**
     * Broadcasts a message to players near a location.
     * 
     * @param location The center location
     * @param message The message to send
     */
    private void broadcastMessage(Location location, String message) {
        double range = 50.0;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= range) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Shuts down the boss manager and cleans up all resources.
     */
    public void shutdown() {
        if (stateCheckTask != null) {
            stateCheckTask.cancel();
        }
        
        // Remove all bosses
        for (UUID bossUUID : new ArrayList<>(activeBosses.keySet())) {
            removeBoss(bossUUID);
        }
        
        activeBosses.clear();
        falseVillagerToBoss.clear();
    }
    
    /**
     * Represents a validation result.
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;
        
        public ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

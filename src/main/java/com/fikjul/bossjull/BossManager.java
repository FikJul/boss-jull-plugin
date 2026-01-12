package com.fikjul.bossjull;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core logic controller for the Sentinel boss fight.
 * Manages state transitions, mechanics validation, and attack sequences.
 */
public class BossManager {
    
    private static final long SLAM_INTERVAL = 200L; // 10 seconds
    private static final long SLAM_CHARGE_TIME = 40L; // 2 seconds
    private static final double SLAM_DAMAGE_PHASE_1 = 10.0;
    private static final double SLAM_DAMAGE_PHASE_2 = 15.0;
    private static final double MOVEMENT_THRESHOLD = 0.5;
    
    private final BossJullPlugin plugin;
    private SentinelBoss activeBoss;
    private BukkitTask particleTask;
    private BukkitTask mechanicCheckTask;
    private BukkitTask slamTask;
    private boolean slamCharging = false;
    
    private final Map<UUID, Long> feedbackCooldowns = new HashMap<>();
    private static final long FEEDBACK_COOLDOWN_MS = 3000; // 3 seconds
    
    /**
     * Creates a new BossManager.
     * 
     * @param plugin The plugin instance
     */
    public BossManager(BossJullPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks if there is an active boss.
     * 
     * @return true if a boss is currently active
     */
    public boolean hasActiveBoss() {
        return activeBoss != null && !activeBoss.getEntity().isDead();
    }
    
    /**
     * Gets the active boss.
     * 
     * @return The active boss, or null if none
     */
    public SentinelBoss getActiveBoss() {
        return activeBoss;
    }
    
    /**
     * Summons a new Sentinel boss at the specified location.
     * 
     * @param location The location to summon the boss
     */
    public void summonBoss(Location location) {
        if (hasActiveBoss()) {
            return;
        }
        
        BossBar bossBar = Bukkit.createBossBar("", org.bukkit.boss.BarColor.WHITE, org.bukkit.boss.BarStyle.SEGMENTED_10);
        activeBoss = new SentinelBoss(location, bossBar);
        
        startTasks();
    }
    
    /**
     * Starts all recurring tasks for the boss fight.
     */
    private void startTasks() {
        // Particle task - every 10 ticks (0.5 seconds)
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasActiveBoss()) {
                    cancel();
                    return;
                }
                activeBoss.spawnRingParticles();
            }
        }.runTaskTimer(plugin, 0L, 10L);
        
        // Mechanic check task - every 5 ticks (0.25 seconds)
        mechanicCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasActiveBoss()) {
                    cancel();
                    return;
                }
                checkMechanics();
            }
        }.runTaskTimer(plugin, 0L, 5L);
        
        // Slam task - every 200 ticks (10 seconds)
        slamTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasActiveBoss()) {
                    cancel();
                    return;
                }
                BossState state = activeBoss.getCurrentState();
                if (state == BossState.PHASE_1_COMBAT || state == BossState.PHASE_2_COMBAT) {
                    executeSlamAttack();
                }
            }
        }.runTaskTimer(plugin, SLAM_INTERVAL, SLAM_INTERVAL);
    }
    
    /**
     * Checks and updates boss mechanics, state transitions, and boss bars.
     */
    private void checkMechanics() {
        List<Player> activePlayers = getActivePlayers();
        BossState currentState = activeBoss.getCurrentState();
        
        // Update boss bar visibility
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (activePlayers.contains(player)) {
                activeBoss.getBossBar().addPlayer(player);
            } else {
                activeBoss.getBossBar().removePlayer(player);
            }
        }
        
        // Update boss bar progress
        activeBoss.updateBossBar();
        
        // Check for death
        if (activeBoss.getEntity().getHealth() <= 0) {
            activeBoss.setCurrentState(BossState.DEAD);
            handleBossDeath();
            return;
        }
        
        // State transitions
        switch (currentState) {
            case IDLE:
                if (activePlayers.size() >= 2) {
                    activeBoss.setCurrentState(BossState.PHASE_1_COMBAT);
                }
                break;
                
            case PHASE_1_COMBAT:
                if (activePlayers.size() < 2) {
                    resetBoss();
                } else if (activeBoss.getEntity().getHealth() <= SentinelBoss.PHASE_2_THRESHOLD) {
                    transitionToPhase2();
                }
                break;
                
            case PHASE_2_TRANSITION:
                // Transition is handled by a delayed task
                break;
                
            case PHASE_2_COMBAT:
                if (activePlayers.size() < 2) {
                    resetBoss();
                }
                break;
                
            case DEAD:
                // Boss is dead, do nothing
                break;
        }
    }
    
    /**
     * Gets the list of active players in the arena.
     * 
     * @return List of active players
     */
    private List<Player> getActivePlayers() {
        if (!hasActiveBoss()) {
            return new ArrayList<>();
        }
        
        return Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.getWorld().equals(activeBoss.getEntity().getWorld()))
            .filter(player -> activeBoss.isInArena(player))
            .filter(player -> !player.isDead())
            .filter(player -> player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
            .collect(Collectors.toList());
    }
    
    /**
     * Validates if a player can damage the boss.
     * 
     * @param attacker The attacking player
     * @return true if damage is allowed
     */
    public boolean canDamageBoss(Player attacker) {
        if (!hasActiveBoss()) {
            return false;
        }
        
        BossState state = activeBoss.getCurrentState();
        if (state != BossState.PHASE_1_COMBAT && state != BossState.PHASE_2_COMBAT) {
            return false;
        }
        
        List<Player> activePlayers = getActivePlayers();
        
        // Require exactly 2 players
        if (activePlayers.size() != 2) {
            sendFeedback(attacker, "§c✗ Requires exactly 2 players in the arena!");
            return false;
        }
        
        // Count players in each ring
        long innerCount = activePlayers.stream().filter(activeBoss::isInInnerRing).count();
        long outerCount = activePlayers.stream().filter(activeBoss::isInOuterRing).count();
        
        // Must have one in each ring
        if (innerCount != 1 || outerCount != 1) {
            sendFeedback(attacker, "§c✗ Need 1 player in INNER ring and 1 in OUTER ring!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Sends feedback to a player with rate limiting.
     * 
     * @param player The player to send feedback to
     * @param message The message to send
     */
    private void sendFeedback(Player player, String message) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        Long lastFeedback = feedbackCooldowns.get(uuid);
        if (lastFeedback != null && (now - lastFeedback) < FEEDBACK_COOLDOWN_MS) {
            return;
        }
        
        player.sendMessage(message);
        feedbackCooldowns.put(uuid, now);
    }
    
    /**
     * Transitions the boss from Phase 1 to Phase 2.
     */
    private void transitionToPhase2() {
        activeBoss.setCurrentState(BossState.PHASE_2_TRANSITION);
        
        // Broadcast transition message
        for (Player player : getActivePlayers()) {
            player.sendMessage("§e§lPHASE 2: THE RINGS HAVE SWAPPED! SWITCH POSITIONS!");
        }
        
        // Transition to Phase 2 combat after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasActiveBoss() && activeBoss.getCurrentState() == BossState.PHASE_2_TRANSITION) {
                    activeBoss.setCurrentState(BossState.PHASE_2_COMBAT);
                }
            }
        }.runTaskLater(plugin, 40L);
    }
    
    /**
     * Resets the boss to idle state when player count drops.
     */
    private void resetBoss() {
        activeBoss.setCurrentState(BossState.IDLE);
        activeBoss.getEntity().setHealth(SentinelBoss.MAX_HEALTH);
        
        // Broadcast reset message
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (activeBoss.getBossBar().getPlayers().contains(player)) {
                player.sendMessage("§7The Sentinel returns to dormancy...");
            }
        }
    }
    
    /**
     * Handles boss death.
     */
    private void handleBossDeath() {
        List<Player> activePlayers = getActivePlayers();
        
        for (Player player : activePlayers) {
            player.sendMessage("§a§l✓ The Sentinel has been defeated!");
        }
        
        // Remove boss after a short delay
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBoss != null) {
                    activeBoss.remove();
                    activeBoss = null;
                    stopTasks();
                }
            }
        }.runTaskLater(plugin, 100L);
    }
    
    /**
     * Executes the slam attack sequence.
     */
    private void executeSlamAttack() {
        if (slamCharging) {
            return;
        }
        
        slamCharging = true;
        
        // Charging phase
        activeBoss.playSlamChargeEffects();
        
        for (Player player : getActivePlayers()) {
            player.sendMessage("§c§lSENTINEL CHARGES A SLAM! STAND STILL!");
            activeBoss.recordPlayerPosition(player);
        }
        
        // Execute phase after charge time
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasActiveBoss()) {
                    slamCharging = false;
                    return;
                }
                
                activeBoss.playSlamExecuteEffects();
                
                double damage = activeBoss.getCurrentState() == BossState.PHASE_2_COMBAT 
                    ? SLAM_DAMAGE_PHASE_2 
                    : SLAM_DAMAGE_PHASE_1;
                
                for (Player player : getActivePlayers()) {
                    Location recordedPos = activeBoss.getRecordedPosition(player);
                    if (recordedPos != null) {
                        double distanceMoved = player.getLocation().distance(recordedPos);
                        
                        if (distanceMoved > MOVEMENT_THRESHOLD) {
                            player.damage(damage);
                            player.sendMessage("§c✗ You moved during the slam! -" + damage + " HP");
                        } else {
                            player.sendMessage("§a✓ You stood still and avoided the slam!");
                        }
                    }
                }
                
                activeBoss.clearRecordedPositions();
                slamCharging = false;
            }
        }.runTaskLater(plugin, SLAM_CHARGE_TIME);
    }
    
    /**
     * Stops all running tasks.
     */
    private void stopTasks() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
        if (mechanicCheckTask != null) {
            mechanicCheckTask.cancel();
            mechanicCheckTask = null;
        }
        if (slamTask != null) {
            slamTask.cancel();
            slamTask = null;
        }
    }
    
    /**
     * Shuts down the boss manager and cleans up resources.
     */
    public void shutdown() {
        stopTasks();
        if (activeBoss != null) {
            activeBoss.remove();
            activeBoss = null;
        }
        feedbackCooldowns.clear();
    }
}

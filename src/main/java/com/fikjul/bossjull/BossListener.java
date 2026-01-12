package com.fikjul.bossjull;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Event handler for Sentinel boss interactions.
 */
public class BossListener implements Listener {
    
    private final BossJullPlugin plugin;
    private final BossManager bossManager;
    
    /**
     * Creates a new BossListener.
     * 
     * @param plugin The plugin instance
     */
    public BossListener(BossJullPlugin plugin) {
        this.plugin = plugin;
        this.bossManager = plugin.getBossManager();
    }
    
    /**
     * Handles player interaction with the summon item.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if right-clicking
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Check if item is summon item
        if (!SummonItem.isSummonItem(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        // Validate game mode
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            player.sendMessage("§c✗ Summon item can only be used in Survival or Adventure mode!");
            return;
        }
        
        // Check if boss already active
        if (bossManager.hasActiveBoss()) {
            player.sendMessage("§c✗ The Sentinel is already active!");
            return;
        }
        
        // Summon the boss
        bossManager.summonBoss(player.getLocation());
        
        // Consume the item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Play sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.2f);
        
        // Send messages
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§c§l   THE SENTINEL OF THE RING AWAKENS!");
        player.sendMessage("");
        player.sendMessage("§e§l⚠ REQUIRES 2 PLAYERS TO DEFEAT");
        player.sendMessage("");
        player.sendMessage("§6§lMECHANICS:");
        player.sendMessage("§7• §dINNER RING §7(4 blocks) §7- One player must stay here");
        player.sendMessage("§7• §cOUTER RING §7(8 blocks) §7- One player must stay here");
        player.sendMessage("§7• §4SLAM ATTACKS §7- Stand still when warned!");
        player.sendMessage("§7• At §e50% HP§7, rings §lSWAP COLORS §7- switch positions!");
        player.sendMessage("");
        player.sendMessage("§c§lGood luck...");
        player.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * Handles player damage to the boss entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if damaged entity is the boss
        if (!bossManager.hasActiveBoss()) {
            return;
        }
        
        Entity damagedEntity = event.getEntity();
        if (!damagedEntity.equals(bossManager.getActiveBoss().getEntity())) {
            return;
        }
        
        // Extract attacking player
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        }
        
        if (attacker == null) {
            event.setCancelled(true);
            return;
        }
        
        // Validate damage conditions
        if (!bossManager.canDamageBoss(attacker)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Prevents boss from taking non-player damage.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!bossManager.hasActiveBoss()) {
            return;
        }
        
        Entity damagedEntity = event.getEntity();
        if (!damagedEntity.equals(bossManager.getActiveBoss().getEntity())) {
            return;
        }
        
        // Only allow damage from EntityDamageByEntityEvent (which is processed separately)
        if (!(event instanceof EntityDamageByEntityEvent)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles boss death.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!bossManager.hasActiveBoss()) {
            return;
        }
        
        Entity entity = event.getEntity();
        if (!entity.equals(bossManager.getActiveBoss().getEntity())) {
            return;
        }
        
        // Clear drops and XP
        event.getDrops().clear();
        event.setDroppedExp(0);
        
        // Custom rewards can be added here
    }
    
    /**
     * Removes players from boss bar when they quit.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!bossManager.hasActiveBoss()) {
            return;
        }
        
        bossManager.getActiveBoss().getBossBar().removePlayer(event.getPlayer());
    }
}

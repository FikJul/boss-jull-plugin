package com.fikjul.bossjull.falseprophet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for events related to the False Prophet boss fight.
 */
public class BossListener implements Listener {
    
    private final FalseProphetPlugin plugin;
    private final BossManager bossManager;
    
    /**
     * Creates a new BossListener.
     * 
     * @param plugin The plugin instance
     */
    public BossListener(FalseProphetPlugin plugin) {
        this.plugin = plugin;
        this.bossManager = plugin.getBossManager();
    }
    
    /**
     * Handles player interaction to summon the boss.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && 
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Only handle main hand to avoid double triggers
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if item is summon totem
        if (!SummonItem.isSummonItem(item)) {
            return;
        }
        
        event.setCancelled(true);
        
        Location playerLoc = player.getLocation();
        
        // Validate summon conditions
        BossManager.ValidationResult result = bossManager.validateSummonConditions(player, playerLoc);
        
        if (!result.isSuccess()) {
            player.sendMessage(result.getMessage());
            return;
        }
        
        // Consume the item
        item.setAmount(item.getAmount() - 1);
        
        // Summon the boss
        FalseProphetBoss boss = bossManager.summonBoss(playerLoc);
        
        // Send success message
        player.sendMessage("§6You have summoned The False Prophet!");
    }
    
    /**
     * Handles damage to the boss or false villagers.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        
        // Check if boss is being damaged
        if (bossManager.isBoss(damaged)) {
            // Get attacker
            Player attacker = null;
            if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            }
            
            if (attacker == null) {
                event.setCancelled(true);
                return;
            }
            
            FalseProphetBoss boss = bossManager.getBoss(damaged.getUniqueId());
            if (boss == null || boss.isDead()) {
                return;
            }
            
            // Check if boss can be damaged
            if (!bossManager.canDamageBoss(boss, attacker)) {
                event.setCancelled(true);
                return;
            }
            
            // Allow damage and update boss bar
            boss.updateBossBar();
        }
    }
    
    /**
     * Handles entity death events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        // Check if false villager died
        if (bossManager.isFalseVillager(entity)) {
            // Clear drops and XP
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Notify boss manager
            bossManager.onFalseVillagerDeath(entity.getUniqueId());
        }
        
        // Check if boss died
        if (bossManager.isBoss(entity)) {
            FalseProphetBoss boss = bossManager.getBoss(entity.getUniqueId());
            
            if (boss != null) {
                // Clear drops and XP
                event.getDrops().clear();
                event.setDroppedExp(0);
                
                // Play victory effects
                Location loc = entity.getLocation();
                loc.getWorld().strikeLightningEffect(loc);
                
                // Broadcast victory message
                for (Player player : loc.getWorld().getPlayers()) {
                    if (player.getLocation().distance(loc) <= 50.0) {
                        player.sendMessage("§6The False Prophet has been defeated!");
                    }
                }
                
                // Remove the boss
                bossManager.removeBoss(boss.getUUID());
            }
        }
    }
    
    /**
     * Prevents boss and false villagers from taking non-player damage.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        
        // Prevent environmental damage to boss
        if (bossManager.isBoss(entity)) {
            if (!(event instanceof EntityDamageByEntityEvent)) {
                event.setCancelled(true);
            }
        }
        
        // Prevent environmental damage to false villagers
        if (bossManager.isFalseVillager(entity)) {
            if (!(event instanceof EntityDamageByEntityEvent)) {
                event.setCancelled(true);
            }
        }
    }
}

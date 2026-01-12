package com.fikjul.falseprophet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Event listener for The False Prophet boss fight.
 * Handles summon item usage, boss damage, and minion deaths.
 */
public class BossListener implements Listener {
    
    private final FalseProphetPlugin plugin;
    private final BossManager bossManager;
    private final SummonItemManager summonItemManager;
    
    public BossListener(FalseProphetPlugin plugin, BossManager bossManager, SummonItemManager summonItemManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.summonItemManager = summonItemManager;
    }
    
    /**
     * Handles player interactions for summon item usage.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if right-clicking
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if holding summon item
        if (!summonItemManager.isSummonItem(item)) {
            return;
        }
        
        // Cancel the event
        event.setCancelled(true);
        
        // Validate summon conditions
        SummonItemManager.SummonValidation validation = summonItemManager.validateSummonConditions(player);
        
        if (!validation.isValid()) {
            // Send failure message
            player.sendMessage(validation.getMessage());
            return;
        }
        
        // Conditions met - summon the boss
        player.sendMessage(validation.getMessage());
        
        // Consume item
        item.setAmount(item.getAmount() - 1);
        
        // Spawn boss at player location
        bossManager.spawnBoss(player.getLocation());
    }
    
    /**
     * Handles damage to the boss entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        
        // Check if the damaged entity is the boss
        if (!bossManager.isBoss(damaged)) {
            return;
        }
        
        // Check if damage should be blocked
        if (bossManager.shouldBlockDamage()) {
            event.setCancelled(true);
            
            // Apply weakness to attacker if it's a player
            if (event.getDamager() instanceof Player) {
                Player attacker = (Player) event.getDamager();
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1, false, false));
                attacker.sendMessage("§c§lThe False Prophet is protected by his followers!");
                attacker.playSound(attacker.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
            return;
        }
        
        // Damage is allowed - update boss manager
        bossManager.onBossDamage(event.getFinalDamage());
        
        // Play hurt sound
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 0.8f);
        }
    }
    
    /**
     * Handles entity deaths for boss and minions.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        
        // Check if it's a false villager minion
        if (bossManager.isMinion(entity)) {
            // Clear drops and XP
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Notify boss manager
            bossManager.onMinionDeath(entity.getUniqueId());
            
            // Play death sound
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_DEATH, 1.0f, 1.0f);
            return;
        }
        
        // Check if it's the boss
        if (bossManager.isBoss(entity)) {
            // Death is handled by BossManager, just clear default drops
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
}

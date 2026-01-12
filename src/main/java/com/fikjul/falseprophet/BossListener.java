package com.fikjul.falseprophet;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

/**
 * Handles all events related to The False Prophet boss.
 */
public class BossListener implements Listener {
    
    private final FalseProphetPlugin plugin;
    private final BossManager bossManager;
    
    // Valid biomes for summoning
    private static final List<Biome> VALID_BIOMES = Arrays.asList(
        Biome.PLAINS,
        Biome.SUNFLOWER_PLAINS,
        Biome.DESERT,
        Biome.SAVANNA,
        Biome.SAVANNA_PLATEAU,
        Biome.WINDSWEPT_SAVANNA,
        Biome.TAIGA,
        Biome.SNOWY_PLAINS,
        Biome.ICE_SPIKES,
        Biome.SNOWY_TAIGA
    );
    
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
     * Handles player interaction with the summon item.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if item is the summon totem
        if (!SummonItem.isSummonItem(item)) {
            return;
        }
        
        // Validate summon conditions
        Location playerLoc = player.getLocation();
        
        // Check block beneath feet
        Block blockBelow = playerLoc.subtract(0, 1, 0).getBlock();
        if (blockBelow.getType() != Material.EMERALD_BLOCK) {
            player.sendMessage("§cYou must stand on an Emerald Block!");
            event.setCancelled(true);
            return;
        }
        
        // Check biome
        Biome biome = player.getLocation().getBlock().getBiome();
        if (!VALID_BIOMES.contains(biome)) {
            player.sendMessage("§cYou must be in a Village to summon The False Prophet!");
            event.setCancelled(true);
            return;
        }
        
        // Check if boss already exists
        if (bossManager.hasBoss()) {
            player.sendMessage("§cThe False Prophet has already been summoned!");
            event.setCancelled(true);
            return;
        }
        
        // Summon successful!
        event.setCancelled(true);
        
        // Consume item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        
        // Get spawn location (restore Y coordinate)
        Location spawnLoc = player.getLocation().add(0, 1, 0);
        
        // Visual effects
        World world = spawnLoc.getWorld();
        
        // Lightning effect (no damage)
        world.strikeLightningEffect(spawnLoc);
        
        // Sounds
        world.playSound(spawnLoc, Sound.BLOCK_BELL_USE, 2.0f, 0.8f);
        world.playSound(spawnLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        
        // Particles
        world.spawnParticle(Particle.VILLAGER_HAPPY, spawnLoc, 50, 1.0, 1.0, 1.0);
        world.spawnParticle(Particle.GLOW, spawnLoc, 30, 1.0, 1.0, 1.0, 
            new Particle.DustOptions(Color.fromRGB(0, 255, 0), 2.0f));
        
        // Spawn boss
        bossManager.spawnBoss(spawnLoc);
        
        // Broadcast message
        Bukkit.broadcastMessage("§6§l========================================");
        Bukkit.broadcastMessage("§c§l  The False Prophet has been summoned!");
        Bukkit.broadcastMessage("§6§l========================================");
    }
    
    /**
     * Handles damage to the boss entity.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if damaged entity is the boss
        if (!(event.getEntity() instanceof Villager)) {
            return;
        }
        
        Villager villager = (Villager) event.getEntity();
        
        // Check if this is the boss
        if (!villager.getPersistentDataContainer().has(bossManager.getBossKey(), PersistentDataType.BYTE)) {
            return;
        }
        
        // Extract attacking player
        Player attacker = null;
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }
        
        if (attacker == null) {
            return;
        }
        
        // Handle boss damage
        if (!bossManager.handleBossDamage(attacker)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handles entity death events.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager)) {
            return;
        }
        
        Villager villager = (Villager) event.getEntity();
        
        // Check if this is the boss
        if (villager.getPersistentDataContainer().has(bossManager.getBossKey(), PersistentDataType.BYTE)) {
            // Boss died
            event.getDrops().clear();
            event.setDroppedExp(0);
            bossManager.handleBossDeath();
            return;
        }
        
        // Check if this is a false villager
        if (villager.getPersistentDataContainer().has(bossManager.getFalseVillagerKey(), PersistentDataType.STRING)) {
            // False villager died
            event.getDrops().clear();
            event.setDroppedExp(0);
            bossManager.handleFalseVillagerDeath(villager.getUniqueId());
        }
    }
}

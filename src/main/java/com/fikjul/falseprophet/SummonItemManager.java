package com.fikjul.falseprophet;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

/**
 * Manages the creation and validation of the summon item for The False Prophet.
 */
public class SummonItemManager {
    
    private final FalseProphetPlugin plugin;
    private final NamespacedKey summonItemKey;
    
    // Village biomes where the boss can be summoned
    private static final List<Biome> VILLAGE_BIOMES = Arrays.asList(
        Biome.PLAINS,
        Biome.DESERT,
        Biome.SAVANNA,
        Biome.TAIGA,
        Biome.SNOWY_PLAINS,
        Biome.SUNFLOWER_PLAINS
    );
    
    public SummonItemManager(FalseProphetPlugin plugin) {
        this.plugin = plugin;
        this.summonItemKey = new NamespacedKey(plugin, "summon_item");
    }
    
    /**
     * Creates a new summon item for The False Prophet.
     * @return The summon item
     */
    public ItemStack createSummonItem() {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§6Totem of False Faith");
        
        // Set lore
        meta.setLore(Arrays.asList(
            "§7A sacred relic worshipped by the masses",
            "§7Only the chosen may reveal the truth"
        ));
        
        // Add hidden enchantment for glow effect
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        
        // Hide enchantments and attributes
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        
        // Mark as summon item using PersistentDataContainer
        meta.getPersistentDataContainer().set(summonItemKey, PersistentDataType.STRING, "true");
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Checks if an item is a valid summon item.
     * @param item The item to check
     * @return True if the item is a summon item
     */
    public boolean isSummonItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(summonItemKey, PersistentDataType.STRING);
    }
    
    /**
     * Validates summon conditions for a player.
     * @param player The player attempting to summon
     * @return Validation result with success status and message
     */
    public SummonValidation validateSummonConditions(Player player) {
        // Check if standing on emerald block
        Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (blockBelow != Material.EMERALD_BLOCK) {
            return new SummonValidation(false, "§c§lYou must stand on an Emerald Block!");
        }
        
        // Check if in village biome
        Biome biome = player.getLocation().getBlock().getBiome();
        if (!VILLAGE_BIOMES.contains(biome)) {
            return new SummonValidation(false, "§c§lYou must be in a Village to summon the False Prophet!");
        }
        
        return new SummonValidation(true, "§a§lThe False Prophet has been summoned!");
    }
    
    /**
     * Result of summon validation.
     */
    public static class SummonValidation {
        private final boolean valid;
        private final String message;
        
        public SummonValidation(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

package com.fikjul.falseprophet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Manages the creation and validation of the False Faith totem summon item.
 */
public class SummonItem {
    
    private static final String DISPLAY_NAME = "§6Totem of False Faith";
    private static final String LORE_LINE_1 = "§7A sacred relic worshipped by the masses";
    private static final String LORE_LINE_2 = "§7Only the chosen may reveal the truth";
    
    /**
     * Creates the False Faith totem item.
     * 
     * @return ItemStack representing the summon totem
     */
    public static ItemStack createSummonItem() {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(DISPLAY_NAME);
            
            // Set lore
            meta.setLore(Arrays.asList(LORE_LINE_1, LORE_LINE_2));
            
            // Add enchantment for glow effect
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            
            // Hide enchantment and attributes
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Checks if an ItemStack is the False Faith totem.
     * 
     * @param item The ItemStack to check
     * @return true if the item is the summon totem, false otherwise
     */
    public static boolean isSummonItem(ItemStack item) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        // Check display name
        if (!DISPLAY_NAME.equals(meta.getDisplayName())) {
            return false;
        }
        
        // Check lore
        if (meta.getLore() == null || meta.getLore().size() < 2) {
            return false;
        }
        
        return meta.getLore().contains(LORE_LINE_1) && meta.getLore().contains(LORE_LINE_2);
    }
}

package com.fikjul.bossjull.falseprophet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for creating and validating the Totem of False Faith summon item.
 */
public class SummonItem {
    
    private static final String DISPLAY_NAME = "§6Totem of False Faith";
    private static final List<String> LORE = Arrays.asList(
        "§7A sacred relic worshipped by the masses",
        "§7Only the chosen may reveal the truth"
    );
    
    /**
     * Creates a new Totem of False Faith summon item.
     * 
     * @return The created summon item
     */
    public static ItemStack createSummonItem() {
        ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(DISPLAY_NAME);
            meta.setLore(LORE);
            
            // Add enchant glow effect (hidden)
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Validates if an ItemStack is the summon totem.
     * 
     * @param item The ItemStack to check
     * @return true if the item is a valid summon totem
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
        List<String> itemLore = meta.getLore();
        if (itemLore == null || !itemLore.equals(LORE)) {
            return false;
        }
        
        return true;
    }
}

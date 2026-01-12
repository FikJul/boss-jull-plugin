package com.fikjul.bossjull;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Manages the creation and validation of Sentinel summon items.
 */
public class SummonItem {
    
    private static final String DISPLAY_NAME = "§c§lSentinel Summoning Star";
    private static final String LORE_IDENTIFIER = "§7§o[Sentinel Summon]";
    
    private static final List<String> ITEM_LORE = Arrays.asList(
        LORE_IDENTIFIER,
        "",
        "§7Right-click to summon the",
        "§c§lSentinel of the Ring",
        "",
        "§e⚠ Requires 2 players to defeat",
        "§e⚠ Single-use item",
        "",
        "§6§lMechanics:",
        "§7• One player in §dINNER RING §7(4 blocks)",
        "§7• One player in §cOUTER RING §7(8 blocks)",
        "§7• Stand still during §4SLAM ATTACKS",
        "",
        "§c§lGood luck..."
    );
    
    /**
     * Creates a new summon item with the configured display name, lore, and enchantments.
     * 
     * @return A new summon item ItemStack
     */
    public static ItemStack createSummonItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(DISPLAY_NAME);
            meta.setLore(ITEM_LORE);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Checks if the given ItemStack is a valid Sentinel summon item.
     * 
     * @param item The ItemStack to check
     * @return true if the item is a summon item, false otherwise
     */
    public static boolean isSummonItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }
        
        if (!DISPLAY_NAME.equals(meta.getDisplayName())) {
            return false;
        }
        
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }
        
        return LORE_IDENTIFIER.equals(lore.get(0));
    }
}

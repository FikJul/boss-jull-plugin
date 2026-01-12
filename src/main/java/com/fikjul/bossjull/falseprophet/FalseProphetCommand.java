package com.fikjul.bossjull.falseprophet;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the /givefalsefaith command.
 */
public class FalseProphetCommand implements CommandExecutor {
    
    private final FalseProphetPlugin plugin;
    
    /**
     * Creates a new command handler.
     * 
     * @param plugin The plugin instance
     */
    public FalseProphetCommand(FalseProphetPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("givefalsefaith")) {
            return false;
        }
        
        // Check permission
        if (!sender.hasPermission("bossjull.falseprophet")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        Player target;
        int amount = 1;
        
        // Parse arguments
        if (args.length == 0) {
            // Give to self
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player when using this command from console.");
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 1) {
            // Give to specified player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + args[0] + "' not found.");
                return true;
            }
        } else if (args.length == 2) {
            // Give to specified player with amount
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer '" + args[0] + "' not found.");
                return true;
            }
            
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage("§cAmount must be between 1 and 64.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount. Please specify a number between 1 and 64.");
                return true;
            }
        } else {
            // Show help
            sender.sendMessage("§6False Prophet Boss - Help");
            sender.sendMessage("§7Usage: /givefalsefaith [player] [amount]");
            sender.sendMessage("");
            sender.sendMessage("§e✦ Summon Requirements:");
            sender.sendMessage("§7  • Stand on an Emerald Block");
            sender.sendMessage("§7  • Be in a Village biome (Plains, Desert, Savanna, Taiga, Snowy Plains)");
            sender.sendMessage("§7  • Right-click the Totem of False Faith");
            sender.sendMessage("");
            sender.sendMessage("§e✦ Boss Mechanics:");
            sender.sendMessage("§7  • The boss is IMMUNE when minions are alive");
            sender.sendMessage("§7  • Kill all False Villagers to make the boss VULNERABLE");
            sender.sendMessage("§7  • You have 6 seconds to damage the boss (4 seconds when enraged)");
            sender.sendMessage("§7  • Boss enters ENRAGED phase at 30% HP");
            sender.sendMessage("§7  • Enraged boss spawns more minions (5 instead of 3)");
            return true;
        }
        
        // Give the item(s)
        for (int i = 0; i < amount; i++) {
            ItemStack item = SummonItem.createSummonItem();
            target.getInventory().addItem(item);
        }
        
        // Play sound
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Send messages
        if (amount == 1) {
            target.sendMessage("§aYou have received the Totem of False Faith!");
        } else {
            target.sendMessage("§aYou have received " + amount + " Totems of False Faith!");
        }
        
        if (!sender.equals(target)) {
            sender.sendMessage("§aGave " + amount + " Totem(s) of False Faith to " + target.getName());
        }
        
        return true;
    }
}

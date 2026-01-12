package com.fikjul.falseprophet;

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
     * Creates a new FalseProphetCommand.
     * 
     * @param plugin The plugin instance
     */
    public FalseProphetCommand(FalseProphetPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Determine target player
        Player target;
        int amount = 1;
        
        if (args.length == 0) {
            // Give to self
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player when using this command from console.");
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 1) {
            // Give to specified player or parse amount
            Player possiblePlayer = Bukkit.getPlayer(args[0]);
            if (possiblePlayer != null) {
                target = possiblePlayer;
            } else {
                // Try to parse as amount
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cPlayer not found: " + args[0]);
                    return true;
                }
                target = (Player) sender;
                try {
                    amount = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid player or amount: " + args[0]);
                    return true;
                }
            }
        } else {
            // Give to specified player with specified amount
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return true;
            }
            
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount: " + args[1]);
                return true;
            }
        }
        
        // Validate amount
        if (amount < 1 || amount > 64) {
            sender.sendMessage("§cAmount must be between 1 and 64.");
            return true;
        }
        
        // Create and give items
        for (int i = 0; i < amount; i++) {
            ItemStack item = SummonItem.createSummonItem();
            target.getInventory().addItem(item);
        }
        
        // Play sound
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Send confirmation messages
        target.sendMessage("§aYou have received §6" + amount + "x Totem of False Faith§a!");
        
        if (!sender.equals(target)) {
            sender.sendMessage("§aGave §6" + amount + "x Totem of False Faith §ato " + target.getName() + "!");
        }
        
        return true;
    }
}

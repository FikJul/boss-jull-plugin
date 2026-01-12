package com.fikjul.falseprophet;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for The False Prophet boss fight.
 * Handles /falseprophet give, summon, and help commands.
 */
public class FalseProphetCommand implements CommandExecutor, TabCompleter {
    
    private final FalseProphetPlugin plugin;
    private final BossManager bossManager;
    private final SummonItemManager summonItemManager;
    
    public FalseProphetCommand(FalseProphetPlugin plugin, BossManager bossManager, SummonItemManager summonItemManager) {
        this.plugin = plugin;
        this.bossManager = bossManager;
        this.summonItemManager = summonItemManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check for help or no arguments
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGive(sender, args);
            case "summon":
                return handleSummon(sender, args);
            default:
                sender.sendMessage("§c§lUnknown command. Use /falseprophet help for usage.");
                return true;
        }
    }
    
    /**
     * Handles the give subcommand.
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("falseprophet.give")) {
            sender.sendMessage("§c§lYou don't have permission to use this command.");
            return true;
        }
        
        Player target;
        int amount = 1;
        
        // Parse arguments: /falseprophet give [player] [amount]
        if (args.length >= 2) {
            // Target player specified
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c§lPlayer not found: " + args[1]);
                return true;
            }
            
            // Parse amount if specified
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount < 1 || amount > 64) {
                        sender.sendMessage("§c§lAmount must be between 1 and 64.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c§lInvalid amount: " + args[2]);
                    return true;
                }
            }
        } else {
            // No player specified - give to sender if they're a player
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c§lYou must specify a player when running from console.");
                return true;
            }
            target = (Player) sender;
        }
        
        // Give the summon item
        ItemStack summonItem = summonItemManager.createSummonItem();
        summonItem.setAmount(amount);
        target.getInventory().addItem(summonItem);
        
        sender.sendMessage("§a§lGave " + amount + " Totem of False Faith to " + target.getName());
        if (!target.equals(sender)) {
            target.sendMessage("§a§lYou received " + amount + " Totem of False Faith!");
        }
        
        return true;
    }
    
    /**
     * Handles the summon subcommand.
     */
    private boolean handleSummon(CommandSender sender, String[] args) {
        // Check permission
        if (!sender.hasPermission("falseprophet.summon")) {
            sender.sendMessage("§c§lYou don't have permission to use this command.");
            return true;
        }
        
        // Must be a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c§lThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Summon boss at player location (bypass conditions)
        bossManager.spawnBoss(player.getLocation());
        player.sendMessage("§a§lThe False Prophet has been summoned!");
        
        return true;
    }
    
    /**
     * Sends help information to the sender.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== The False Prophet Commands ===");
        sender.sendMessage("§e/falseprophet give [player] [amount] §7- Give summon item");
        sender.sendMessage("§e/falseprophet summon §7- Summon boss (bypass conditions)");
        sender.sendMessage("§e/falseprophet help §7- Show this help");
        sender.sendMessage("");
        sender.sendMessage("§7To summon the boss legitimately:");
        sender.sendMessage("§7  1. Stand on an Emerald Block");
        sender.sendMessage("§7  2. Be in a Village biome");
        sender.sendMessage("§7  3. Right-click with the Totem of False Faith");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - subcommands
            completions.addAll(Arrays.asList("give", "summon", "help"));
            return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Second argument for give - player names
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Third argument for give - amount suggestions
            completions.addAll(Arrays.asList("1", "5", "10", "16", "32", "64"));
            return completions.stream()
                .filter(s -> s.startsWith(args[2]))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}

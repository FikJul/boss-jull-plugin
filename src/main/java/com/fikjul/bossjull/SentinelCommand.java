package com.fikjul.bossjull;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Command executor for the /sentinel command.
 */
public class SentinelCommand implements CommandExecutor {
    
    private final BossJullPlugin plugin;
    private final BossManager bossManager;
    
    /**
     * Creates a new SentinelCommand.
     * 
     * @param plugin The plugin instance
     */
    public SentinelCommand(BossJullPlugin plugin) {
        this.plugin = plugin;
        this.bossManager = plugin.getBossManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMenu(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGiveCommand(sender, args);
            case "summon":
                return handleSummonCommand(sender);
            case "help":
                sendHelpMenu(sender);
                return true;
            default:
                sender.sendMessage("§c✗ Unknown subcommand. Use /sentinel help for help.");
                return true;
        }
    }
    
    /**
     * Handles the give subcommand.
     */
    private boolean handleGiveCommand(CommandSender sender, String[] args) {
        // Determine target player
        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c✗ Player not found: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c✗ Console must specify a player: /sentinel give <player> [amount]");
                return true;
            }
            target = (Player) sender;
        }
        
        // Determine amount
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage("§c✗ Amount must be between 1 and 64.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§c✗ Invalid amount: " + args[2]);
                return true;
            }
        }
        
        // Create and give items
        for (int i = 0; i < amount; i++) {
            ItemStack summonItem = SummonItem.createSummonItem();
            target.getInventory().addItem(summonItem);
        }
        
        // Play sound
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Send messages
        if (sender.equals(target)) {
            sender.sendMessage("§a✓ You received " + amount + " Sentinel Summoning Star(s)!");
        } else {
            sender.sendMessage("§a✓ Gave " + amount + " Sentinel Summoning Star(s) to " + target.getName());
            target.sendMessage("§a✓ You received " + amount + " Sentinel Summoning Star(s)!");
        }
        
        return true;
    }
    
    /**
     * Handles the summon subcommand (admin/legacy).
     */
    private boolean handleSummonCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c✗ Only players can summon the boss directly.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if boss already active
        if (bossManager.hasActiveBoss()) {
            sender.sendMessage("§c✗ The Sentinel is already active!");
            return true;
        }
        
        // Summon the boss
        bossManager.summonBoss(player.getLocation());
        sender.sendMessage("§a✓ Sentinel of the Ring summoned at your location!");
        
        return true;
    }
    
    /**
     * Sends the help menu to the sender.
     */
    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage("§c§l━━━━━━━━━━ SENTINEL COMMANDS ━━━━━━━━━━");
        sender.sendMessage("");
        sender.sendMessage("§e/sentinel give §7[player] [amount]");
        sender.sendMessage("  §7Give summon item(s) to a player");
        sender.sendMessage("  §7Default: yourself, amount 1");
        sender.sendMessage("  §7Example: §f/sentinel give Steve 5");
        sender.sendMessage("");
        sender.sendMessage("§e/sentinel summon");
        sender.sendMessage("  §7Directly summon the boss (admin)");
        sender.sendMessage("  §7Example: §f/sentinel summon");
        sender.sendMessage("");
        sender.sendMessage("§e/sentinel help");
        sender.sendMessage("  §7Show this help menu");
        sender.sendMessage("");
        sender.sendMessage("§6§lBOSS MECHANICS:");
        sender.sendMessage("§7• Requires §e2 players §7to damage");
        sender.sendMessage("§7• One in §dINNER RING §7(4 blocks)");
        sender.sendMessage("§7• One in §cOUTER RING §7(8 blocks)");
        sender.sendMessage("§7• Stand still during §4SLAM ATTACKS");
        sender.sendMessage("§7• Rings swap at §e50% HP");
        sender.sendMessage("");
        sender.sendMessage("§c§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}

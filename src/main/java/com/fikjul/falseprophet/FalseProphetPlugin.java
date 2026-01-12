package com.fikjul.falseprophet;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for VillagerFalseProphet.
 * 
 * This plugin implements a custom boss fight against "The False Prophet",
 * a corrupted villager with unique immunity mechanics based on minion management.
 */
public class FalseProphetPlugin extends JavaPlugin {
    
    private BossManager bossManager;
    
    @Override
    public void onEnable() {
        // Initialize boss manager
        bossManager = new BossManager(this);
        
        // Register listener
        getServer().getPluginManager().registerEvents(new BossListener(this), this);
        
        // Register command
        getCommand("givefalsefaith").setExecutor(new FalseProphetCommand(this));
        
        // Log startup
        getLogger().info("VillagerFalseProphet has been enabled!");
        getLogger().info("The False Prophet awaits...");
    }
    
    @Override
    public void onDisable() {
        // Clean up boss manager
        if (bossManager != null) {
            bossManager.shutdown();
        }
        
        // Log shutdown
        getLogger().info("VillagerFalseProphet has been disabled!");
    }
    
    /**
     * Gets the boss manager instance.
     * 
     * @return The BossManager
     */
    public BossManager getBossManager() {
        return bossManager;
    }
}

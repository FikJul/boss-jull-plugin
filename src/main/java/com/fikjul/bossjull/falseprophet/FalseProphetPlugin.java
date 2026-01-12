package com.fikjul.bossjull.falseprophet;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for the False Prophet boss plugin.
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
        
        // Log enable message
        getLogger().info("FalseProphetPlugin has been enabled!");
        getLogger().info("The False Prophet awaits those brave enough to summon him...");
    }
    
    @Override
    public void onDisable() {
        // Shutdown boss manager
        if (bossManager != null) {
            bossManager.shutdown();
        }
        
        // Log disable message
        getLogger().info("FalseProphetPlugin has been disabled.");
    }
    
    /**
     * Gets the boss manager.
     * 
     * @return The BossManager instance
     */
    public BossManager getBossManager() {
        return bossManager;
    }
}

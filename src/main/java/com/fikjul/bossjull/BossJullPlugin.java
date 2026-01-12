package com.fikjul.bossjull;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Sentinel of the Ring boss fight.
 * Handles initialization, registration, and cleanup.
 */
public class BossJullPlugin extends JavaPlugin {
    
    private BossManager bossManager;
    
    @Override
    public void onEnable() {
        // Initialize boss manager
        bossManager = new BossManager(this);
        
        // Register event listener
        getServer().getPluginManager().registerEvents(new BossListener(this), this);
        
        // Register command
        getCommand("sentinel").setExecutor(new SentinelCommand(this));
        
        getLogger().info("Sentinel of the Ring plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up boss manager
        if (bossManager != null) {
            bossManager.shutdown();
        }
        
        getLogger().info("Sentinel of the Ring plugin disabled!");
    }
    
    /**
     * Gets the boss manager instance.
     * 
     * @return The boss manager
     */
    public BossManager getBossManager() {
        return bossManager;
    }
}

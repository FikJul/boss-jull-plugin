package com.fikjul.falseprophet;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for The False Prophet boss fight.
 * Handles plugin initialization, shutdown, and registration of commands and listeners.
 */
public class FalseProphetPlugin extends JavaPlugin {
    
    private BossManager bossManager;
    private SummonItemManager summonItemManager;
    
    @Override
    public void onEnable() {
        // Initialize managers
        this.summonItemManager = new SummonItemManager(this);
        this.bossManager = new BossManager(this);
        
        // Register event listener
        getServer().getPluginManager().registerEvents(new BossListener(this, bossManager, summonItemManager), this);
        
        // Register command
        FalseProphetCommand commandHandler = new FalseProphetCommand(this, bossManager, summonItemManager);
        getCommand("falseprophet").setExecutor(commandHandler);
        getCommand("falseprophet").setTabCompleter(commandHandler);
        
        getLogger().info("The False Prophet has been awakened!");
    }
    
    @Override
    public void onDisable() {
        // Clean up active boss fights
        if (bossManager != null) {
            bossManager.cleanup();
        }
        
        getLogger().info("The False Prophet has been banished!");
    }
    
    /**
     * Get the boss manager instance.
     * @return The boss manager
     */
    public BossManager getBossManager() {
        return bossManager;
    }
    
    /**
     * Get the summon item manager instance.
     * @return The summon item manager
     */
    public SummonItemManager getSummonItemManager() {
        return summonItemManager;
    }
}

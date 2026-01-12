package com.fikjul.falseprophet;

/**
 * Represents the different states of The False Prophet boss.
 * This enum is used to implement the boss's finite state machine.
 */
public enum BossState {
    /**
     * Boss has just spawned and is initializing
     */
    SPAWNING,
    
    /**
     * Boss is immune to damage (false villagers are alive)
     */
    IMMUNE,
    
    /**
     * Boss can take damage (all false villagers are dead)
     */
    VULNERABLE,
    
    /**
     * Boss HP is below 30%, more aggressive phase
     */
    ENRAGED,
    
    /**
     * Boss has been defeated
     */
    DEAD
}

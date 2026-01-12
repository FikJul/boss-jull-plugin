package com.fikjul.bossjull.falseprophet;

/**
 * Represents the various states of the False Prophet boss fight.
 * The boss transitions through these states based on gameplay conditions.
 */
public enum BossState {
    /**
     * Boss is being summoned - initial spawn phase
     */
    SPAWNING,
    
    /**
     * Boss cannot take damage - minions are alive
     */
    IMMUNE,
    
    /**
     * Boss can take damage - all minions are dead
     */
    VULNERABLE,
    
    /**
     * Boss HP < 30%, more aggressive behavior
     */
    ENRAGED,
    
    /**
     * Boss has been defeated
     */
    DEAD
}

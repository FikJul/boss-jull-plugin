package com.fikjul.falseprophet;

/**
 * Represents the different states of The False Prophet boss fight.
 * States control damage immunity, minion spawning, and boss behavior.
 */
public enum BossState {
    /**
     * Initial spawn animation state (3 seconds).
     * Boss is invulnerable and initial minions are summoned.
     */
    SPAWNING,
    
    /**
     * Boss is protected by false followers and cannot take damage.
     * At least one false villager must be alive.
     */
    IMMUNE,
    
    /**
     * All false villagers are dead, boss can take damage.
     * Lasts 6 seconds (4 seconds when ENRAGED) before returning to IMMUNE.
     */
    VULNERABLE,
    
    /**
     * Boss HP is below 30% (150/500 HP).
     * Summons more minions and has reduced vulnerable duration.
     */
    ENRAGED,
    
    /**
     * Boss has been defeated.
     * Triggers death effects and cleanup.
     */
    DEAD
}

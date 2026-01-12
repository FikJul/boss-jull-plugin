package com.fikjul.bossjull;

/**
 * Finite State Machine states for the Sentinel boss fight.
 */
public enum BossState {
    /**
     * Boss is waiting for combat to begin (idle state).
     */
    IDLE,
    
    /**
     * Phase 1 combat with normal ring positions.
     */
    PHASE_1_COMBAT,
    
    /**
     * Transition phase with visual cues.
     */
    PHASE_2_TRANSITION,
    
    /**
     * Phase 2 combat with swapped ring positions.
     */
    PHASE_2_COMBAT,
    
    /**
     * Boss has been defeated.
     */
    DEAD
}

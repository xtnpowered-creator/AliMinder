package com.aliminder.app.domain.model

/**
 * Persona stages representing the emotional arc of AliMinder's interventions.
 * 
 * The persona evolves based on proximity to PoNR (Point of No Return):
 * - Optimistic: ≥30 minutes until PoNR
 * - Weary: 15-30 minutes until PoNR  
 * - Grave: Past PoNR (user is objectively late)
 */
enum class PersonaStage {
    /** T-Minus 30m+: Brief, helpful, slightly snarky */
    OPTIMISTIC,
    
    /** T-Minus 15-30m: Disappointed, audibly exhausted */
    WEARY,
    
    /** Past PoNR: No advice, only consequences */
    GRAVE;
    
    companion object {
        /**
         * Determines persona stage based on delta (minutes until PoNR).
         * 
         * @param deltaMinutes Minutes until PoNR (negative = late)
         * @return Appropriate persona stage
         */
        fun fromDelta(deltaMinutes: Int): PersonaStage {
            return when {
                deltaMinutes >= 30 -> OPTIMISTIC
                deltaMinutes >= 15 -> WEARY
                else -> GRAVE  // Includes negative values (late)
            }
        }
    }
}

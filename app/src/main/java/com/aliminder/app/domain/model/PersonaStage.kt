package com.aliminder.app.domain.model

/**
 * Persona stages representing the emotional arc of AliMinder's interventions.
 * 
 * The persona evolves based on proximity to PoNR (Point of No Return) and Start Time:
 * - Optimistic: > 30 minutes until PoNR
 * - Weary: ≤ 30 minutes until PoNR
 * - Urgent (formerly Grave): Past PoNR, but BEFORE Start Time
 * - Late: Past Start Time
 */
enum class PersonaStage {
    /** T-Minus 30m+ until PoNR: Brief, helpful, slightly snarky */
    OPTIMISTIC,
    
    /** T-Minus 30m until PoNR: Disappointed, audibly exhausted */
    WEARY,
    
    /** Past PoNR, Pre-Start: High alert, you are late to leave */
    URGENT,
    
    /** Past Start Time: You have missed the start */
    LATE;
}

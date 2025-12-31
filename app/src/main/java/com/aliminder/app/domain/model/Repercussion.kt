package com.aliminder.app.domain.model

/**
 * Repercussion category for contextual fear matching.
 */
enum class RepercussionCategory {
    PROFESSIONAL,
    SOCIAL,
    FINANCIAL,
    PERSONAL
}

/**
 * Repercussion (Hardwired Fear) configuration.
 * 
 * Represents a user-defined consequence used in Grave stage scripting.
 * The "Social Mirror" logic selects appropriate fears based on event context.
 */
data class Repercussion(
    /** Unique identifier */
    val id: String,
    
    /** User-defined fear text (e.g., "Boss's disapproval") */
    val text: String,
    
    /** Severity rating (1-10, user-defined) */
    val gravityScore: Int,
    
    /** Primary category */
    val category: RepercussionCategory,
    
    /** Context tags for matching (e.g., ["work", "1:1_meeting"]) */
    val contextTags: List<String> = emptyList(),
    
    /** Number of associated outro audio clips */
    val associatedClipsCount: Int = 0
) {
    init {
        require(gravityScore in 1..10) {
            "Gravity score must be between 1 and 10"
        }
    }
    
    /**
     * Checks if this repercussion matches the given context tags.
     */
    fun matchesContext(tags: List<String>): Boolean {
        return contextTags.any { it in tags }
    }
}

package com.aliminder.app.domain.model

/**
 * Structured address with separate fields for API compatibility.
 * Ensures proper formatting for Google Maps Distance Matrix API.
 */
data class Address(
    val street: String,      // Street number and name (e.g., "123 Main St")
    val city: String,        // City name (e.g., "Austin")
    val state: String,       // 2-letter uppercase code (e.g., "TX")
    val zipCode: String      // 5-digit ZIP code (e.g., "78701")
) {
    /**
     * Format address for Google Maps API.
     * Returns: "123 Main St, Austin, TX 78701"
     */
    fun toGoogleMapsFormat(): String {
        return "$street, $city, $state $zipCode"
    }
    
    /**
     * Format address for display (multi-line).
     * Returns:
     * "123 Main St
     *  Austin, TX 78701"
     */
    fun toDisplayString(): String {
        return "$street\n$city, $state $zipCode"
    }
    
    /**
     * Format address for single-line display.
     * Returns: "123 Main St, Austin, TX 78701"
     */
    fun toSingleLineString(): String {
        return toGoogleMapsFormat()
    }
    
    companion object {
        /**
         * Validate state code (2 uppercase letters).
         */
        fun isValidState(state: String): Boolean {
            return state.matches(Regex("^[A-Z]{2}$"))
        }
        
        /**
         * Validate ZIP code (5 digits).
         */
        fun isValidZipCode(zip: String): Boolean {
            return zip.matches(Regex("^\\d{5}$"))
        }
    }
}

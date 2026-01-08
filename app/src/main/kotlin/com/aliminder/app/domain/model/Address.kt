package com.aliminder.app.domain.model

/**
 * Structured address with separate fields for API compatibility.
 * Ensures proper formatting for Google Maps Distance Matrix API.
 */
data class Address(
    val name: String? = null,    // Location Name (e.g., "Starbucks", "Home")
    val street: String,          // Street number and name (e.g., "123 Main St")
    val city: String,            // City name (e.g., "Austin")
    val state: String,           // 2-letter uppercase code (e.g., "TX")
    val zipCode: String          // 5-digit ZIP code (e.g., "78701")
) {
    /**
     * Format address for Google Maps API.
     * Returns: "123 Main St, Austin, TX 78701"
     * Ignores name as API routes to the address parts.
     */
    fun toGoogleMapsFormat(): String {
        return "$street, $city, $state $zipCode"
    }
    
    /**
     * Format address for display (multi-line).
     * Returns 3-line format if name exists, else 2-line.
     * Line 1: Name (e.g. "Work")
     * Line 2: Street
     * Line 3: City, State Zip
     */
    fun toDisplayString(): String {
        return if (!name.isNullOrBlank()) {
            "$name\n$street\n$city, $state $zipCode"
        } else {
            "$street\n$city, $state $zipCode"
        }
    }
    
    /**
     * Format address for single-line display.
     * Returns: "Name: 123 Main St, Austin, TX 78701"
     */
    fun toSingleLineString(): String {
        return if (!name.isNullOrBlank()) {
            "$name: ${toGoogleMapsFormat()}"
        } else {
            toGoogleMapsFormat()
        }
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

        /**
         * Smart Parse: Attempt to convert a raw string into a structured Address.
         * Used for legacy data or external provider strings.
         * 
         * Expected formats:
         * 1. "123 Main St, Austin, TX 78701"
         * 2. "Starbucks, 123 Main St, Austin, TX 78701"
         */
        fun parse(raw: String): Address {
            val parts = raw.split(",").map { it.trim() }
            
            // Fallback for simple strings
            if (parts.size < 3) {
                return Address(
                    name = null,
                    street = raw,
                    city = "",
                    state = "",
                    zipCode = ""
                )
            }

            // Parse backwards from the end (Zip/State usually last)
            val lastPart = parts.last() // "TX 78701" or "78701"
            
            // Try to extract State/Zip from last part
            val stateZipRegex = Regex("([A-Z]{2})\\s+(\\d{5})")
            val match = stateZipRegex.find(lastPart)
            
            val (state, zip) = if (match != null) {
                match.groupValues[1] to match.groupValues[2]
            } else {
                // Formatting is weird, just return blanks
                "" to ""
            }
            
            val city = if (parts.size >= 2) parts[parts.size - 2] else ""
            
            // Everything before City is Street (and maybe Name)
            // If there are > 2 parts pending, the first might be a Name
            // e.g. ["Starbucks", "123 Main St", "Austin", "TX 78701"]
            val remainingParts = parts.dropLast(2)
            
            val (name, street) = if (remainingParts.size > 1) {
                remainingParts.first() to remainingParts.drop(1).joinToString(", ")
            } else {
                null to remainingParts.joinToString(", ")
            }

            return Address(
                name = name,
                street = street,
                city = city,
                state = state,
                zipCode = zip
            )
        }
    }
}

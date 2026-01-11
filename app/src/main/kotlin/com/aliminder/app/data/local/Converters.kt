package com.aliminder.app.data.local

import androidx.room.TypeConverter
import com.aliminder.app.domain.model.Attendee
import com.aliminder.app.domain.model.ChecklistItem
import com.aliminder.app.domain.model.DutyPriority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters for complex Duty fields.
 * Uses Gson to serialize Lists and Objects to JSON strings.
 */
class AliMinderTypeConverters {
    private val gson = Gson()

    // --- LocalDateTime ---
    @TypeConverter
    fun toDate(dateString: String?): java.time.LocalDateTime? {
        return dateString?.let {
            java.time.LocalDateTime.parse(it)
        }
    }

    @TypeConverter
    fun toDateString(date: java.time.LocalDateTime?): String? {
        return date?.toString()
    }

    // --- DutyProvider ---
    @TypeConverter
    fun fromDutyProvider(provider: com.aliminder.app.domain.model.DutyProvider): String {
        return provider.name
    }

    @TypeConverter
    fun toDutyProvider(value: String): com.aliminder.app.domain.model.DutyProvider {
        return try {
            com.aliminder.app.domain.model.DutyProvider.valueOf(value)
        } catch (e: Exception) {
            com.aliminder.app.domain.model.DutyProvider.SHADOW // Fallback
        }
    }

    // --- Priority ---
    @TypeConverter
    fun fromPriority(priority: DutyPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): DutyPriority {
        return try {
            DutyPriority.valueOf(value)
        } catch (e: Exception) {
            DutyPriority.NORMAL // Fallback
        }
    }

    // --- Attendees ---
    @TypeConverter
    fun fromAttendeeList(attendees: List<Attendee>?): String? {
        if (attendees == null) return null
        return gson.toJson(attendees)
    }

    @TypeConverter
    fun toAttendeeList(json: String?): List<Attendee> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<Attendee>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Organizer (Single Attendee) ---
    @TypeConverter
    fun fromAttendee(attendee: Attendee?): String? {
        if (attendee == null) return null
        return gson.toJson(attendee)
    }

    @TypeConverter
    fun toAttendee(json: String?): Attendee? {
        if (json.isNullOrBlank()) return null
        return try {
            gson.fromJson(json, Attendee::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- Checklist ---
    @TypeConverter
    fun fromChecklist(checklist: List<ChecklistItem>?): String? {
        if (checklist == null) return null
        return gson.toJson(checklist)
    }

    @TypeConverter
    fun toChecklist(json: String?): List<ChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<ChecklistItem>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

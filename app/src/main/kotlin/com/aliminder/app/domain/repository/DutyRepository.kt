package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface DutyRepository {
    fun getAllDuties(): Flow<List<Event>>
}

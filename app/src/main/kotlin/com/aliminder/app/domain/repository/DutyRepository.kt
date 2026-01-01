package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.Duty
import kotlinx.coroutines.flow.Flow

interface DutyRepository {
    fun getAllDuties(): Flow<List<Duty>>
}

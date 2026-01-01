package com.aliminder.app.data.repository

import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.mapper.toDomainEvent
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.repository.DutyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DutyRepositoryImpl @Inject constructor(
    private val dutyDao: DutyDao
) : DutyRepository {

    override fun getAllDuties(): Flow<List<Event>> {
        return dutyDao.getAllDuties().map {
            it.map { dutyEntity -> dutyEntity.toDomainEvent() }
        }
    }
}

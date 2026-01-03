package com.aliminder.app.domain.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aliminder.app.domain.repository.DutyRepository
import com.aliminder.app.domain.repository.UserSettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AutoHideDutiesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dutyRepository: DutyRepository,
    private val userSettingsRepository: UserSettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userSettings = userSettingsRepository.getUserSettings().first()
            Log.d("AutoHide", "Worker running. Threshold: ${userSettings.autoHideOverdueMinutes} minutes")
            dutyRepository.autoHideOverdueDuties(userSettings.autoHideOverdueMinutes)
            Result.success()
        } catch (e: Exception) {
            // In case of error, retry the work
            Log.e("AutoHide", "Worker failed", e)
            Result.retry()
        }
    }
}

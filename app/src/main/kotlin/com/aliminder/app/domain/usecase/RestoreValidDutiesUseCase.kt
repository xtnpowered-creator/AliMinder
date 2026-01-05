package com.aliminder.app.domain.usecase

import com.aliminder.app.domain.repository.DutyRepository
import javax.inject.Inject

/**
 * Restores duties that were auto-hidden but are now valid under a new threshold.
 * 
 * This use case breaks the circular dependency between UserSettingsRepository
 * and DutyRepository by providing a separate layer for duty restoration logic.
 * 
 * Example: If threshold changes from 30 to 60 minutes, duties that were hidden
 * between 30-60 minutes ago will be restored to the active list.
 */
class RestoreValidDutiesUseCase @Inject constructor(
    private val dutyRepository: DutyRepository
) {
    /**
     * Restores duties that fall within the new auto-hide threshold.
     * 
     * @param newThresholdMinutes The new threshold in minutes
     */
    suspend operator fun invoke(newThresholdMinutes: Int) {
        dutyRepository.restoreNewlyValidDuties(newThresholdMinutes)
    }
}

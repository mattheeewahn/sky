package com.skytrace.app.services.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.skytrace.app.data.repository.SatelliteRepository
import com.skytrace.app.domain.model.SatelliteCategory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Background worker for syncing catalog data.
 * Runs periodically to keep TLE and asteroid data fresh.
 */
@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val satelliteRepository: SatelliteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Sync ISS/Stations TLE (most time-sensitive)
            satelliteRepository.syncCategory(SatelliteCategory.ISS)

            // Sync active satellites
            satelliteRepository.syncCategory(SatelliteCategory.ACTIVE)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "skytrace_data_sync"

        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
                6, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun enqueueSingleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}

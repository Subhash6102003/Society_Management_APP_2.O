package com.mgbheights.android.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mgbheights.shared.domain.repository.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Background worker for periodic data sync.
 * Runs every 15 minutes when network is available.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val noticeRepository: NoticeRepository,
    private val visitorRepository: VisitorRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("SyncWorker: Starting background sync")

            // Check if user is logged in
            if (!authRepository.isLoggedIn()) {
                Timber.d("SyncWorker: User not logged in, skipping sync")
                return Result.success()
            }

            // Sync user data
            val userResult = authRepository.getCurrentUser()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!

                // Sync bills
                maintenanceRepository.getAllBills()

                // Sync notices
                noticeRepository.getAllNotices()

                // Sync visitors (today's)
                visitorRepository.getTodaysVisitors()

                Timber.d("SyncWorker: Background sync completed successfully")
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker: Background sync failed")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "mgb_heights_periodic_sync"

        /**
         * Enqueue periodic sync work.
         */
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    SYNC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )

            Timber.d("SyncWorker: Periodic sync enqueued")
        }

        /**
         * Request an immediate one-time sync.
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueue(syncRequest)

            Timber.d("SyncWorker: One-time sync requested")
        }

        /**
         * Cancel all sync work.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(SYNC_WORK_NAME)
        }
    }
}


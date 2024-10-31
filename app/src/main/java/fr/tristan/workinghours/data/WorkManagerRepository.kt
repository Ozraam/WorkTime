package fr.tristan.workinghours.data

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import fr.tristan.workinghours.worker.NotificationWorker

interface LeaveRepository {
    fun prepareLeave(delay: Long)
}

class WorkManagerLeaveRepository(context: Context) : LeaveRepository {
    private val workManager = WorkManager.getInstance(context)

    override fun prepareLeave(delay: Long) {
        val leaveRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS).build()
        workManager.enqueueUniqueWork(
            "leave",
            androidx.work.ExistingWorkPolicy.REPLACE,
            leaveRequest
        )
    }

}
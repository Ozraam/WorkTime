package fr.tristan.workinghours.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.tristan.workinghours.R

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {

        makeStatusNotification(
            applicationContext.resources.getString(R.string.time_to_go),
            applicationContext
        )
        return Result.success()
    }
}
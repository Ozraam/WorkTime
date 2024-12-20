package fr.tristan.workinghours.data

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

interface WorkDayRepository {
    fun getAll(): Flow<List<WorkDay>>

    suspend fun insert(workDay: WorkDay)

    suspend fun update(workDay: WorkDay)

    suspend fun delete(workDay: WorkDay)
    suspend fun exportData(context: Context, callback: (String) -> Unit)
}

class OfflineWorkDayRepository(private val workDayDao: WorkDayDAO) : WorkDayRepository {
    override fun getAll(): Flow<List<WorkDay>> {
        return workDayDao.getAll()
    }

    override suspend fun insert(workDay: WorkDay) {
        workDayDao.insert(workDay)
    }

    override suspend fun update(workDay: WorkDay) {
        workDayDao.update(workDay)
    }

    override suspend fun delete(workDay: WorkDay) {
        workDayDao.delete(workDay)
    }

    override suspend fun exportData(context: Context, callback: (String) -> Unit) {
        val workDays = workDayDao.getAllSync()
        val csv = workDays.joinToString("\n") { it.toCSV() }
        val filename = "workdays.csv"
        val csvHeader = "date, start, end, launchStart, launchEnd"
        Log.d("WorkDayRepository", "Exporting data to $filename")

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, filename)

        withContext(Dispatchers.IO) {
            file.createNewFile()
            file.writeText("$csvHeader\n$csv")
            Log.d("WorkDayRepository", "Data exported to $file, ${file.exists()}, ${file.path}")
            callback(file.path)
        }
    }
}
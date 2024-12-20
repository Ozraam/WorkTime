package fr.tristan.workinghours.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class WorkDayRepositoryFake(private var workdays: List<WorkDay>) : WorkDayRepository {


    override suspend fun insert(workDay: WorkDay) {
        workdays += workDay
    }

    override suspend fun update(workDay: WorkDay) {
        val index = workdays.indexOfFirst { it.date == workDay.date }
        if (index != -1) {
            workdays = workdays.toMutableList().apply {
                this[index] = workDay
            }
        }
    }

    override fun getAll(): Flow<List<WorkDay>> {
        return flowOf(workdays)
    }


    override suspend fun delete(workDay: WorkDay) {
        workdays -= workDay
    }

    override suspend fun exportData(context: Context, callback: (String) -> Unit) {
        val csv = workdays.joinToString("\n") { it.toCSV() }
        val filename = "workdays.csv"
        val csvHeader = "date, start, end, launchStart, launchEnd"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(csvHeader.toByteArray())
            it.write(csv.toByteArray())
        }
    }
}

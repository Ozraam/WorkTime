package fr.tristan.workinghours.data

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
}

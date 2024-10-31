package fr.tristan.workinghours.data

import kotlinx.coroutines.flow.Flow

interface WorkDayRepository {
    fun getAll(): Flow<List<WorkDay>>

    suspend fun insert(workDay: WorkDay)

    suspend fun update(workDay: WorkDay)
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

}
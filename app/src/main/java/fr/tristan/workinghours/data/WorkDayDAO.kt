package fr.tristan.workinghours.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDayDAO {
    @Insert
    suspend fun insert(workDay: WorkDay)

    @Update
    suspend fun update(workDay: WorkDay)

    @Query("SELECT * FROM work_day")
    fun getAll(): Flow<List<WorkDay>>

    @Delete
    suspend fun delete(workDay: WorkDay)

    @Query("SELECT * FROM work_day")
    fun getAllSync(): List<WorkDay>
}

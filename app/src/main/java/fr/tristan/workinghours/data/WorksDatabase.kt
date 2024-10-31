package fr.tristan.workinghours.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Database(entities = [WorkDay::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class WorksDatabase : RoomDatabase(){
    abstract fun workDayDao(): WorkDayDAO

    companion object {
        @Volatile
        private var INSTANCE: WorksDatabase? = null

        fun getDatabase(context: Context): WorksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorksDatabase::class.java,
                    "works_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


class DateConverter {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}
package com.example.healthdiary.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM readings ORDER BY timestamp DESC")
    fun readings(): Flow<List<Reading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReading(reading: Reading)

    @Delete
    suspend fun deleteReading(reading: Reading)

    @Query("SELECT * FROM medications ORDER BY name COLLATE NOCASE")
    fun medications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)
}

/** v1 -> v2: morning/evening and the medicines taken were added to every reading. */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE readings ADD COLUMN dayPart TEXT NOT NULL DEFAULT 'MORNING'")
        db.execSQL("ALTER TABLE readings ADD COLUMN meds TEXT NOT NULL DEFAULT ''")
    }
}

@Database(entities = [Reading::class, Medication::class], version = 2, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun dao(): DiaryDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun get(context: Context): AppDb =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    "diary.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}

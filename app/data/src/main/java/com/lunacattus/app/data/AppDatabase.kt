package com.lunacattus.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lunacattus.app.data.entity.DataEntity

@Database(
    entities = [DataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataDao(): DataDao
}
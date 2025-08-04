package com.lunacattus.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lunacattus.app.data.entity.DataEntity
import com.lunacattus.app.data.entity.VideoEntity

@Database(
    entities = [DataEntity::class, VideoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataDao(): DataDao
    abstract fun videoDao(): VideoDao
}
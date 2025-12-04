package com.lunacattus.app.player.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lunacattus.app.player.model.VideoEntity

@Database(
    entities = [VideoEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}
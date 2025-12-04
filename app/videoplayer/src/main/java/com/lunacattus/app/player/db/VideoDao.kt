package com.lunacattus.app.player.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lunacattus.app.player.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Insert
    suspend fun insertPlayList(videoEntity: VideoEntity)

    @Query("SELECT * FROM VideoEntity")
    fun queryAllVideo(): Flow<List<VideoEntity>>

    @Query("DELETE FROM VideoEntity WHERE id =:id ")
    suspend fun deleteVideo(id: String)
}
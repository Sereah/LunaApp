package com.lunacattus.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lunacattus.app.data.entity.DataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataDao {

    @Query("SELECT * FROM data")
    fun queryAllData(): Flow<List<DataEntity>>

    @Insert
    suspend fun insertData(dataEntity: DataEntity)
}
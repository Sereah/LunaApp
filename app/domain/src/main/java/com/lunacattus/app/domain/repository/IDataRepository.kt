package com.lunacattus.app.domain.repository

import com.lunacattus.app.domain.model.Data
import kotlinx.coroutines.flow.Flow

interface IDataRepository {
    suspend fun insertData(data: Data): Result<Unit>
    fun queryAllData(): Result<Flow<List<Data>>>
}
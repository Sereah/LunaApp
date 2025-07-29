package com.lunacattus.app.data.repository

import com.lunacattus.app.data.DataDao
import com.lunacattus.app.data.entity.mapper
import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.domain.repository.IDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
    private val dataDao: DataDao
) : IDataRepository {

    override suspend fun insertData(data: Data): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataDao.insertData(data.mapper())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun queryAllData(): Result<Flow<List<Data>>> {
        return try {
            val query = dataDao.queryAllData().map { it.map { it.mapper() } }
            Result.success(query)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

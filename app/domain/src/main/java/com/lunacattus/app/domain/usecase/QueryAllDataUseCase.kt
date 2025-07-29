package com.lunacattus.app.domain.usecase

import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.domain.repository.IDataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QueryAllDataUseCase @Inject constructor(
    private val repository: IDataRepository
) {
    operator fun invoke(): Result<Flow<List<Data>>> = repository.queryAllData()
}
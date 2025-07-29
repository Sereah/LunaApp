package com.lunacattus.app.domain.usecase

import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.domain.repository.IDataRepository
import javax.inject.Inject

class InsertDataUseCase @Inject constructor(
    private val repository: IDataRepository
) {
    suspend operator fun invoke(data: Data): Result<Unit> = repository.insertData(data)
}
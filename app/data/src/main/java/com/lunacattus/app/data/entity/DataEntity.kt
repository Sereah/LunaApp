package com.lunacattus.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lunacattus.app.domain.model.Data

@Entity(tableName = "data")
data class DataEntity(
    @PrimaryKey val id: String,
    val name: String,
)

fun Data.mapper(): DataEntity {
    return DataEntity(
        id = this.id,
        name = this.name,
    )
}

fun DataEntity.mapper(): Data {
    return Data(
        id = this.id,
        name = this.name,
    )
}
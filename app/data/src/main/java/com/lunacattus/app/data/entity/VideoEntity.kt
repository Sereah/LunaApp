package com.lunacattus.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lunacattus.app.domain.model.Video

@Entity(tableName = "VideoEntity")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subTitle: String,
    val description: String,
    val coverPic: String,
    val uri: String
)

fun Video.mapper(): VideoEntity {
    return VideoEntity(
        id = id,
        title = this.title,
        subTitle = this.subtitle,
        description = this.description,
        coverPic = coverPic,
        uri = this.uri
    )
}

fun VideoEntity.mapper(): Video {
    return Video(
        id = id,
        description = description,
        uri = uri,
        subtitle = subTitle,
        coverPic = coverPic,
        title = title,
    )
}
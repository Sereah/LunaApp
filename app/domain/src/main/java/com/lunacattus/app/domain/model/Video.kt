package com.lunacattus.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

data class JsonVideos(
    val videos: List<JsonVideo>
)

@Parcelize
data class JsonVideo(
    val description: String,
    val sources: List<String>,
    val subtitle: String,
    val coverPic: String,
    val title: String
) : Parcelable

data class Video(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val uri: String,
    val subtitle: String,
    val coverPic: String,
    val title: String
)

fun JsonVideo.mapper() : Video {
    return Video(
        description = description,
        uri = sources.first(),
        subtitle = subtitle,
        coverPic = coverPic,
        title = title
    )
}
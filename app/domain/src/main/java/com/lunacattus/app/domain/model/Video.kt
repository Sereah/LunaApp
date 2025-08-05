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
    val title: String,
    val type: VideoType,
)

sealed interface VideoType {
    data object LocalVideo : VideoType
    data object WebStream : VideoType
    data object JsonFile : VideoType
    data object Unknow : VideoType

    companion object {
        fun fromString(value: String): VideoType = when (value) {
            "WebStream" -> WebStream
            "JsonFile" -> JsonFile
            "LocalVideo" -> LocalVideo
            else -> Unknow
        }

        fun toString(type: VideoType): String = when (type) {
            WebStream -> "WebStream"
            JsonFile -> "JsonFile"
            LocalVideo -> "LocalVideo"
            else -> "Unknow"
        }
    }
}

fun JsonVideo.mapper(): Video {
    return Video(
        description = description,
        uri = sources.first(),
        subtitle = subtitle,
        coverPic = coverPic,
        title = title,
        type = VideoType.JsonFile
    )
}
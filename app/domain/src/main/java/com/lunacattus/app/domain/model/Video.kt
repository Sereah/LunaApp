package com.lunacattus.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Videos(
    val videos: List<Video>
)

@Parcelize
data class Video(
    val description: String,
    val sources: List<String>,
    val subtitle: String,
    val thumb: String,
    val title: String
) : Parcelable
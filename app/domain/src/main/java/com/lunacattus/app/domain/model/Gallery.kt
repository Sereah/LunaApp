package com.lunacattus.app.domain.model

import android.net.Uri

data class GalleryImage(
    val id: Long,
    val name: String,
    val addData: Long,
    val addDataDisplay: String,
    val contentUri: Uri,
)

data class GalleryVideo(
    val id: Long,
    val name: String,
    val addData: Long,
    val addDataDisplay: String,
    val contentUri: Uri,
)

data class GalleryDate(
    val date: String,
)

sealed class Gallery {
    data class Image(val galleryImage: GalleryImage) : Gallery()
    data class Video(val galleryVideo: GalleryVideo) : Gallery()
    data class Date(val galleryDate: GalleryDate) : Gallery()
    data object OtherFile : Gallery()
}

val Gallery.id: Long?
    get() = when (this) {
        is Gallery.Image -> this.galleryImage.id
        is Gallery.Video -> this.galleryVideo.id
        else -> null
    }

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

sealed class Gallery {
    data class Image(val galleryImage: GalleryImage) : Gallery()
    data class Video(val galleryVideo: GalleryVideo) : Gallery()
}

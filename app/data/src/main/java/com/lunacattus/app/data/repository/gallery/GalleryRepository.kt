package com.lunacattus.app.data.repository.gallery

import android.content.Context
import android.provider.MediaStore
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.domain.repository.IGalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    private val context: Context,
    mediaStoreQueryManagerFactory: MediaStoreQueryManager.Factory,
) : IGalleryRepository {

    private val imageQuery: MediaStoreQueryManager =
        mediaStoreQueryManagerFactory.create(
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection = imageProjection,
            cursorMapper = { cursor ->
                imageCursorToGallery(cursor)
            }
        )

    private val videoQuery: MediaStoreQueryManager =
        mediaStoreQueryManagerFactory.create(
            uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection = videoProjection,
            cursorMapper = { cursor ->
                videoCursorToGallery(cursor)
            }
        )

    private val allQuery: MediaStoreQueryManager =
        mediaStoreQueryManagerFactory.create(
            uri = MediaStore.Files.getContentUri("external"),
            projection = allMediaProjection,
            cursorMapper = { cursor ->
                allMediaCursorToGallery(cursor)
            }
        )

    override fun queryAllPic(): Flow<List<Gallery>> {
        return imageQuery.list
    }

    override suspend fun loadMorePic(pageSize: Int) {
        imageQuery.loadMore(pageSize)
    }

    override fun queryAllVideo(): Flow<List<Gallery>> {
        return videoQuery.list
    }

    override suspend fun loadMoreVideo(pageSize: Int) {
        videoQuery.loadMore(pageSize)
    }

    override fun queryAllMedia(): Flow<List<Gallery>> {
        return allQuery.list
    }

    override suspend fun loadMoreMedia(pageSize: Int) {
        allQuery.loadMore(pageSize)
    }

    override fun cleared() {
        imageQuery.cleared()
        videoQuery.cleared()
        allQuery.cleared()
    }

    companion object {
        const val TAG = "GalleryRepository"
    }
}
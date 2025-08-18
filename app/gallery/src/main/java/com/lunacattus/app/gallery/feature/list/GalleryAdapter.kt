package com.lunacattus.app.gallery.feature.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lunacattus.app.base.view.setOnClickListenerWithDebounce
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.gallery.databinding.ItemListImageBinding
import com.lunacattus.app.gallery.databinding.ItemListVideoBinding

class GalleryAdapter(
    private val context: Context,
    private val onItemClick: (Gallery) -> Unit
) : PagingDataAdapter<Gallery, RecyclerView.ViewHolder>(GALLERY_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val imageBinding = ItemListImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val videoBinding = ItemListVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return when (viewType) {
            TYPE_IMAGE -> ListAdapterImageViewHolder(imageBinding)
            TYPE_VIDEO -> ListAdapterVideoViewHolder(videoBinding)
            else -> throw IllegalArgumentException("Unknown Gallery item type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is Gallery.Image -> (holder as ListAdapterImageViewHolder).bind(item, position)
            is Gallery.Video -> (holder as ListAdapterVideoViewHolder).bind(item, position)
            else -> throw IllegalArgumentException("Unknown Gallery item type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Gallery.Image -> TYPE_IMAGE
            is Gallery.Video -> TYPE_VIDEO
            else -> throw IllegalArgumentException("Unknown Gallery item type")
        }
    }

    companion object {
        private val GALLERY_COMPARATOR = object : DiffUtil.ItemCallback<Gallery>() {
            override fun areItemsTheSame(oldItem: Gallery, newItem: Gallery): Boolean {
                return when {
                    oldItem is Gallery.Image && newItem is Gallery.Image -> oldItem.galleryImage.id == newItem.galleryImage.id
                    oldItem is Gallery.Video && newItem is Gallery.Video -> oldItem.galleryVideo.id == newItem.galleryVideo.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: Gallery, newItem: Gallery): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1
    }

    inner class ListAdapterImageViewHolder(val binding: ItemListImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Gallery?, position: Int) {
            val image = item as? Gallery.Image
            if (image == null) {
                binding.image.setImageURI(null)
            } else {
                Glide.with(context)
                    .load(image.galleryImage.contentUri)
                    .into(binding.image)
            }
            binding.position.text = position.toString()
            binding.id.text = image?.galleryImage?.id.toString()
            binding.root.setOnClickListenerWithDebounce {
                item?.let {
                    onItemClick(it)
                }
            }
        }
    }

    inner class ListAdapterVideoViewHolder(val binding: ItemListVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Gallery?, position: Int) {
            val video = item as? Gallery.Video
            if (video != null) {
                val uri = video.galleryVideo.contentUri
                Glide.with(context)
                    .load(uri)
                    .into(binding.bg)
            }
            binding.position.text = position.toString()
            binding.id.text = video?.galleryVideo?.id.toString()
            binding.root.setOnClickListenerWithDebounce {
                item?.let {
                    onItemClick(it)
                }
            }
        }
    }

}
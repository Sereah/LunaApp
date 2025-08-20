package com.lunacattus.app.gallery.feature.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lunacattus.app.base.view.setOnClickListenerWithDebounce
import com.lunacattus.app.domain.model.Gallery
import com.lunacattus.app.gallery.databinding.ItemListImageBinding
import com.lunacattus.app.gallery.databinding.ItemListTitleBinding
import com.lunacattus.app.gallery.databinding.ItemListVideoBinding

class GalleryListAdapter(
    private val onItemClick: (Gallery) -> Unit
) : ListAdapter<Gallery, RecyclerView.ViewHolder>(GALLERY_COMPARATOR) {

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
        val dateBinding = ItemListTitleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return when (viewType) {
            TYPE_IMAGE -> ListAdapterImageViewHolder(imageBinding)
            TYPE_VIDEO -> ListAdapterVideoViewHolder(videoBinding)
            TYPE_TITLE -> ListAdapterTitleViewHolder(dateBinding)
            else -> throw IllegalArgumentException("Unknown Gallery item type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is Gallery.Image -> (holder as ListAdapterImageViewHolder).bind(item)
            is Gallery.Video -> (holder as ListAdapterVideoViewHolder).bind(item)
            is Gallery.Date -> (holder as ListAdapterTitleViewHolder).bind(item)
            else -> throw IllegalArgumentException("Unknown Gallery item type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Gallery.Image -> TYPE_IMAGE
            is Gallery.Video -> TYPE_VIDEO
            is Gallery.Date -> TYPE_TITLE
            else -> throw IllegalArgumentException("Unknown Gallery item type")
        }
    }

    companion object {
        private val GALLERY_COMPARATOR = object : DiffUtil.ItemCallback<Gallery>() {
            override fun areItemsTheSame(oldItem: Gallery, newItem: Gallery): Boolean {
                return when {
                    oldItem is Gallery.Image && newItem is Gallery.Image -> oldItem.galleryImage.id == newItem.galleryImage.id
                    oldItem is Gallery.Video && newItem is Gallery.Video -> oldItem.galleryVideo.id == newItem.galleryVideo.id
                    oldItem is Gallery.Date && newItem is Gallery.Date -> oldItem.galleryDate.date == newItem.galleryDate.date
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: Gallery, newItem: Gallery): Boolean {
                return oldItem == newItem
            }
        }
        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1
        const val TYPE_TITLE = 2
    }

    inner class ListAdapterTitleViewHolder(val binding: ItemListTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Gallery.Date) {
            binding.title.text = item.galleryDate.date
        }
    }

    inner class ListAdapterImageViewHolder(val binding: ItemListImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Gallery.Image) {
            Glide.with(binding.root)
                .load(item.galleryImage.contentUri)
                .into(binding.image)
            binding.root.setOnClickListenerWithDebounce {
                onItemClick(item)
            }
        }
    }

    inner class ListAdapterVideoViewHolder(val binding: ItemListVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Gallery.Video) {
            val uri = video.galleryVideo.contentUri
            Glide.with(binding.root)
                .load(uri)
                .into(binding.bg)
            binding.root.setOnClickListenerWithDebounce {
                onItemClick(video)
            }
        }
    }

}
package com.lunacattus.app.presentation.view.feature.one

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.presentation.view.databinding.ItemTextViewBinding

class DataListAdapter : ListAdapter<Data, DataListViewHolder>(
    object : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(
            oldItem: Data,
            newItem: Data
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Data,
            newItem: Data
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DataListViewHolder {
        val binding = ItemTextViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DataListViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        val binding = holder.binding
        binding.textView.text = item.name
        binding.textView.textSize = 20f
    }
}

class DataListViewHolder(val binding: ItemTextViewBinding) : RecyclerView.ViewHolder(binding.root)
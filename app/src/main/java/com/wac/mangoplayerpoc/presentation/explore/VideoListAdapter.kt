package com.wac.mangoplayerpoc.presentation.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.databinding.ItemSongBinding
import com.wac.mangoplayerpoc.databinding.ItemSongListLayoutBinding

class VideoListAdapter (private val glide: RequestManager) :
    ListAdapter<Song, VideoListAdapter.ItemViewHolder>(DiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSongListLayoutBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ItemSongListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) = with(binding) {
            glide.load(item.imageUrl).into(songIcon)
//            songTitle.text = item.title
            songSubTitle.text = item.title

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(item)

                }
            }
        }
    }

    private var onItemClickListener: ((Song) -> Unit)? = null
    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

}

class DiffCallBack : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }

}
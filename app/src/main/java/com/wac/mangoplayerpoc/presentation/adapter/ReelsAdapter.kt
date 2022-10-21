package com.wac.mangoplayerpoc.presentation.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.wac.mangoplayerpoc.data.Reel
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.databinding.ReelsItemLayoutBinding


class ReelsAdapter(private val glide: RequestManager) :
    ListAdapter<Reel, ReelsAdapter.ItemViewHolder>(DiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ReelsItemLayoutBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReelsAdapter.ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: ReelsItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(item: Reel) = with(binding) {
//            val audioAttributes = AudioAttributes.Builder()
//                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
//                .setUsage(C.USAGE_MEDIA)
//                .build()
//
//            val exoPlayer = ExoPlayer.Builder(itemView.context).apply {
//                setAudioAttributes(audioAttributes, false)
//                setHandleAudioBecomingNoisy(true)
//            }.build()
//            exoPlayer.volume = 0f
//            exoPlayer.setMediaSource(
//                ProgressiveMediaSource.Factory(DefaultDataSource.Factory(itemView.context))
//                    .createMediaSource(
//                        MediaItem.fromUri(
//                            item.video
//                        )
//                    )
//            )
//            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
//            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
//            exoPlayer.prepare()
//            exoPlayer.playWhenReady = false
//            binding.reelsVideoView.player = exoPlayer


            glide.load(item.video).into(binding.videoImage)

//            binding.videoView.setVideoURI(Uri.parse(item.video))
//            binding.videoView.start()
            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(item)

                }
            }
        }
    }

    private var onItemClickListener: ((Reel) -> Unit)? = null
    fun setOnItemClickListener(listener: (Reel) -> Unit) {
        onItemClickListener = listener
    }


    class DiffCallBack : DiffUtil.ItemCallback<Reel>() {
        override fun areItemsTheSame(oldItem: Reel, newItem: Reel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reel, newItem: Reel): Boolean {
            return oldItem == newItem
        }

    }
}
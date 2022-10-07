package com.wac.mangoplayerpoc.repository

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.wac.mangoplayerpoc.data.VideoSongResponse

interface ApplicationRepository {
    fun saveLastPlayed(description: MediaDescriptionCompat, position: Long)
    fun getLastPlayed(): MediaBrowserCompat.MediaItem?
    fun getVideoSong(): List<VideoSongResponse.VideoItem>
    fun setVideoPlay(set: Boolean)
    fun getVideoPlay(): Boolean
}
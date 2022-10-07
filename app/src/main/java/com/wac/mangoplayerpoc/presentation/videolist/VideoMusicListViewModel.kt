package com.wac.mangoplayerpoc.presentation.videolist

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.ViewModel
import com.wac.mangoplayerpoc.common.Constants
import com.wac.mangoplayerpoc.common.Constants.MEDIA_ROOT_ID
import com.wac.mangoplayerpoc.data.VideoSongResponse
import com.wac.mangoplayerpoc.exoplayer.MusicService
import com.wac.mangoplayerpoc.exoplayer.MusicServiceConnection
import com.wac.mangoplayerpoc.repository.ApplicationRepository
import com.wac.mangoplayerpoc.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoMusicListViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val applicationRepository: ApplicationRepository,
    private val musicServiceConnection: MusicServiceConnection,
    private val application: Application

) : ViewModel() {
//    val musicServiceConnection = MusicServiceConnection(application)

    val videoMusic = applicationRepository.getVideoSong()

    init {
//        setVideoMusic()
//        val args = Bundle()
//        args.putInt("nRecNo", 2)
//        musicServiceConnection.sendCommand("video", args)
//        musicServiceConnection.mediaBrowser.disconnect()
//        musicServiceConnection.mediaBrowser.connect()
    }

    fun setVideoMusic() {

        videoMusic.map { it.toMusic() }.let {
            songRepository.setVideoMusic(it)
            val args = Bundle()
            args.putInt("nRecNo", 2)
            musicServiceConnection.sendCommand("video", args)
        }
    }
}
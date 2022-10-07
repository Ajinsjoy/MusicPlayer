package com.wac.mangoplayerpoc.presentation.explore

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.wac.mangoplayerpoc.exoplayer.MusicServiceConnection
import com.wac.mangoplayerpoc.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val musicServiceConnection: MusicServiceConnection
) :
    ViewModel() {

    val videoSong1 = songRepository.getVideoSong()

    fun setVideoMusic() {

        videoSong1.map { it.toMusic() }.let {
            songRepository.setVideoMusic(it)
            val args = Bundle()
            args.putInt("nRecNo", 2)
            musicServiceConnection.sendCommand("video1", args)
        }
    }
}
package com.wac.mangoplayerpoc.presentation.explore

import android.app.Application
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.data.Reel
import com.wac.mangoplayerpoc.data.model.MusicModel
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.exoplayer.MusicServiceConnection
import com.wac.mangoplayerpoc.exoplayer.isPlayEnabled
import com.wac.mangoplayerpoc.exoplayer.isPlaying
import com.wac.mangoplayerpoc.exoplayer.isPrepared
import com.wac.mangoplayerpoc.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val application: Application,
    private val songRepository: SongRepository,
    private val musicServiceConnection: MusicServiceConnection
) :
    ViewModel() {

    private val curPlayingSong = musicServiceConnection.curPlayingSong
    private val playbackState = musicServiceConnection.playbackState

    val reels = listOf<Reel>(
        Reel(
            id = 1,
            video = "android.resource://" + application.packageName + "/" + R.raw.lake,
            userImage = "https://generated.photos/vue-static/home/hero/4.png",
            videoImage = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ForBiggerBlazes.jpg",
            userName = "Farhan Roy",
            isLiked = true,
            likesCount = 778,
            commentsCount = 156,
            comment = "Wkwkwk..."
        ),
        Reel(
            id = 2,
            video = "android.resource://" + application.packageName + "/" + R.raw.food,
            userImage = "https://generated.photos/vue-static/home/hero/7.png",
            videoImage = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ElephantsDream.jpg",
            userName = "Muhammad Ali",
            isLiked = true,
            likesCount = 5923,
            commentsCount = 11,
            comment = "Awas kamu ya klo pergi"
        ),
        Reel(
            id = 3,
            video = "android.resource://" + application.packageName + "/" + R.raw.icecream,
            userImage = "https://generated.photos/vue-static/home/hero/3.png",
            videoImage = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ForBiggerFun.jpg",
            userName = "Christian Juned",
            isLiked = true,
            likesCount = 2314,
            comment = "Es krim dingin sedapp",
            commentsCount = 200
        ),
        Reel(
            id = 4,
            video = "android.resource://" + application.packageName + "/" + R.raw.soapbubbles,
            userImage = "https://generated.photos/vue-static/home/hero/6.png",
            videoImage = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/ForBiggerJoyrides.jpg",
            userName = "Cak Jhon",
            isLiked = true,
            likesCount = 786,
            comment = "Ados slurr",
            commentsCount = 700
        ),
        Reel(
            id = 5,
            video = "android.resource://" + application.packageName + "/" + R.raw.castle,
            userImage = "https://generated.photos/vue-static/home/hero/2.png",
            videoImage = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/Sintel.jpg",
            userName = "David Dulkader",
            isLiked = true,
            likesCount = 1890,
            comment = "Kerjaan di tengah hutan",
            commentsCount = 232
        )

    )


    val videoSong1 = songRepository.getVideoSong()
    val song = songRepository.getSong()

    fun setVideoMusic(song: Song, play: Boolean) =viewModelScope.launch {

        videoSong1.map { it.toMusic() }.let {
            songRepository.setVideoMusic(it)
            val args = Bundle()
            args.putInt("nRecNo", 2)
            args.putParcelableArrayList("list",it as ArrayList<MusicModel>)
            musicServiceConnection.sendCommand("video1", args)
        }
        delay(100L)
        playOrToggleSong(song,play)
    }

    fun setSong(song: Song, play: Boolean) = viewModelScope.launch {
        this@ExploreViewModel.song.collect {
            songRepository.setVideoMusic(it)
            val args = Bundle()
            args.putInt("nRecNo", 2)
            args.putParcelableArrayList("list",it as ArrayList<MusicModel>)
            musicServiceConnection.sendCommand("song", args)
        }
        delay(100L)
        playOrToggleSong(song,play)
    }



    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) = viewModelScope.launch {

//        applicationRepository.saveLastPlayed(mediaItem.id)
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.id == curPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControl.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControl.play()
                    else -> Unit
                }
            }
        } else {

            musicServiceConnection.transportControl.playFromMediaId(mediaItem.id, null)
        }
    }

}
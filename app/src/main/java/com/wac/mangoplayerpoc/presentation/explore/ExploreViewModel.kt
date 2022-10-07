package com.wac.mangoplayerpoc.presentation.explore

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.google.common.reflect.Reflection.getPackageName
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.data.Reel
import com.wac.mangoplayerpoc.exoplayer.MusicServiceConnection
import com.wac.mangoplayerpoc.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val application: Application,
    private val songRepository: SongRepository,
    private val musicServiceConnection: MusicServiceConnection
) :
    ViewModel() {

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

    fun setVideoMusic() {

        videoSong1.map { it.toMusic() }.let {
            songRepository.setVideoMusic(it)
            val args = Bundle()
            args.putInt("nRecNo", 2)
            musicServiceConnection.sendCommand("video1", args)
        }
    }
}
package com.wac.mangoplayerpoc.presentation.main

import android.app.Application
import android.content.SharedPreferences
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.wac.mangoplayerpoc.common.Constants
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.exoplayer.*
import com.wac.mangoplayerpoc.repository.ApplicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val applicationRepository: ApplicationRepository,
    private val sharedPreferences: SharedPreferences,
    application: Application

) : ViewModel() {
    private val _mediaItem = MutableStateFlow(MainActivityState())
    val mediaItem = _mediaItem.asStateFlow()

    val context = application
//    val musicServiceConnection =MusicServiceConnection(application)

    var showVideo: Boolean = false

    var isPlayingSong: Boolean = false
    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    private val curPlayingSong = musicServiceConnection.curPlayingSong
    private val playbackState = musicServiceConnection.playbackState

    init {

        updateCurrentPlayerPosition()
        viewModelScope.launch {
            _mediaItem.emit(_mediaItem.value.copy(loading = true, showLastPlayedSong = false))
            playbackState.asFlow().collect {
                isPlayingSong = it?.isPlaying == true
                if (it?.isPlaying == true) {
                    getCurPlayingSong()

                }
                it?.let { it1 ->
                    _mediaItem.value.copy(
                        isPlaying = it1.isPlaying
                    )
                }?.let { it2 ->
                    _mediaItem.emit(
                        it2
                    )
                }
            }
        }

    }

    fun setPipMode(pipValue: Boolean) = viewModelScope.launch {
        _mediaItem.emit(_mediaItem.value.copy(pipMode = pipValue))
    }

    fun setVideoPlay(set: Boolean) {
        applicationRepository.setVideoPlay(set)
    }

    private fun getCurPlayingSong() = viewModelScope.launch {
        musicServiceConnection.curPlayingSong.asFlow().collect { mediaMetadata ->
            mediaMetadata?.description?.let {
                val song = Song(
                    id = it.mediaId.toString(),
                    title = it.title.toString(),
                    subTitle = it.subtitle.toString(),
                    songUrl = it.mediaUri.toString(),
                    imageUrl = it.iconUri.toString()
                )
                _mediaItem.emit(
                    _mediaItem.value.copy(
                        song = song,
                        loading = false,
                        showLastPlayedSong = true,
                        curPlayingSong = song
                    )
                )

            }

        }
    }

    private fun updateCurrentPlayerPosition() = viewModelScope.launch {
        while (true) {
            playbackState.value?.let { state ->
                val pos = state.currentPlaybackPosition.toInt()
                if (mediaItem.value.progress != pos) {
                    _mediaItem.update {
                        it.copy(
                            progress = pos,
                            duration = MusicService.cutSongDuration
                        )
                    }
                }
            }
            delay(100L)
        }
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControl.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControl.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControl.seekTo(pos)
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
            if (this@MainActivityViewModel.mediaItem.value.curPlayingSong == null)
                delay(100L)
            musicServiceConnection.transportControl.playFromMediaId(mediaItem.id, null)
        }
    }


    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(
            Constants.MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    fun setPlayVideo(play: Boolean) {
        _mediaItem.update {
            it.copy(
                playVideo = play
            )
        }
        sharedPreferences.edit().putBoolean("Video", play).apply()
    }


}
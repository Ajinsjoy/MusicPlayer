package com.wac.mangoplayerpoc.presentation.nowplaying

import android.app.Application
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.*
import com.google.android.exoplayer2.ExoPlayer
import com.wac.mangoplayerpoc.common.Constants
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.exoplayer.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val savedStateHandle: SavedStateHandle,
    private val musicServiceConnection: MusicServiceConnection,
    application: Application
) : ViewModel() {

    //    val musicServiceConnection =MusicServiceConnection(
//        application
//    )
    private val _mediaItem = MutableStateFlow(NowPlayingState())
    val mediaItem = _mediaItem

    private val _currentPlayingPosition = MutableLiveData<Long>()
    val currentPlayingPosition = _currentPlayingPosition

    private val _currentDuration = MutableLiveData<Long>()
    val currentDuration = _currentDuration

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    private val curPlayingSong = musicServiceConnection.curPlayingSong
    private val playbackState = musicServiceConnection.playbackState


    init {


        savedStateHandle.get<Song>("song")?.let { }
        updateCurrentPlayerPosition()
        viewModelScope.launch {
            _mediaItem.emit(_mediaItem.value.copy(loading = true))
            musicServiceConnection.subscribe(
                Constants.MEDIA_ROOT_ID,
                object : MediaBrowserCompat.SubscriptionCallback() {
                    override fun onChildrenLoaded(
                        parentId: String,
                        children: MutableList<MediaBrowserCompat.MediaItem>
                    ) {

                        super.onChildrenLoaded(parentId, children)
                        val items = children.map {
                            Song(
                                it.mediaId!!,
                                it.description.title.toString(),
                                it.description.subtitle.toString(),
                                it.description.mediaUri.toString(),
                                it.description.iconUri.toString()
                            )
                        }
                        viewModelScope.launch {
                            _mediaItem.emit(
                                _mediaItem.value.copy(
                                    songList = items,
                                    loading = false,

                                    )
                            )
                        }
                    }
                })
            getCurPlayingSong()
            playbackState.asFlow().collect {


                it?.isPlaying?.let { it1 ->
                    _mediaItem.value.copy(
                        isPlaying = it1
                    )
                }?.let { it2 ->
                    _mediaItem.emit(
                        it2
                    )
                }
                if (it?.isPlaying == true) {
                    getCurPlayingSong()
                }
                it?.position?.let { progress ->
                    _mediaItem.emit(
                        _mediaItem.value.copy(
                            progress = progress.toInt()
                        )
                    )
                }
            }
        }

    }


    private fun updateCurrentPlayerPosition() = viewModelScope.launch {
        while (true) {
            playbackState.value?.let { state ->

                val buffering = state.bufferingState
                _mediaItem.update { it.copy(buffering = buffering) }

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
                _mediaItem.emit(_mediaItem.value.copy(song = song, loading = false))

            }

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

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
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

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(
            Constants.MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }


}
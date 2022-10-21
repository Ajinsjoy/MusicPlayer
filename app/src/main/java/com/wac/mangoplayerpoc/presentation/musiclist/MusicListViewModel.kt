package com.wac.mangoplayerpoc.presentation.musiclist

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wac.mangoplayerpoc.common.Constants.MEDIA_ROOT_ID
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.exoplayer.*
import com.wac.mangoplayerpoc.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val musicServiceConnection: MusicServiceConnection,
    application: Application
) : ViewModel() {

//    val musicServiceConnection = MusicServiceConnection(application)
    private val _mediaItem = MutableStateFlow(MusicListState())
    val mediaItem = _mediaItem

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    private val curPlayingSong = musicServiceConnection.curPlayingSong
    private val playbackState = musicServiceConnection.playbackState

    init {

        viewModelScope.launch {

            _mediaItem.emit(_mediaItem.value.copy(loading = true))
            songRepository.getMusic().collect {
                _mediaItem.emit(
                    _mediaItem.value.copy(
                        loading = false,
                        song = it.map { song -> song.toSong() })
                )

            }

//            musicServiceConnection.subscribe(MEDIA_ROOT_ID,
//                object : MediaBrowserCompat.SubscriptionCallback() {
//                    override fun onChildrenLoaded(
//                        parentId: String,
//                        children: MutableList<MediaBrowserCompat.MediaItem>
//                    ) {
//                        super.onChildrenLoaded(parentId, children)
//                        val items = children.map {
//                            Song(
//                                it.mediaId!!,
//                                it.description.title.toString(),
//                                it.description.subtitle.toString(),
//                                it.description.mediaUri.toString(),
//                                it.description.iconUri.toString()
//                            )
//                        }
//                        viewModelScope.launch {
//                            _mediaItem.emit(_mediaItem.value.copy(song = items, loading = false))
//                        }
//                    }
//                })
        }

    }

   fun setSong() = viewModelScope.launch {

        val args = Bundle()
        args.putInt("nRecNo", 2)
        musicServiceConnection.sendCommand("song", args)


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
        if (isPrepared && mediaItem.id == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
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
        musicServiceConnection.unSubscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}
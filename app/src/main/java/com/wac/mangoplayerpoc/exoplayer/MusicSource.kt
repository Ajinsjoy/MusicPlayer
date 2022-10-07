package com.wac.mangoplayerpoc.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.wac.mangoplayerpoc.common.Constants
import com.wac.mangoplayerpoc.data.model.MusicModel
import com.wac.mangoplayerpoc.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class MusicSource @Inject constructor(
    private val songRepository: SongRepository

) {
    var songs = emptyList<MediaMetadataCompat>()
    lateinit var allSongs: List<MusicModel>
    suspend fun fetchMediaData(songId: String) = withContext(Dispatchers.IO) {
        state = State.STATE_INITIALIZING
        when (songId) {
            "video" -> {
                songRepository.getVideoMusic().collect {
                    allSongs = it
                }

            }
            "video1" -> {

                allSongs = songRepository.getVideoSong().map { it.toMusic() }

            }
            "song" -> {
                songRepository.getMusic().collect {
                    allSongs = it
                }

            }
        }


        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.artist)
                .putString(METADATA_KEY_MEDIA_ID, song.id)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_ALBUM, song.album)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.image)
                .putString(METADATA_KEY_MEDIA_URI, song.source)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.image)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.album)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.album)
                .build()
        }
        state = State.STATE_INITIALIZED
    }

    fun asMediaSource(dataSource: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSource).createMediaSource(
                MediaItem.fromUri(
                    song.getString(
                        METADATA_KEY_MEDIA_URI
                    ).toUri()
                )
            )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asVideoSource(dataSource: DefaultDataSource.Factory): ConcatenatingMediaSource {

        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val uri = song.getString(
                METADATA_KEY_MEDIA_URI
            )
            val hlsMediaSource: HlsMediaSource = HlsMediaSource.Factory(dataSource)
                .setAllowChunklessPreparation(false)
                .createMediaSource(MediaItem.fromUri(uri.toUri()))

            concatenatingMediaSource.addMediaSource(hlsMediaSource)
        }

        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == State.STATE_INITIALIZED)
            true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}
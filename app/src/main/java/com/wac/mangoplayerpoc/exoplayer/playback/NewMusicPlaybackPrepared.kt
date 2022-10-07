package com.wac.mangoplayerpoc.exoplayer.playback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.util.MimeTypes
import com.wac.mangoplayerpoc.common.Constants.MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
import com.wac.mangoplayerpoc.exoplayer.MusicService
import com.wac.mangoplayerpoc.exoplayer.MusicSource

class NewMusicPlaybackPrepared(private val musicSource: MusicSource) : MediaSessionConnector.PlaybackPreparer {
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private val musicService: MusicService = MusicService()

    /**
     * UAMP supports preparing (and playing) from search, as well as media ID, so those
     * capabilities are declared here.
     *
     * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
     */
    override fun getSupportedPrepareActions(): Long =
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

    override fun onPrepare(playWhenReady: Boolean) {
        val recentSong = musicService.applicationRepository.getLastPlayed() ?: return
        onPrepareFromMediaId(
            recentSong.mediaId!!,
            playWhenReady,
            recentSong.description.extras
        )
    }

    override fun onPrepareFromMediaId(
        mediaId: String,
        playWhenReady: Boolean,
        extras: Bundle?
    ) {
        musicSource.whenReady {
            val itemToPlay: MediaMetadataCompat? = musicSource.songs
                .find { item ->
                    item.description.mediaId == mediaId
                }
            if (itemToPlay == null) {
                Log.w("itemNull", "Content not found: MediaID=$mediaId")
                // TODO: Notify caller of the error.
            } else {

                val playbackStartPositionMs =
                    extras?.getLong(
                        MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS,
                        C.TIME_UNSET
                    )
                        ?: C.TIME_UNSET

                preparePlaylist(
                    buildPlaylist(itemToPlay),
                    itemToPlay,
                    playWhenReady,
                    playbackStartPositionMs
                )
            }
        }
    }


    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
        musicSource.whenReady {
//            val metadataList = musicSource.search(query, extras ?: Bundle.EMPTY)
//            if (metadataList.isNotEmpty()) {
//                preparePlaylist(
//                    metadataList,
//                    metadataList[0],
//                    playWhenReady,
//                    playbackStartPositionMs = C.TIME_UNSET
//                )
//            }
        }
    }


    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false


    private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
        musicSource.songs.filter { it.description.mediaId == item.description.mediaId }.sortedBy { it.description.mediaId }

    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        // Since the playlist was probably based on some ordering (such as tracks
        // on an album), find which window index to play first so that the song the
        // user actually wants to hear plays first.
        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
        currentPlaylistItems = metadataList
//
//        currentPlayer.playWhenReady = playWhenReady
//        currentPlayer.stop()
//        // Set playlist and prepare.
//        currentPlayer.setMediaItems(
//            metadataList.map { it.toMediaItem() }, initialWindowIndex, playbackStartPositionMs)
//        currentPlayer.prepare()
    }

    private fun MediaMetadataCompat.toMediaItem(): MediaItem {
        return with(MediaItem.Builder()) {
            setMediaId(description.mediaId.toString())
            setUri(description.mediaUri)
            setMimeType(MimeTypes.AUDIO_MPEG)
            setMediaMetadata(toMediaItemMetadata())
        }.build()
    }
    private fun MediaMetadataCompat.toMediaItemMetadata(): MediaMetadata {
        return with(MediaMetadata.Builder()) {
            setTitle(description.title)
            setDisplayTitle(description.title)
            setAlbumTitle(description.subtitle)
        }.build()
    }
}
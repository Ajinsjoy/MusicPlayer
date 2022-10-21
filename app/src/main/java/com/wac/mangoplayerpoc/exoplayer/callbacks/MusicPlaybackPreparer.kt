package com.wac.mangoplayerpoc.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.wac.mangoplayerpoc.common.Constants.MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
import com.wac.mangoplayerpoc.data.model.MusicModel
import com.wac.mangoplayerpoc.exoplayer.MusicService
import com.wac.mangoplayerpoc.exoplayer.MusicSource
import kotlinx.coroutines.launch

class MusicPlaybackPreparer(
    private val musicSource: MusicSource,
    private val playerPrepare: (MediaMetadataCompat?, Boolean, Long) -> Unit,
    private val musicService: MusicService
) : MediaSessionConnector.PlaybackPreparer {

    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {

        //edit data or fetch more data from api

        musicService.serviceScope.launch {
            val list = extras?.getParcelableArrayList<MusicModel>("list")
            Log.d("listFrom", "onCommand: $list")

            list?.let { musicSource.fetchMediaData(command, it) }
        }
//        musicService.notifyChildrenChanged(MEDIA_ROOT_ID)


        return false
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        musicSource.whenReady {
            var playbackStartPositionMs: Long = 0L
            val itemToPlay = musicSource.songs.find { mediaId == it.description.mediaId }
            if (itemToPlay != null) {
                playbackStartPositionMs =
                    extras?.getLong(
                        MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS,
                        C.TIME_UNSET
                    )
                        ?: C.TIME_UNSET
            }
            playerPrepare(itemToPlay, playWhenReady, playbackStartPositionMs)

        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit
    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}
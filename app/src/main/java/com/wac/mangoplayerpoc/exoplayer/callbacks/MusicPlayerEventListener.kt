package com.wac.mangoplayerpoc.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.wac.mangoplayerpoc.exoplayer.MusicNotificationManager
import com.wac.mangoplayerpoc.exoplayer.MusicService
import kotlinx.coroutines.launch

class MusicPlayerEventListener(
    private val musicService: MusicService,
) : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {

            Player.STATE_READY -> {
                    musicService.stopForeground(false)
            }
            Player.STATE_BUFFERING -> Unit
            else -> musicService.hideNotification()

        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "An Unknown error occurred ${error.message}", Toast.LENGTH_LONG).show()
    }


}
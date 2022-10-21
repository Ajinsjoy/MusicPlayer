package com.wac.mangoplayerpoc.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.gms.cast.framework.CastContext
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.common.Constants.MEDIA_ROOT_ID
import com.wac.mangoplayerpoc.common.Constants.NETWORK_ERROR
import com.wac.mangoplayerpoc.exoplayer.callbacks.MusicPlaybackPreparer
import com.wac.mangoplayerpoc.exoplayer.callbacks.MusicPlayerEventListener
import com.wac.mangoplayerpoc.exoplayer.callbacks.MusicPlayerNotificationListener
import com.wac.mangoplayerpoc.exoplayer.cast.CastMediaItemConverter
import com.wac.mangoplayerpoc.repository.ApplicationRepository
import com.wac.mangoplayerpoc.repository.SongRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactoryFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var applicationRepository: ApplicationRepository

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var musicSource: MusicSource

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var musicNotificationManager: MusicNotificationManager
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private lateinit var packageValidator: PackageValidator

    private val serviceJob = Job()
    val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var curPlayingSong: MediaMetadataCompat? = null

    private lateinit var currentPlayer: Player

    private var isPlayerInitialized = false

    companion object {
        var cutSongDuration = 0L
            private set
    }

    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()


    private val castPlayer: CastPlayer? by lazy {
        try {
            val castContext = CastContext.getSharedInstance(this)
            musicPlayerEventListener = MusicPlayerEventListener(this)
            CastPlayer(castContext, CastMediaItemConverter()).apply {
                setSessionAvailabilityListener(CastSessionAvailabilityListener())
                addListener(musicPlayerEventListener)
            }
        } catch (e: Exception) {
            null
        }
    }

    inner class CastSessionAvailabilityListener : SessionAvailabilityListener {


        override fun onCastSessionAvailable() {
            switchToPlayer(currentPlayer, castPlayer!!)

        }

        override fun onCastSessionUnavailable() {
            switchToPlayer(currentPlayer, exoPlayer)
        }
    }

    private fun switchToPlayer(previousPlayer: Player?, newPlayer: Player) {
        if (previousPlayer == newPlayer) {
            return
        }
        currentPlayer = newPlayer
        if (previousPlayer != null) {
            currentPlayer.clearMediaItems()
            currentPlayer.stop()
        }
        mediaSessionConnector.setPlayer(newPlayer)
        previousPlayer?.stop()

    }


    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        return super.bindService(service, conn, flags)
    }


    override fun onCreate() {
        super.onCreate()
        fetchSongs()
//            songRepository.getMusic().collect {
//                musicSource.fetchMediaData(it)
//            }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this),
        ) {
            cutSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(
            musicSource,
            playerPrepare = { mediaMetadataCompat, playWhenReady, playbackStartPositionMs ->
                curPlayingSong = mediaMetadataCompat

                preparePlayer(musicSource.songs, mediaMetadataCompat, true)
                showNotification()
            }, this
        )
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator(mediaSession))

        switchToPlayer(
            previousPlayer = null,
            newPlayer = if (castPlayer?.isCastSessionAvailable == true) castPlayer!! else exoPlayer
        )
//        mediaSessionConnector.setPlayer(currentPlayer)
        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
//        val mediaItem =
//            MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
//        exoPlayer.addMediaItem(mediaItem)
//        exoPlayer.prepare()
//        exoPlayer.setPlayWhenReady(true)
        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
    }


    private fun fetchSongs() {
        serviceScope.launch {
            songRepository.getMusic().collect {
                Log.d("serviceconnection", "on create")
                musicSource.fetchMediaData("song", it)
            }
        }
    }


    private fun showNotification() {
        musicNotificationManager.showNotification(exoPlayer)
    }

    fun hideNotification() {
        musicNotificationManager.hideNotification()
    }

    private inner class MusicQueueNavigator(mediaSession: MediaSessionCompat) :
        TimelineQueueNavigator(
            mediaSession
        ) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (windowIndex < currentPlaylistItems.size) {
                return currentPlaylistItems[windowIndex].description
            }
            return MediaDescriptionCompat.Builder().build()
        }

    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)

//        if(!applicationRepository.getVideoPlay()){
        serviceScope.launch {

                exoPlayer.setMediaSource(musicSource.asMediaSource(dataSourceFactoryFactory))
                exoPlayer.seekTo(curSongIndex, 0)
                exoPlayer.prepare()

            exoPlayer.playWhenReady = playNow
            currentPlaylistItems = songs

        }



//        } else {
//            exoPlayer.setMediaSource(musicSource.asVideoSource(dataSourceFactoryFactory))
//
//        }
//        val hlsMediaSource: HlsMediaSource = HlsMediaSource.Factory(dataSourceFactoryFactory)
//            .createMediaSource(MediaItem.fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"))
//        exoPlayer.setMediaSource(hlsMediaSource)




    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("serviceconnection", "disconnected")
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()

    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
//        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
//        val rootExtras = Bundle().apply {
//            putBoolean(
//                MEDIA_SEARCH_SUPPORTED,
//                isKnownCaller
//            )
//            putBoolean(CONTENT_STYLE_SUPPORTED, true)
//            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
//            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
//        }
//
//        return if (isKnownCaller) {
//            /**
//             * By default return the browsable root. Treat the EXTRA_RECENT flag as a special case
//             * and return the recent root instead.
//             */
//            val isRecentRequest = rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) ?: false
//            val browserRootPath = if (isRecentRequest) UAMP_RECENT_ROOT else UAMP_BROWSABLE_ROOT
//            BrowserRoot(browserRootPath, rootExtras)
//        } else {
//            /**
//             * Unknown caller. There are two main ways to handle this:
//             * 1) Return a root without any content, which still allows the connecting client
//             * to issue commands.
//             * 2) Return `null`, which will cause the system to disconnect the app.
//             *
//             * UAMP takes the first approach for a variety of reasons, but both are valid
//             * options.
//             */
//            BrowserRoot(UAMP_EMPTY_ROOT, rootExtras)
//        }
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {

            MEDIA_ROOT_ID -> {

                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {

                        preparePlayer(musicSource.songs, musicSource.songs[0], false)
                        result.sendResult(musicSource.asMediaItems())
                        if (!isPlayerInitialized && musicSource.songs.isNotEmpty()) {

                            isPlayerInitialized = true
                        }


                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }


}

const val UAMP_BROWSABLE_ROOT = "/"
const val UAMP_EMPTY_ROOT = "@empty@"
const val UAMP_RECOMMENDED_ROOT = "__RECOMMENDED__"
const val UAMP_ALBUMS_ROOT = "__ALBUMS__"
const val UAMP_RECENT_ROOT = "__RECENT__"

const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

const val RESOURCE_ROOT_URI = "android.resource://com.example.android.uamp.next/drawable/"
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2
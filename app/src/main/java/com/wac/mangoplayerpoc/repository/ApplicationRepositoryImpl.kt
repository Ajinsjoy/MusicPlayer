package com.wac.mangoplayerpoc.repository

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.bumptech.glide.RequestManager
import com.google.gson.Gson
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.common.Constants.MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
import com.wac.mangoplayerpoc.common.Constants.RECENT_SONG_ICON_URI_KEY
import com.wac.mangoplayerpoc.common.Constants.RECENT_SONG_MEDIA_ID_KEY
import com.wac.mangoplayerpoc.common.Constants.RECENT_SONG_POSITION_KEY
import com.wac.mangoplayerpoc.common.Constants.RECENT_SONG_SUBTITLE_KEY
import com.wac.mangoplayerpoc.common.Constants.RECENT_SONG_TITLE_KEY
import com.wac.mangoplayerpoc.data.VideoSongResponse
import com.wac.mangoplayerpoc.util.asAlbumArtContentUri
import javax.inject.Inject

class ApplicationRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val glide: RequestManager,
    private val application: Application
) : ApplicationRepository {
    override fun saveLastPlayed(description: MediaDescriptionCompat, position: Long) {
        val localIconUri = glide.asFile().load(description.iconUri)
            .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE).get()
            .asAlbumArtContentUri()

        sharedPreferences.edit()
            .putString(RECENT_SONG_MEDIA_ID_KEY, description.mediaId.toString())
            .putString(RECENT_SONG_TITLE_KEY, description.title.toString())
            .putString(RECENT_SONG_SUBTITLE_KEY, description.subtitle.toString())
            .putString(RECENT_SONG_ICON_URI_KEY, localIconUri.toString())
            .putLong(RECENT_SONG_POSITION_KEY, position)
            .apply()

    }


    override fun getLastPlayed(): MediaBrowserCompat.MediaItem? {
        val mediaId = sharedPreferences.getString(RECENT_SONG_MEDIA_ID_KEY, "")
        return if (mediaId != null) {
            null
        } else {
            val extras = Bundle().also {
                val position = sharedPreferences.getLong(RECENT_SONG_POSITION_KEY, 0L)
                it.putLong(MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS, position)
            }

            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(sharedPreferences.getString(RECENT_SONG_MEDIA_ID_KEY, ""))
                    .setTitle(sharedPreferences.getString(RECENT_SONG_TITLE_KEY, ""))
                    .setSubtitle(sharedPreferences.getString(RECENT_SONG_SUBTITLE_KEY, ""))
                    .setIconUri(
                        Uri.parse(
                            sharedPreferences.getString(
                                RECENT_SONG_ICON_URI_KEY,
                                ""
                            )
                        )
                    )
                    .setExtras(extras)
                    .build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }
    }

    override fun getVideoSong(): List<VideoSongResponse.VideoItem> {
        val musicDataInputStream = application.resources.openRawResource(R.raw.video)
        val music = musicDataInputStream.bufferedReader().use { it.readText() }
        val gsonObj = Gson()
        val musicResponse = gsonObj.fromJson(music, VideoSongResponse::class.java)
        return musicResponse.videoItem.map { it } ?: emptyList()

    }

    override fun setVideoPlay(set: Boolean) {
        sharedPreferences.edit().putBoolean("Video",set).apply()
    }
    override fun getVideoPlay(): Boolean {
        return sharedPreferences.getBoolean("Video",false)
    }



    companion object {
        const val NOTIFICATION_LARGE_ICON_SIZE = 144
    }
}
package com.wac.mangoplayerpoc.repository

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.data.MusicResponse
import com.wac.mangoplayerpoc.data.Video1Response
import com.wac.mangoplayerpoc.data.VideoSongResponse
import com.wac.mangoplayerpoc.data.model.MusicModel
import com.wac.mangoplayerpoc.exoplayer.MusicService
import com.wac.mangoplayerpoc.exoplayer.MusicServiceConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class SongRepositoryImpl @Inject constructor(
    private val application: Application

) :
    SongRepository {


    private val musicDataInputStream = application.resources.openRawResource(R.raw.music)
    private val music = musicDataInputStream.bufferedReader().use { it.readText() }
    private val gsonObj = Gson()
    private val musicResponse: MusicResponse = gsonObj.fromJson(music, MusicResponse::class.java)

    var song by mutableStateOf(musicResponse.music?.map { it.toMusic() } ?: emptyList())

    override fun getMusic(): Flow<List<MusicModel>> = flow {
        emit(song)
    }

    override fun getVideoMusic(): Flow<List<MusicModel>> = flow {

        val musicDataInputStream = application.resources.openRawResource(R.raw.video)
        val music = musicDataInputStream.bufferedReader().use { it.readText() }
        val gsonObj = Gson()
        val musicResponse = gsonObj.fromJson(music, VideoSongResponse::class.java)
        emit(musicResponse.videoItem.map { it.toMusic() })

    }

    override fun getVideoSong(): List<Video1Response.Video> {
        val musicDataInputStream = application.resources.openRawResource(R.raw.video1)
        val music = musicDataInputStream.bufferedReader().use { it.readText() }
        val gsonObj = Gson()
        val musicResponse = gsonObj.fromJson(music, Video1Response::class.java)
        return musicResponse.videos?.map { it } ?: emptyList()

    }

    override fun setVideoMusic(map: List<MusicModel>) {
        song = map


    }
}
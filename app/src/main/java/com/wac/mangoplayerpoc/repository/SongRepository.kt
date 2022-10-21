package com.wac.mangoplayerpoc.repository


import com.wac.mangoplayerpoc.data.Video1Response
import com.wac.mangoplayerpoc.data.model.MusicModel
import kotlinx.coroutines.flow.Flow


interface SongRepository {
    fun getMusic(): Flow<List<MusicModel>>
    fun getSong(): Flow<List<MusicModel>>
    fun getVideoMusic(): Flow<List<MusicModel>>
    fun setVideoMusic(map: List<MusicModel>)
    fun getVideoSong(): List<Video1Response.Video>
}
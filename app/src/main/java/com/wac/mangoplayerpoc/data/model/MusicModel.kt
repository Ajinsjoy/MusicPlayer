package com.wac.mangoplayerpoc.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicModel(
    val album: String,
    val artist: String,
    val duration: Int,
    val genre: String,
    val id: String,
    val image: String,
    val site: String,
    val source: String,
    val title: String,
    val totalTrackCount: Int,
    val trackNumber: Int
):Parcelable{
    fun toSong(): Song {
        return Song(
            id ?: "",
            title ?: "",
            artist?:"",
            source ?: "",
            image ?: "",
        )
    }
}
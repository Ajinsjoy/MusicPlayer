package com.wac.mangoplayerpoc.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wac.mangoplayerpoc.data.model.MusicModel
import com.wac.mangoplayerpoc.data.model.Song

@JsonClass(generateAdapter = true)
data class VideoSongResponse(
    @Json(name = "videoItem")
    val videoItem: List<VideoItem>
) {
    @JsonClass(generateAdapter = true)
    data class VideoItem(
        @Json(name = "album")
        val album: String,
        @Json(name = "artist")
        val artist: String,
        @Json(name = "description")
        val description: String,
        @Json(name = "genre")
        val genre: String,
        @Json(name = "id")
        val id: String,
        @Json(name = "image")
        val image: String,
        @Json(name = "source")
        val source: String,
        @Json(name = "title")
        val title: String
    ){
        fun toSong(): Song {
            return Song(
                id ?: "",
                title ?: "",
                artist?:"",
                source ?: "",
                image ?: "",
            )
        }

        fun toMusic(): MusicModel {
            return MusicModel(
                album ?: "",
                artist ?: "",
                 -1,
                genre ?: "",
                id ?: "",
                image ?: "",
                 "",
                source ?: "",
                title ?: "",
                 0,
                 0
            )
        }
    }
}
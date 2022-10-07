package com.wac.mangoplayerpoc.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wac.mangoplayerpoc.data.model.MusicModel
import com.wac.mangoplayerpoc.data.model.Song

@JsonClass(generateAdapter = true)
data class Video1Response(
    @Json(name = "videos")
    val videos: List<Video>?
) {
    @JsonClass(generateAdapter = true)
    data class Video(
        @Json(name = "album")
        val album: String?,
        @Json(name = "artist")
        val artist: String?,
        @Json(name = "description")
        val description: String?,
        @Json(name = "id")
        val id: String?,
        @Json(name = "sources")
        val sources: String?,
        @Json(name = "subtitle")
        val subtitle: String?,
        @Json(name = "thumb")
        val thumb: String?,
        @Json(name = "title")
        val title: String?
    ) {
        fun toSong(): Song {
            return Song(
                id ?: "",
                title ?: "",
                artist ?: "",
                sources ?: "",
                thumb ?: "",
            )
        }

        fun toMusic(): MusicModel {
            return MusicModel(
                album ?: "",
                artist ?: "",
                -1,
                "",
                id ?: "",
                thumb ?: "",
                "",
                sources ?: "",
                title ?: "",
                0,
                0
            )
        }
    }
}
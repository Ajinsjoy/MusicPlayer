package com.wac.mangoplayerpoc.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.wac.mangoplayerpoc.data.model.MusicModel

@JsonClass(generateAdapter = true)
data class MusicResponse(
    @Json(name = "music")
    val music: List<Music>?
) {
    @JsonClass(generateAdapter = true)
    data class Music(
        @Json(name = "album")
        val album: String,
        @Json(name = "artist")
        val artist: String,
        @Json(name = "duration")
        val duration: Int,
        @Json(name = "genre")
        val genre: String,
        @Json(name = "id")
        val id: String,
        @Json(name = "image")
        val image: String,
        @Json(name = "site")
        val site: String,
        @Json(name = "source")
        val source: String,
        @Json(name = "title")
        val title: String,
        @Json(name = "totalTrackCount")
        val totalTrackCount: Int,
        @Json(name = "trackNumber")
        val trackNumber: Int
    ) {
        fun toMusic(): MusicModel {
            return MusicModel(
                album ?: "",
                artist ?: "",
                duration ?: -1,
                genre ?: "",
                id ?: "",
                image ?: "",
                site ?: "",
                source ?: "",
                title ?: "",
                totalTrackCount ?: 0,
                trackNumber ?: 0
            )
        }
    }
}
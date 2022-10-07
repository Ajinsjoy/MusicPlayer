package com.wac.mangoplayerpoc.presentation.musiclist

import com.wac.mangoplayerpoc.data.model.Song

data class MusicListState(
    val song: List<Song>? = null,
    val loading: Boolean = false
)
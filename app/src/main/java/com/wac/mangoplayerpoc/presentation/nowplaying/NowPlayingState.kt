package com.wac.mangoplayerpoc.presentation.nowplaying

import com.wac.mangoplayerpoc.data.model.Song

data class NowPlayingState(
    val song: Song? = null,
    val songList: List<Song>? = null,
    val loading: Boolean = false,
    val isPlaying: Boolean = false,
    val progress: Int = 0,
    val duration: Long = 0,
    val buffering: Boolean = false
)
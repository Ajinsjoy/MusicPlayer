package com.wac.mangoplayerpoc.presentation.main

import com.wac.mangoplayerpoc.data.model.Song

data class MainActivityState(
    val song: Song? = null,
    val loading: Boolean = false,
    val isPlaying: Boolean = false,
    val showLastPlayedSong: Boolean = false,
    val progress: Int = 0,
    val duration: Long = 0,
    val playVideo:Boolean = false,
    val pipMode:Boolean =false,
    var curPlayingSong: Song? = null,
)
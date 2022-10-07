package com.wac.mangoplayerpoc.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val subTitle: String,
    val songUrl: String,
    val imageUrl: String
) : Parcelable

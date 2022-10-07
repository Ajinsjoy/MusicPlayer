package com.wac.mangoplayerpoc.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reel(
    val id: Int,
   val video: String,
    val userImage: String,
    val userName: String,
    val videoImage:String,
    val isLiked: Boolean = false,
    val likesCount: Int,
    val comment: String,
    val commentsCount: Int
) : Parcelable{
    fun getVideoUrl(): Uri {
        return Uri.parse("asset:///${video}")
    }
}
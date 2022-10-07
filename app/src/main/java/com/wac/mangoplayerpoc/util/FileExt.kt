package com.wac.mangoplayerpoc.util

import android.content.ContentResolver
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.MimeTypes
import java.io.File

fun File.asAlbumArtContentUri(): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .appendPath(this.path)
        .build()
}

 fun MediaMetadataCompat.toMediaItem(): MediaItem {
    return with(MediaItem.Builder()) {
        setMediaId(description.mediaId.toString())
        setUri(description.mediaUri)
        setMimeType(MimeTypes.AUDIO_MPEG)
        setMediaMetadata(toMediaItemMetadata())
    }.build()
}
private fun MediaMetadataCompat.toMediaItemMetadata(): MediaMetadata {
    return with(MediaMetadata.Builder()) {
        setTitle(description.title)
        setDisplayTitle(description.title)
        setAlbumTitle(description.subtitle)
    }.build()
}

private const val AUTHORITY = "com.example.android.uamp"
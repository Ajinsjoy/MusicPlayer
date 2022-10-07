package com.wac.mangoplayerpoc.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
//
//    @Provides
//    @ServiceScoped
//    fun provideAudioAttribute() = AudioAttributes.Builder()
//        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
//        .setUsage(C.USAGE_MEDIA)
//        .build()
//
//    @Provides
//    @ServiceScoped
//    fun provideExoPlayer(
//        @ApplicationContext context: Context,
//        audioAttributes: AudioAttributes
//    ) = ExoPlayer.Builder(context).build().apply {
//        setAudioAttributes(audioAttributes, true)
//        setHandleAudioBecomingNoisy(true)
//
//    }

    @Provides
    @ServiceScoped
    fun provideDataSourceFactory(@ApplicationContext context: Context) =
        DefaultDataSource.Factory(
            context
//        DefaultDataSourceFactory(context,Util.getUserAgent(context,"Mongo App")
        )


}
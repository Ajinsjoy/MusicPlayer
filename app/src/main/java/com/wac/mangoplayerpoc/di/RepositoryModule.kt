package com.wac.mangoplayerpoc.di

import com.wac.mangoplayerpoc.exoplayer.ConnectService
import com.wac.mangoplayerpoc.exoplayer.MusicService
import com.wac.mangoplayerpoc.repository.ApplicationRepository
import com.wac.mangoplayerpoc.repository.ApplicationRepositoryImpl
import com.wac.mangoplayerpoc.repository.SongRepository
import com.wac.mangoplayerpoc.repository.SongRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideSongRepository(songRepositoryImpl: SongRepositoryImpl): SongRepository {
        return songRepositoryImpl
    }





}
package com.example.dleep2.di

import com.example.dleep2.data.remote.MusicDatabase
import com.example.dleep2.exoplayer.FirebaseMusicSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideFirebaseMusicSource(musicDatabase: MusicDatabase): FirebaseMusicSource {
        return FirebaseMusicSource(musicDatabase)
    }

    @Provides
    @ViewModelScoped
    fun provideMusicDatabase(): MusicDatabase {
        return MusicDatabase()
    }
}

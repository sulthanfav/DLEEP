package com.example.dleep2.di

import android.content.Context
import androidx.room.Room
import com.example.dleep2.data.AppDatabase
import com.example.dleep2.data.RecentlyPlayedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database").build()
    }

    @Provides
    fun provideRecentlyPlayedDao(db: AppDatabase): RecentlyPlayedDao {
        return db.recentlyPlayedDao()
    }
}

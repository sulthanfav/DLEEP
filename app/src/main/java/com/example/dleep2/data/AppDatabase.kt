package com.example.dleep2.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dleep2.data.entities.RecentlyPlayed

@Database(entities = [RecentlyPlayed::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
}

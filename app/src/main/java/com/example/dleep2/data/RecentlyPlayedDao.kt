package com.example.dleep2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dleep2.data.entities.RecentlyPlayed

@Dao
interface RecentlyPlayedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentlyPlayed: RecentlyPlayed)

    @Query("SELECT * FROM recently_played ORDER BY id DESC LIMIT 10")
    suspend fun getRecentlyPlayed(): List<RecentlyPlayed>
}

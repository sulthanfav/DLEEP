package com.example.dleep2.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayed(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: String,
    val title: String
)

package com.example.dleep2.data.remote

import com.example.dleep2.data.entities.video
import com.example.dleep2.other.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VideoDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val videoCollection = firestore.collection(Constants.VIDEO_COLLECTION)

    suspend fun getAllVideo(): List<video> {
        return try {
            videoCollection.get().await().toObjects(video::class.java)
        } catch(e: Exception) {
            emptyList()
        }
    }
}
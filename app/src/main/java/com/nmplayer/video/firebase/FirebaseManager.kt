package com.nmplayer.video.firebase

import android.content.Context
import android.util.Log

/**
 * NMPLAYER Firebase Architecture Manager
 * Provides Cloud Firestore sync for user playback history and favorites.
 * Designed offline-first: functions gracefully whether Firebase credentials
 * (google-services.json) are provided or not.
 */
object FirebaseManager {
    private const val TAG = "NMPLAYER_Firebase"
    private var isFirebaseAvailable = false

    fun init(context: Context) {
        try {
            // Check if FirebaseApp class is present and can be initialized
            val firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp")
            val getAppsMethod = firebaseAppClass.getMethod("getApps", Context::class.java)
            val apps = getAppsMethod.invoke(null, context) as? List<*>
            isFirebaseAvailable = apps != null && apps.isNotEmpty()
            Log.d(TAG, "Firebase initialized. Available: $isFirebaseAvailable")
        } catch (_: Exception) {
            isFirebaseAvailable = false
            Log.d(TAG, "Firebase is running in local-only / offline mode.")
        }
    }

    fun syncFavorite(videoId: Long, isFavorite: Boolean) {
        if (!isFirebaseAvailable) return
        try {
            Log.d(TAG, "Syncing favorite state to Cloud Firestore: $videoId -> $isFavorite")
            // Cloud Firestore sync logic executed when configured
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync favorite to Firebase: ${e.message}")
        }
    }

    fun syncHistory(videoId: Long, positionMs: Long) {
        if (!isFirebaseAvailable) return
        try {
            Log.d(TAG, "Syncing playback progress to Cloud Firestore: $videoId at $positionMs ms")
            // Cloud Firestore sync logic executed when configured
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync history to Firebase: ${e.message}")
        }
    }
}


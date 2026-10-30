package com.nmplayer.video.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nmplayer_preferences", Context.MODE_PRIVATE)

    companion object {
        const val KEY_THEME_MODE = "key_theme_mode"
        const val THEME_SYSTEM = "system"
        const val THEME_DARK = "dark"
        const val THEME_LIGHT = "light"

        const val KEY_DEFAULT_SPEED = "key_default_speed"
        const val KEY_DEFAULT_ASPECT = "key_default_aspect"
        const val KEY_FAVORITES = "key_favorites"
        const val KEY_HISTORY = "key_history"
        const val KEY_LAST_VIDEO_ID = "key_last_video_id"
        const val KEY_LAST_FOLDER_ID = "key_last_folder_id"
        const val KEY_LAST_POSITION = "key_last_position"
    }

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_DARK) ?: THEME_DARK
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    fun applyTheme() {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    var defaultSpeed: Float
        get() = prefs.getFloat(KEY_DEFAULT_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_DEFAULT_SPEED, value).apply()

    var defaultAspectRatio: Int
        get() = prefs.getInt(KEY_DEFAULT_ASPECT, 0) // 0 = FIT, 1 = FILL, 2 = ZOOM
        set(value) = prefs.edit().putInt(KEY_DEFAULT_ASPECT, value).apply()

    // Favorites Management
    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(videoId: Long): Boolean {
        return getFavoriteIds().contains(videoId.toString())
    }

    fun toggleFavorite(videoId: Long): Boolean {
        val set = getFavoriteIds().toMutableSet()
        val idStr = videoId.toString()
        val isFav: Boolean
        if (set.contains(idStr)) {
            set.remove(idStr)
            isFav = false
        } else {
            set.add(idStr)
            isFav = true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, set).apply()
        return isFav
    }

    // Playback Position & History
    fun saveVideoProgress(videoId: Long, folderId: Long, positionMs: Long, durationMs: Long) {
        prefs.edit()
            .putLong(KEY_LAST_VIDEO_ID, videoId)
            .putLong(KEY_LAST_FOLDER_ID, folderId)
            .putLong(KEY_LAST_POSITION, positionMs)
            .putLong("pos_$videoId", positionMs)
            .putLong("dur_$videoId", durationMs)
            .putLong("time_$videoId", System.currentTimeMillis())
            .apply()

        // Update recent list
        val historyList = getHistoryIds().toMutableList()
        val idStr = videoId.toString()
        historyList.remove(idStr)
        historyList.add(0, idStr) // Add to head
        if (historyList.size > 50) {
            historyList.removeAt(historyList.size - 1)
        }
        val array = JSONArray(historyList)
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun getLastPosition(videoId: Long): Long {
        return prefs.getLong("pos_$videoId", 0L)
    }

    fun getHistoryIds(): List<String> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    val lastVideoId: Long
        get() = prefs.getLong(KEY_LAST_VIDEO_ID, -1L)

    val lastFolderId: Long
        get() = prefs.getLong(KEY_LAST_FOLDER_ID, -1L)
}


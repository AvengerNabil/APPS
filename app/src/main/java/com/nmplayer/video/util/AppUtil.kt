package com.nmplayer.video.util

import android.content.Context
import android.net.Uri
import android.text.format.DateUtils
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

object AppUtil {
    // get storage path of media file for android Q and above
    fun getRealPath(uri: Uri, context: Context): String? {
        var realPath: String? = null
        try {
            if (uri.scheme == AppConstant.CONTENT) {
                val projection = arrayOf(AppConstant.DATA)
                val cursor = context.contentResolver.query(
                    uri,
                    projection, null, null, null
                )
                if (cursor != null) {
                    val id = cursor.getColumnIndexOrThrow(AppConstant.DATA)
                    cursor.moveToNext()
                    realPath = try {
                        cursor.getString(id)
                    } catch (e: Exception) {
                        null
                    } finally {
                        cursor.close()
                    }
                } else if (uri.scheme == AppConstant.FILE) {
                    realPath = uri.path
                }
            } else if (uri.scheme == AppConstant.FILE) {
                realPath = uri.path
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            realPath = null
        }
        return realPath
    }

    fun getDate(date: Long): String {
        var tempDate = date
        tempDate *= DateUtils.SECOND_IN_MILLIS
        return SimpleDateFormat(AppConstant.DATE_TIME_FORMAT, Locale.getDefault()).format(
            Date(tempDate)
        )
    }

    fun formatDuration(durationMs: Long): String {
        val totalSec = durationMs / 1000
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}


package com.soufianekre.floatingdraw.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.soufianekre.floatingdraw.R
import com.soufianekre.floatingdraw.models.FileDirItem
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream


fun Context.isPathOnSD(path: String) = sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)

fun Context.getIsPathDirectory(path: String): Boolean {
    return File(path).isDirectory

}

fun Context.getMediaStoreLastModified(path: String): Long {
    val projection = arrayOf(
        MediaStore.MediaColumns.DATE_MODIFIED
    )

    val uri = getFileUri(path)
    val selection = "${BaseColumns._ID} = ?"
    val selectionArgs = arrayOf(path.substringAfterLast("/"))

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(MediaStore.MediaColumns.DATE_MODIFIED) * 1000
            }
        }
    } catch (ignored: Exception) {
        showError(ignored.localizedMessage)
    }
    return 0
}

fun Context.getHumanReadablePath(path: String): String {
    return getString(
        when (path) {
            "/" -> R.string.root
            internalStoragePath -> R.string.internal
            else -> R.string.sd_card
        }
    )
}


fun Context.humanizePath(path: String): String {
    val trimmedPath = path.trimEnd('/')
    val basePath = path.getBasePath(this)
    return when (basePath) {
        "/" -> "${getHumanReadablePath(basePath)}$trimmedPath"
        else -> trimmedPath.replaceFirst(basePath, getHumanReadablePath(basePath))
    }
}


//fun Context.getTimeFormat() = if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12

fun Context.getResolution(path: String): Point? {
    return if (path.isImageFast() || path.isImageSlow()) {
        path.getImageResolution()
    } else {
        null
    }
}

fun Context.getTitle(path: String): String? {
    val projection = arrayOf(
        MediaStore.MediaColumns.TITLE
    )

    val uri = getFileUri(path)
    val selection =
        if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaStore.MediaColumns.DATA} = ?"
    val selectionArgs =
        if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(MediaStore.MediaColumns.TITLE)
            }
        }
    } catch (ignored: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    } catch (ignored: Exception) {
        null
    }
}


fun Context.getFileUri(path: String) = when {
    path.isImageSlow() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    else -> MediaStore.Files.getContentUri("external")
}



fun Context.isSDCardSetAsDefaultStorage() =
    sdCardPath.isNotEmpty() && Environment.getExternalStorageDirectory().absolutePath.equals(
        sdCardPath,
        true
    )




package com.soufianekre.floatingdraw.models

import android.content.Context
import android.net.Uri
import com.soufianekre.floatingdraw.data.app_prefs.*
import com.soufianekre.floatingdraw.extensions.*
import com.soufianekre.floatingdraw.helper.*
import java.io.File

open class FileDirItem(val path: String, val name: String = "", var isDirectory: Boolean = false,
                       var children: Int = 0, var size: Long = 0L,
                       var modified: Long = 0L) : Comparable<FileDirItem> {
    companion object {
        var sorting = 0
    }

    override fun toString() = "FileDirItem(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified)"

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> {
                    result = if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(name.toLowerCase(), other.name.toLowerCase())
                    } else {
                        name.toLowerCase().compareTo(other.name.toLowerCase())
                    }
                }
                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }
                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }
                else -> {
                    result = getExtension().toLowerCase().compareTo(other.getExtension().toLowerCase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")


    fun getProperSize(context: Context, countHidden: Boolean): Long {
        return if (isNougatPlus() && path.startsWith("content://")) {
            try {
                context.contentResolver.openInputStream(Uri.parse(path))?.available()?.toLong() ?: 0L
            } catch (e: Exception) {
                0L
            }
        } else {
            File(path).getProperSize(countHidden)
        }
    }

    fun getProperFileCount(context: Context, countHidden: Boolean): Int {
        return File(path).getFileCount(countHidden)
    }

    fun getDirectChildrenCount(context: Context, countHiddenItems: Boolean): Int {
        return File(path).getDirectChildrenCount(countHiddenItems)
        }


    fun getLastModified(context: Context): Long {
        return if (isNougatPlus() && path.startsWith("content://")) {
            context.getMediaStoreLastModified(path)
        } else {
            File(path).lastModified()
        }
    }

    fun getParentPath() = path.getParentPath()


    fun getFileDurationSeconds(context: Context) = context.getDuration(path)


    fun getAlbum(context: Context) = context.getAlbum(path)

    fun getTitle(context: Context) = context.getTitle(path)

    fun getResolution(context: Context) = context.getResolution(path)

    fun getImageResolution() = path.getImageResolution()

    //fun getPublicUri(context: Context) = context.getDocumentFile(path)?.uri ?: ""
}
